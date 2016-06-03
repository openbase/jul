package org.dc.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.extension.rsb.iface.RSBLocalServerInterface;
import org.dc.jul.pattern.Observable;
import org.dc.jul.pattern.Observer;
import org.dc.jul.pattern.Remote;
import org.dc.jul.schedule.SyncObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Factory;
import rsb.Informer;
import rsb.config.ParticipantConfig;
import rsb.config.TransportConfig;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.spatial.LocationConfigType.LocationConfig;
import rst.spatial.LocationRegistryType.LocationRegistry;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class RSBCommunicationServiceTest {

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(LocationRegistry.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(LocationConfig.getDefaultInstance()));
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

//    @Test(timeout = 20000)
    @Test
    public void testInitialSync() throws Exception {
        String scope = "/test/synchronization";
        final SyncObject waitForDataSync = new SyncObject("WaitForDataSync");
        LocationConfig location1 = LocationConfig.newBuilder().setId("Location1").build();
        LocationRegistry.Builder testData = LocationRegistry.getDefaultInstance().toBuilder().addLocationConfig(location1);
        communicationService = new RSBCommunicationServiceImpl(testData);
        communicationService.init(scope);
        communicationService.activate();

        RSBRemoteService remoteService = new RSBRemoteServiceImpl();
        remoteService.init(scope);
        remoteService.addDataObserver(new Observer<LocationRegistry>() {

            @Override
            public void update(final Observable<LocationRegistry> source, LocationRegistry data) throws Exception {
                if (data.getLocationConfigCount() == 1 && data.getLocationConfig(0).getId().equals("Location1")) {
                    firstSync = true;
                    synchronized (waitForDataSync) {
                        waitForDataSync.notifyAll();
                    }
                }
                if (data.getLocationConfigCount() == 2 && data.getLocationConfig(0).getId().equals("Location1") && data.getLocationConfig(1).getId().equals("Location2")) {
                    secondSync = true;
                    synchronized (waitForDataSync) {
                        waitForDataSync.notifyAll();
                    }
                }
            }
        });

        synchronized (waitForDataSync) {
            if (firstSync == false) {
                logger.info("Wait for data sync");
                remoteService.activate();
                waitForDataSync.wait();
            }
        }
        assertTrue("Synchronization after the start of the remote service has not been done", firstSync);

        communicationService.deactivate();
        LocationConfig location2 = LocationConfig.newBuilder().setId("Location2").build();
        testData.addLocationConfig(location2);
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

        try {
            remoteService.ping().get();
            assertTrue("Pinging was not canceled after timeout.", false);
        } catch (Exception ex) {
            // ping canceled
        }
        assertEquals("Remote is still connected after remote service shutdown!", Remote.RemoteConnectionState.CONNECTING, remoteService.getConnectionState());
        remoteService.deactivate();
        assertEquals("Remote is not disconnected after deactivation!", Remote.RemoteConnectionState.DISCONNECTED, remoteService.getConnectionState());
    }

    @Test
    public void testInProcessCommunication() throws Exception {
        ParticipantConfig config = Factory.getInstance().getDefaultParticipantConfig();
//        config = config.copy();

        for (TransportConfig transport : config.getEnabledTransports()) {
            logger.info("Disable " + transport.getName() + " communication during tests.");
            transport.setEnabled(false);
        }
        logger.info("Enable inprocess communication during tests.");
        config.getOrCreateTransport("inprocess").setEnabled(true);

        for (TransportConfig transport : config.getEnabledTransports()) {
            logger.info("Enabled: " + transport.getName());
        }
        // config modi
        Informer<Object> informer = Factory.getInstance().createInformer("/test", config);
        informer.activate();
        informer.send("TestString");
    }
    
    /**
     * Test if a RemoteService will reconnect when the communication service restarts.
     * 
     * @throws Exception 
     */
    @Test(timeout = 5000)
    public void testReconnection() throws Exception {
        String scope = "/test/reconnection";
        LocationConfig location1 = LocationConfig.newBuilder().setId("Location1").build();
        LocationRegistry.Builder testData = LocationRegistry.getDefaultInstance().toBuilder().addLocationConfig(location1);
        communicationService = new RSBCommunicationServiceImpl(testData);
        communicationService.init(scope);
        communicationService.activate();

        RSBRemoteService remoteService = new RSBRemoteServiceImpl();
        remoteService.init(scope);
        remoteService.activate();
        
        remoteService.waitForConnectionState(Remote.RemoteConnectionState.CONNECTED);

        communicationService.deactivate();
        
        remoteService.waitForConnectionState(Remote.RemoteConnectionState.CONNECTING);

        communicationService.activate();
        
        remoteService.waitForConnectionState(Remote.RemoteConnectionState.CONNECTED);

        remoteService.shutdown();
        
        remoteService.waitForConnectionState(Remote.RemoteConnectionState.DISCONNECTED);
        
        communicationService.shutdown();
    }
    
    /**
     * Test if a RemoteService will reconnect when the communication service restarts.
     * 
     * @throws Exception 
     */
//    @Test(timeout = 5000)
//    public void testReconnection() throws Exception {
//        String scope = "/test/reconnection";
//        LocationConfig location1 = LocationConfig.newBuilder().setId("Location1").build();
//        LocationRegistry.Builder testData = LocationRegistry.getDefaultInstance().toBuilder().addLocationConfig(location1);
//
//        RSBRemoteService remoteService = new RSBRemoteServiceImpl();
//        remoteService.init(scope);
//        remoteService.activate();
//        
//        communicationService = new RSBCommunicationServiceImpl(testData);
//        communicationService.init(scope);
//        communicationService.activate();
//        
//        remoteService.waitForConnectionState(Remote.RemoteConnectionState.CONNECTED);
//
//        communicationService.deactivate();
//        
//        remoteService.waitForConnectionState(Remote.RemoteConnectionState.CONNECTING);
//
//        communicationService.activate();
//        
//        remoteService.waitForConnectionState(Remote.RemoteConnectionState.CONNECTED);
//
//        remoteService.shutdown();
//        
//        remoteService.waitForConnectionState(Remote.RemoteConnectionState.DISCONNECTED);
//        
//        communicationService.shutdown();
//    }

    public class RSBCommunicationServiceImpl extends RSBCommunicationService<LocationRegistry, LocationRegistry.Builder> {

        public RSBCommunicationServiceImpl(LocationRegistry.Builder builder) throws InstantiationException {
            super(builder);
        }

        @Override
        public void registerMethods(RSBLocalServerInterface server) throws CouldNotPerformException {
        }
    }

    public class RSBRemoteServiceImpl extends RSBRemoteService<LocationRegistry> {

        public RSBRemoteServiceImpl() {
            super(LocationRegistry.class);
        }

        @Override
        public void notifyDataUpdate(LocationRegistry data) throws CouldNotPerformException {
        }
    }
}
