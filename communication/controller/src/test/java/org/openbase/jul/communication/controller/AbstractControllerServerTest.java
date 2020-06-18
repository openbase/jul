package org.openbase.jul.communication.controller;

/*
 * #%L
 * JUL Extension Controller
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.junit.*;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.StackTracePrinter;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.openbase.jul.schedule.SyncObject;
import org.openbase.type.domotic.registry.UnitRegistryDataType.UnitRegistryData.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import org.openbase.type.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openbase.type.domotic.state.ConnectionStateType.ConnectionState.State.*;
import static org.openbase.type.domotic.state.AvailabilityStateType.AvailabilityState.State.*;

/**
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class AbstractControllerServerTest {

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitConfig.getDefaultInstance()));
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public AbstractControllerServerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private boolean firstSync = false;
    private boolean secondSync = false;
    private AbstractControllerServer communicationService;

    /**
     * Test if the initial sync that happens if a communication service starts
     * successfully publishes its data to a remote.
     *
     * @throws Exception
     */
    @Test(timeout = 20000)
    public void testInitialSync() throws Exception {
        System.out.println("testInitialSync");

        String scope = "/test/synchronization";
        final SyncObject waitForDataSync = new SyncObject("WaitForDataSync");
        UnitConfig unit1 = UnitConfig.newBuilder().setId("Location1").build();
        UnitRegistryData.Builder testData = UnitRegistryData.getDefaultInstance().toBuilder().addLocationUnitConfig(unit1);
        communicationService = new AbstractControllerServerImpl(testData);
        communicationService.init(scope);
        communicationService.activate();

        AbstractRemoteClient remoteService = new AbstractRemoteClientImpl();
        remoteService.init(scope);
        remoteService.addDataObserver((Observer< DataProvider<UnitRegistryData>, UnitRegistryData>) (source, data) -> {
            if (data.getLocationUnitConfigCount() == 1 && data.getLocationUnitConfig(0).getId().equals("Location1")) {
                firstSync = true;
                synchronized (waitForDataSync) {
                    waitForDataSync.notifyAll();
                }
            }
            if (data.getLocationUnitConfigCount() == 2 && data.getLocationUnitConfig(0).getId().equals("Location1") && data.getLocationUnitConfig(1).getId().equals("Location2")) {
                secondSync = true;
                synchronized (waitForDataSync) {
                    waitForDataSync.notifyAll();
                }
            }
        });

        synchronized (waitForDataSync) {
            if (firstSync == false) {
                remoteService.activate();
                waitForDataSync.wait();
            }
        }
        assertTrue("Synchronization after the start of the remote service has not been done", firstSync);

        communicationService.shutdown();
        UnitConfig location2 = UnitConfig.newBuilder().setId("Location2").build();
        testData.addLocationUnitConfig(location2);
        communicationService = new AbstractControllerServerImpl(testData);
        communicationService.init(scope);

        synchronized (waitForDataSync) {
            if (secondSync == false) {
                communicationService.activate();
                waitForDataSync.wait();
            }
        }
        assertTrue("Synchronization after the restart of the communication service has not been done", secondSync);

        communicationService.deactivate();

        remoteService.addConnectionStateObserver(((source, data) -> {
            logger.info("ConnectionState [" + data + "]");
        }));

        try {
            remoteService.ping().get();
            assertTrue("Pinging was not canceled after timeout.", false);
        } catch (ExecutionException ex) {
            // ping canceled
        }

        assertEquals("Remote is still connected after remote service shutdown!", CONNECTING, remoteService.getConnectionState());
        communicationService.activate();
        communicationService.waitForAvailabilityState(ONLINE);
        remoteService.waitForConnectionState(CONNECTED);
        remoteService.shutdown();
        communicationService.shutdown();
        assertEquals("Communication Service is not offline after shutdown!", OFFLINE, communicationService.getAvailabilityState());
        assertEquals("Remote is not disconnected after shutdown!", DISCONNECTED, remoteService.getConnectionState());
    }

    /**
     * Test if a RemoteService will reconnect when the communication service
     * restarts.
     *
     * @throws Exception
     */
    @Test(timeout = 20000)
    public void testReconnection() throws Exception {
        // todo: this test takes to much time! Even more after increasing the deactivation timeout in the RSBSynchronizedParticipant class. There seems to be an issue that rsb takes to many time during deactivation.
        System.out.println("testReconnection");

        AbstractRemoteClient remoteService = new AbstractRemoteClientImpl();
        remoteService.addConnectionStateObserver((source, data) -> {
            logger.info("New connection state [" + data + "]");
        });

        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start();

        String scope = "/test/reconnection";
        UnitConfig location1 = UnitConfig.newBuilder().setId("Location1").build();
        UnitRegistryData.Builder testData = UnitRegistryData.getDefaultInstance().toBuilder().addLocationUnitConfig(location1);
        communicationService = new AbstractControllerServerImpl(testData);
        communicationService.init(scope);
        communicationService.activate();

        logger.info(stopWatch.stop() + "ms to activate communication service");
        stopWatch.restart();

        remoteService.init(scope);
        remoteService.activate();
        logger.info(stopWatch.getTime() + "ms to activate remote service");
        stopWatch.restart();

        logger.info("wait for inital connection...");
        remoteService.waitForConnectionState(CONNECTED);

        logger.info(stopWatch.getTime() + "ms till remote service connected");
        stopWatch.restart();

        communicationService.deactivate();

        logger.info(stopWatch.getTime() + "ms till communication service deactivated");
        stopWatch.restart();

        logger.info("wait for connection loss after controller shutdown...");
        remoteService.waitForConnectionState(CONNECTING);

        logger.info(stopWatch.getTime() + "ms till remote service switched to connecting");
        stopWatch.restart();

        communicationService.activate();

        logger.info(stopWatch.getTime() + "ms till communication service reactivated");
        stopWatch.restart();

        logger.info("wait for reconnection after controller start...");
        remoteService.waitForConnectionState(CONNECTED);

        logger.info(stopWatch.getTime() + "ms till remote service reconnected");
        stopWatch.restart();

        remoteService.shutdown();

        logger.info(stopWatch.getTime() + "ms till remote service shutdown");
        stopWatch.restart();

        logger.info("wait for remote shutdown...");
        remoteService.waitForConnectionState(DISCONNECTED);

        logger.info(stopWatch.getTime() + "ms till remote service switched to disconnected");
        stopWatch.restart();

        communicationService.shutdown();

        logger.info(stopWatch.getTime() + "ms till communication service shutdown");
        stopWatch.restart();
    }

    /**
     * Test waiting for data from a communication service.
     *
     * @throws Exception
     */
    @Test(timeout = 10000)
    public void testWaitForData() throws Exception {
        System.out.println("testWaitForData");

        String scope = "/test/waitfordata";
        UnitConfig location1 = UnitConfig.newBuilder().setId("Location1").build();
        UnitRegistryData.Builder testData = UnitRegistryData.getDefaultInstance().toBuilder().addLocationUnitConfig(location1);

        AbstractRemoteClient remoteService = new AbstractRemoteClientImpl();
        remoteService.init(scope);
        communicationService = new AbstractControllerServerImpl(testData);
        communicationService.init(scope);

        remoteService.activate();

        Future dataFuture = remoteService.getDataFuture();

        communicationService.activate();

        assertEquals("DataFuture did not return data from communicationService!", communicationService.getData(), dataFuture.get());

        communicationService.shutdown();
        remoteService.shutdown();
    }

    /**
     * Test requesting data from a communication service.
     *
     * @throws Exception
     */
    @Test(timeout = 10000)
    public void testRequestData() throws Exception {
        System.out.println("testRequestData");

        String scope = "/test/requestdata";
        UnitConfig location1 = UnitConfig.newBuilder().setId("Location1").build();
        UnitRegistryData.Builder testData = UnitRegistryData.getDefaultInstance().toBuilder().addLocationUnitConfig(location1);

        AbstractRemoteClient remoteService = new AbstractRemoteClientImpl();
        remoteService.init(scope);
        communicationService = new AbstractControllerServerImpl(testData);
        communicationService.init(scope);

        remoteService.activate();
        communicationService.activate();

        remoteService.requestData().get();

        assertEquals("CommunicationService data and remoteService data do not match after requestData!", communicationService.getData(), remoteService.getData());

        communicationService.shutdown();
        remoteService.shutdown();
    }

    /**
     * Test if when there are 2 remotes connected to a communication service
     * the shutdown of one remote affects the communication of the other one.
     *
     * @throws Exception
     */
    @Test(timeout = 10000)
    public void testRemoteInterference() throws Exception {
        System.out.println("testRemoteInterference");

        String scope = "/test/interference";
        UnitConfig location1 = UnitConfig.newBuilder().setId("Location1").build();
        UnitRegistryData.Builder testData = UnitRegistryData.getDefaultInstance().toBuilder().addLocationUnitConfig(location1);

        AbstractRemoteClient remoteService1 = new AbstractRemoteClientImpl();
        AbstractRemoteClient remoteService2 = new AbstractRemoteClientImpl();
        remoteService1.init(scope);
        remoteService2.init(scope);
        communicationService = new AbstractControllerServerImpl(testData);
        communicationService.init(scope);
        communicationService.activate();

        remoteService1.activate();
        remoteService2.activate();

        System.out.println("remoteService1.waitForConnectionState(CONNECTED)");
        remoteService1.waitForConnectionState(CONNECTED);
        System.out.println("remoteService2.waitForConnectionState(CONNECTED)");
        remoteService2.waitForConnectionState(CONNECTED);

        remoteService1.shutdown();
        System.out.println("remoteService1.waitForConnectionState(DISCONNECTED)");
        remoteService1.waitForConnectionState(DISCONNECTED);

        assertEquals("Remote connected to the same service got shutdown too", CONNECTED, remoteService2.getConnectionState());
        remoteService2.requestData().get();

        communicationService.deactivate();

        System.out.println("remoteService2.waitForConnectionState(CONNECTING)");
        remoteService2.waitForConnectionState(CONNECTING);

        communicationService.activate();
        System.out.println("remoteService2.waitForConnectionState(CONNECTED)");
        remoteService2.waitForConnectionState(CONNECTED);
        assertEquals("Remote reconnected even though it already shutdown", DISCONNECTED, remoteService1.getConnectionState());

        remoteService2.shutdown();
        communicationService.shutdown();
    }

//    Temporally disabled until issue openbase/jul#55 has been solved.
//    /**
//     * @throws Exception
//     */
//    @Test(timeout = 10000)
//    public void testNotification() throws Exception {
//        System.out.println("testNotification");
//
//        String scope = "/test/notification";
//        UnitConfig location = UnitConfig.newBuilder().setId("id").build();
//        communicationService = new AbstractControllerServerImpl(UnitRegistryData.getDefaultInstance().toBuilder().addLocationUnitConfig(location));
//        communicationService.init(scope);
//
//        AbstractRemoteClient remoteService = new AbstractRemoteClientImpl();
//        remoteService.init(scope);
//        remoteService.activate();
//
//        GlobalCachedExecutorService.submit( () -> {
//            try {
//                // make sure the remote is ready to wait for data
//                Thread.sleep(10);
//                communicationService.activate();
//                // notification should be send automatically.
//            } catch (Exception ex) {
//                ExceptionPrinter.printHistory(new FatalImplementationErrorException(this, ex), System.err);
//            }
//        });
//
//        remoteService.waitForData();
//        try {
//            remoteService.ping().get(500, TimeUnit.MILLISECONDS);
//        } catch (TimeoutException ex) {
//
//            StackTracePrinter.printAllStackTraces(LoggerFactory.getLogger(getClass()), LogLevel.WARN);
//            Assert.fail("Even though wait for data returned the pinging immediatly afterwards took to long. Please check stacktrace for deadlocks...");
//        }
//
//        remoteService.deactivate();
//        communicationService.deactivate();
//    }

    /**
     *
     * @throws Exception
     */
    @Test(timeout = 20000)
    public void testReinit() throws Exception {
        System.out.println("testReinit");

        final int TEST_PARALLEL_REINIT_TASKS = 5;

        String scope = "/test/notification";
        UnitConfig location = UnitConfig.newBuilder().setId("id").build();
        communicationService = new AbstractControllerServerImpl(UnitRegistryData.getDefaultInstance().toBuilder().addLocationUnitConfig(location));
        communicationService.init(scope);
        communicationService.activate();

        AbstractRemoteClient remoteService = new AbstractRemoteClientImpl();
        remoteService.init(scope);
        remoteService.activate();

        final Runnable reinitTask = () -> {
            try {
                remoteService.reinit();
                remoteService.requestData().get();
            } catch (Exception ex) {
                ExceptionPrinter.printHistory("reinit failed!", ex, logger);
            }
        };

        // execute reinits
        final ArrayList<Future> taskFutures = new ArrayList<>();
        for (int i = 0; i < TEST_PARALLEL_REINIT_TASKS; i++) {
            taskFutures.add(GlobalCachedExecutorService.submit(reinitTask));
        }
        for (Future future : taskFutures) {
            try {
                future.get(15, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                //StackTracePrinter.printAllStackTrace(AbstractControllerServerTest.class);
                StackTracePrinter.detectDeadLocksAndPrintStackTraces(AbstractControllerServerTest.class);
                Assert.fail("Reint took too long! Please analyse deadlock in stacktrace...");
            }
        }

        remoteService.waitForData();
        try {
            remoteService.ping().get(500, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            Assert.fail("Even though wait for data returned the pinging immediately afterwards failed");
        }
        communicationService.deactivate();
        remoteService.deactivate();
        remoteService.reinit();

        communicationService.shutdown();
        remoteService.shutdown();


        try {
            remoteService.reinit();
            Assert.fail("No exception occurred.");
        } catch (CouldNotPerformException ex) {
            // this should happen
        }
    }

    public static class AbstractControllerServerImpl extends AbstractControllerServer<UnitRegistryData, Builder> {

        static {
            DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitRegistryData.getDefaultInstance()));
        }

        public AbstractControllerServerImpl(UnitRegistryData.Builder builder) throws InstantiationException {
            super(builder);
        }

        @Override
        public void registerMethods(RSBLocalServer server) {
        }
    }

    public static class AbstractRemoteClientImpl extends AbstractRemoteClient<UnitRegistryData> {

        static {
            DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitRegistryData.getDefaultInstance()));
        }

        public AbstractRemoteClientImpl() {
            super(UnitRegistryData.class);
        }
    }
}
