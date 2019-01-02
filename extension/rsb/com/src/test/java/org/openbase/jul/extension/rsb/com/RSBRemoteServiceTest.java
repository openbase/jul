package org.openbase.jul.extension.rsb.com;

/*-
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
import org.openbase.jul.exception.TimeoutException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.protobuf.ClosableDataBuilder;
import org.openbase.jul.extension.rsb.com.RSBCommunicationServiceTest.RSBCommunicationServiceImpl;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.extension.type.util.TransactionSynchronizationFuture;
import org.openbase.jul.pattern.Controller.ControllerAvailabilityState;
import org.openbase.jul.pattern.Remote;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.communication.TransactionValueType.TransactionValue;
import org.openbase.type.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import org.openbase.type.domotic.state.PowerStateType.PowerState.State;
import org.openbase.type.domotic.unit.dal.PowerSwitchDataType.PowerSwitchData;
import org.openbase.type.domotic.unit.dal.PowerSwitchDataType.PowerSwitchData.Builder;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RSBRemoteServiceTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RSBRemoteServiceTest() {
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

    /**
     * Test of waitForConnectionState method, of class RSBRemoteService.
     *
     * @throws java.lang.InterruptedException
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    @Test(timeout = 10000)
    public void testWaitForConnectionState() throws InterruptedException, CouldNotPerformException {
        System.out.println("waitForConnectionState");
        RSBRemoteService instance = new RSBCommunicationServiceTest.RSBRemoteServiceImpl();
        instance.init("/test/waitForConnectionState");

        // Test Timeout
        instance.activate();

        try {
            instance.waitForConnectionState(Remote.ConnectionState.CONNECTED, 10);
            Assert.fail("No exception thrown.");
        } catch (TimeoutException ex) {
            // should be thrown...
            Assert.assertTrue(true);
        }

        // Test if shutdown is blocked by waitForConnection without timeout
        System.out.println("Test if waitForConnection is interrupted through shutdown!");
        GlobalCachedExecutorService.submit(() -> {
            try {
                System.out.println("Thread is running");
                assertTrue("Instance is not active while waiting", instance.isActive());
                System.out.println("Wait for ConnectionState");
                instance.waitForConnectionState(Remote.ConnectionState.CONNECTED);
            } catch (CouldNotPerformException | InterruptedException ex) {
//                    ExceptionPrinter.printHistoryAndReturnThrowable(ex, logger);
            }
            return null;
        });

        Thread.sleep(100);

        instance.shutdown();
    }

    @Test(timeout = 5000)
    public void testDeactivation() throws InterruptedException, CouldNotPerformException {
        System.out.println("testDeactivation");

        RSBRemoteService instance = new RSBCommunicationServiceTest.RSBRemoteServiceImpl();
        instance.init("/test/testDeactivation");
        instance.activate();

        RSBCommunicationServiceTest.RSBCommunicationServiceImpl communicationService = new RSBCommunicationServiceImpl(UnitRegistryData.newBuilder());
        communicationService.init("/test/testDeactivation");
        communicationService.activate();
        communicationService.waitForAvailabilityState(ControllerAvailabilityState.ONLINE);
        instance.waitForConnectionState(ConnectionState.CONNECTED);
        instance.waitForData();
        System.out.println("shutdown...");
        System.out.println("main thread name: " + Thread.currentThread().getName());
        communicationService.deactivate();
        instance.deactivate();
        communicationService.shutdown();
        instance.shutdown();
    }

    /**
     * Test what happens when one thread calls an asynchronous method while another reinitializes
     * the remote services and requests new data afterwards.
     * This is a simple example for issue https://github.com/openbase/bco.registry/issues/59,
     *
     * @throws Exception
     */
    @Test(timeout = 5000)
    public void testReinit() throws Exception {
        System.out.println("testReinit");

        final RSBRemoteService remoteService = new RSBCommunicationServiceTest.RSBRemoteServiceImpl();
        remoteService.init("/test/testReinit");
        remoteService.activate();

        GlobalCachedExecutorService.submit(() -> {
            try {
                remoteService.callMethodAsync("method").get();
            } catch (CouldNotPerformException | InterruptedException | ExecutionException ex) {
                // is expected since reinit should kill the method call
            }
        });

        Thread.sleep(100);

        remoteService.reinit();
        try {
            remoteService.requestData().get(100, TimeUnit.MILLISECONDS);
        } catch (CancellationException ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Requesting data has been cancelled", ex), logger);
        } catch (java.util.concurrent.TimeoutException ex) {
            // is expected here since no server is started
        }

        remoteService.shutdown();
    }

    private boolean prioritizedObservableFinished = false;

    /**
     * Test for the synchronization using transaction ids. This tests verifies if the {@link TransactionSynchronizationFuture}
     * can only return after the internal prioritized observable of the remote service has finished its notification.
     * <p>
     * This is needed e.g. for registry remotes because they synchronize their internal remote registries using this
     * observable. If it is not finished when the future returns following calls can fail.
     * See issue: <a href="https://github.com/openbase/bco.registry/issues/98">https://github.com/openbase/bco.registry/issues/98</a>
     *
     * @throws Exception if an error occurs.
     */
    @Test(timeout = 5000)
    public void testTransactionSynchronization() throws Exception {
        final String scope = "/test/transaction/sync";

        final TransactionCommunicationService communicationService = new TransactionCommunicationService();
        communicationService.init(scope);
        communicationService.activate();

        final TransactionRemoteService remoteService = new TransactionRemoteService();
        remoteService.init(scope);
        remoteService.activate();
        remoteService.waitForData();

        long transactionId = remoteService.getTransactionId();
        remoteService.getInternalPrioritizedDataObservable().addObserver((source, data) -> {
            Thread.sleep(100);
            prioritizedObservableFinished = true;
        });
        remoteService.performTransaction().get();
        assertTrue("Transaction id did not increase after performTransaction call", remoteService.getTransactionId() > transactionId);
        assertTrue("Prioritized observable is not finished but sync future already returned", prioritizedObservableFinished);

        remoteService.shutdown();
        communicationService.shutdown();
    }

    private static class TransactionCommunicationService extends RSBCommunicationService<PowerSwitchData, PowerSwitchData.Builder> {

        /**
         * Create a communication service.
         *
         * @throws InstantiationException if the creation fails
         */
        public TransactionCommunicationService() throws InstantiationException {
            super(PowerSwitchData.newBuilder());
        }

        @Override
        public void registerMethods(RSBLocalServer server) throws CouldNotPerformException {
            try {
                RPCHelper.registerMethod(this.getClass().getMethod("performTransaction"), this, server);
            } catch (NoSuchMethodException ex) {
                throw new CouldNotPerformException("Could not register method[performTransaction]", ex);
            }
        }

        public TransactionValue performTransaction() throws CouldNotPerformException {
            // update transaction
            updateTransactionId();
            // change data builder to trigger notification
            try (ClosableDataBuilder<Builder> dataBuilder = getDataBuilder(this)) {
                dataBuilder.getInternalBuilder().getPowerStateBuilder().setValue(State.ON);
            }
            // return transaction value
            return TransactionValue.newBuilder().setTransactionId(getTransactionId()).build();
        }
    }

    private static class TransactionRemoteService extends RSBRemoteService<PowerSwitchData> {

        public TransactionRemoteService() {
            super(PowerSwitchData.class);
        }

        public Future<TransactionValue> performTransaction() throws CouldNotPerformException {
            return new TransactionSynchronizationFuture<>(RPCHelper.callRemoteMethod(this, TransactionValue.class), this);
        }
    }
}
