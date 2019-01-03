package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.pattern.Observer;
import org.openbase.type.domotic.state.AvailabilityStateType.AvailabilityState;
import org.openbase.type.domotic.state.ConnectionStateType;
import org.openbase.type.domotic.state.ConnectionStateType.ConnectionState;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.openbase.jul.schedule.SyncObject;
import org.openbase.jul.schedule.WatchDog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import org.openbase.type.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openbase.type.domotic.state.ConnectionStateType.ConnectionState.State.*;
import static org.openbase.type.domotic.state.AvailabilityStateType.AvailabilityState.State.*;

/**
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class RSBCommunicationServiceTest {

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitRegistryData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitConfig.getDefaultInstance()));
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public RSBCommunicationServiceTest() {
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
    private RSBCommunicationService communicationService;

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
        communicationService = new RSBCommunicationServiceImpl(testData);
        communicationService.init(scope);
        communicationService.activate();

        RSBRemoteService remoteService = new RSBRemoteServiceImpl();
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
        communicationService = new RSBCommunicationServiceImpl(testData);
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

        RSBRemoteService remoteService = new RSBRemoteServiceImpl();
        remoteService.addConnectionStateObserver((source, data) -> {
            logger.info("New connection state [" + data + "]");
        });

        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start();

        String scope = "/test/reconnection";
        UnitConfig location1 = UnitConfig.newBuilder().setId("Location1").build();
        UnitRegistryData.Builder testData = UnitRegistryData.getDefaultInstance().toBuilder().addLocationUnitConfig(location1);
        communicationService = new RSBCommunicationServiceImpl(testData);
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

        RSBRemoteService remoteService = new RSBRemoteServiceImpl();
        remoteService.init(scope);
        communicationService = new RSBCommunicationServiceImpl(testData);
        communicationService.init(scope);

        remoteService.activate();

        CompletableFuture dataFuture = remoteService.getDataFuture();

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

        RSBRemoteService remoteService = new RSBRemoteServiceImpl();
        remoteService.init(scope);
        communicationService = new RSBCommunicationServiceImpl(testData);
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

        RSBRemoteService remoteService1 = new RSBRemoteServiceImpl();
        RSBRemoteService remoteService2 = new RSBRemoteServiceImpl();
        remoteService1.init(scope);
        remoteService2.init(scope);
        communicationService = new RSBCommunicationServiceImpl(testData);
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

    /**
     * @throws Exception
     */
    @Test(timeout = 10000)
    public void testNotification() throws Exception {
        System.out.println("testNotification");

        String scope = "/test/notification";
        UnitConfig location = UnitConfig.newBuilder().setId("id").build();
        communicationService = new RSBCommunicationServiceImpl(UnitRegistryData.getDefaultInstance().toBuilder().addLocationUnitConfig(location));
        communicationService.init(scope);

        RSBRemoteService remoteService = new RSBRemoteServiceImpl();
        remoteService.init(scope);
        remoteService.activate();

        GlobalCachedExecutorService.submit(new NotificationCallable(communicationService));

        remoteService.waitForData();
        try {
            remoteService.ping().get(500, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            Assert.fail("Even though wait for data returned the pinging immediatly afterwards failed");
        }

        communicationService.deactivate();
        remoteService.deactivate();
    }

//    /**
//     *
//     * @throws Exception
//     */
//    @Test(timeout = 10000)
//    public void testReinit() throws Exception {
//        System.out.println("testReinit");
//
//        final int TEST_PARALLEL_REINIT_TASKS = 5;
//
//        String scope = "/test/notification";
//        UnitConfig location = UnitConfig.newBuilder().setId("id").build();
//        communicationService = new RSBCommunicationServiceImpl(UnitRegistryData.getDefaultInstance().toBuilder().addLocationUnitConfig(location));
//        communicationService.init(scope);
//        communicationService.activate();
//
//        RSBRemoteService remoteService = new RSBRemoteServiceImpl();
//        remoteService.init(scope);
//        remoteService.activate();
//
//        final Runnable reinitTask = () -> {
//            try {
//                remoteService.reinit();
//                remoteService.requestData().get();
//            } catch (Exception ex) {
//                ExceptionPrinter.printHistory("reinit failed!", ex, logger);
//            }
//        };
//
//        
//        // execute reinits 
//        final ArrayList<Future> taskFutures = new ArrayList<Future>();
//        for (int i = 0; i < TEST_PARALLEL_REINIT_TASKS; i++) {
//            taskFutures.add(GlobalCachedExecutorService.submit(reinitTask));
//        }
//        for (Future future : taskFutures) {
//            future.get();
//        }
//        
//        remoteService.requestData().get();
//        try {
//            remoteService.ping().get(500, TimeUnit.MILLISECONDS);
//        } catch (TimeoutException ex) {
//            Assert.fail("Even though wait for data returned the pinging immediatly afterwards failed");
//        }
//        communicationService.deactivate();
//        remoteService.deactivate();
//        remoteService.reinit();
//        
//        communicationService.shutdown();
//        remoteService.shutdown();
//        remoteService.reinit();
//    }

    public static class RSBCommunicationServiceImpl extends RSBCommunicationService<UnitRegistryData, UnitRegistryData.Builder> {

        static {
            DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitRegistryData.getDefaultInstance()));
        }

        public RSBCommunicationServiceImpl(UnitRegistryData.Builder builder) throws InstantiationException {
            super(builder);
        }

        @Override
        public void registerMethods(RSBLocalServer server) throws CouldNotPerformException {
        }
    }

    public static class RSBRemoteServiceImpl extends RSBRemoteService<UnitRegistryData> {

        static {
            DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitRegistryData.getDefaultInstance()));
        }

        public RSBRemoteServiceImpl() {
            super(UnitRegistryData.class);
        }
    }

    public class NotificationCallable implements Callable<Void> {

        private final RSBCommunicationService communicationService;
        private final Object watchDogUpdateLock = new Object();

        public NotificationCallable(RSBCommunicationService communicationService) {
            this.communicationService = communicationService;
            this.communicationService.informerWatchDog.addObserver((WatchDog source, WatchDog.ServiceState data) -> {
                synchronized (watchDogUpdateLock) {
                    if (data == WatchDog.ServiceState.RUNNING) {
                        watchDogUpdateLock.notifyAll();
                    }
                }
            });
            this.communicationService.serverWatchDog.addObserver((WatchDog source, WatchDog.ServiceState data) -> {
                synchronized (watchDogUpdateLock) {
                    if (data == WatchDog.ServiceState.RUNNING) {
                        watchDogUpdateLock.notifyAll();
                    }
                }
            });
        }

        @Override
        public Void call() throws Exception {
            communicationService.activate();
            while (!stateReached()) {
                communicationService.deactivate();
                communicationService.activate();
            }
            return null;
        }

        private boolean stateReached() throws InterruptedException, CouldNotPerformException {
            synchronized (watchDogUpdateLock) {
                while (true) {
                    watchDogUpdateLock.wait();
                    if (communicationService.informer.isActive() && communicationService.server.isActive()) {
                        communicationService.notifyChange();
                        return true;
                    } else if (communicationService.server.isActive()) {
                        return false;
                    }
                }
            }
        }
    }
}
