package org.openbase.jul.extension.rsb.com;

/*-
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.extension.rsb.iface.RSBRemoteServer;
import org.openbase.jul.iface.Requestable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class RSBFutureCancelTest implements Requestable<Object> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private RSBLocalServer localServer;
    private RSBRemoteServer remoteServer;
    
    public RSBFutureCancelTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Override
    public Object requestStatus() throws CouldNotPerformException {
        System.out.println("RequestStatus");
        try {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Interrupted");
                    Thread.currentThread().interrupt();
                }
                System.out.println("Sleeping...");
                Thread.sleep(200);
            }
        } catch (InterruptedException ex) {
            System.out.println("Interrupted");
        } catch (CancellationException ex) {
            System.out.println("Cancelled");
        } catch (Exception ex) {
            System.out.println("Other" + ex);
        } catch (Throwable ex) {
            System.out.println("Test" + ex);
        }
        
        return null;
    }
    
    @Test
    public void testFutureCancellation() throws Exception {
        System.out.println("TestFutureCancellation");
        
        Scope scope = new Scope("/test/futureCancel");
        ParticipantConfig participantConfig = RSBSharedConnectionConfig.getParticipantConfig();
        
        localServer = RSBFactoryImpl.getInstance().createSynchronizedLocalServer(scope, participantConfig);
        remoteServer = RSBFactoryImpl.getInstance().createSynchronizedRemoteServer(scope, participantConfig);

        // register rpc methods.
        RPCHelper.registerInterface(Requestable.class, this, localServer);
        
        localServer.activate();
        remoteServer.activate();
        
        Future<Event> future = remoteServer.callAsync("requestStatus");
//        System.out.println("Before sleep");
//        Thread.sleep(2000);
//        System.out.println("After sleep");

        try {
            future.get(1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            System.out.println("Future cancelled: " + future.cancel(true));
            Thread.sleep(1000);
        }
    }
    
}
