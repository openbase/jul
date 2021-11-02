package org.openbase.jul.communication.controller;

/*-
 * #%L
 * JUL Extension Controller
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import com.google.protobuf.Any;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.communication.config.CommunicatorConfig;
import org.openbase.jul.communication.iface.CommunicatorFactory;
import org.openbase.jul.communication.iface.RPCClient;
import org.openbase.jul.communication.iface.RPCServer;
import org.openbase.jul.communication.mqtt.CommunicatorFactoryImpl;
import org.openbase.jul.communication.mqtt.DefaultCommunicatorConfig;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.type.processing.ScopeProcessor;
import org.openbase.jul.iface.Requestable;
import org.openbase.type.communication.EventType.Event;
import org.openbase.type.communication.ScopeType.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class FutureCancelTest implements Requestable<Object> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private RPCServer server;
    private RPCClient client;

    public FutureCancelTest() {
    }

    @BeforeClass
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
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

    /**
     * This test shows that the method executed by the local server does not
     * get interrupted through canceling the future.
     *
     * @throws Exception
     */
    @Test
    public void testFutureCancellation() throws Exception {
        System.out.println("TestFutureCancellation");

        final CommunicatorFactory factory = CommunicatorFactoryImpl.Companion.getInstance();
        final CommunicatorConfig defaultCommunicatorConfig = DefaultCommunicatorConfig.Companion.getInstance();


        Scope scope = ScopeProcessor.generateScope("/test/futureCancel");

        server = factory.createRPCServer(scope, defaultCommunicatorConfig);
        client = factory.createRPCClient(scope, defaultCommunicatorConfig);

        // register rpc methods.
        server.registerMethods(Requestable.class, this);

        server.activate();
        client.activate();

        Future<Any> future = client.callMethod("requestStatus", Any.class);
        try {
            future.get(1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            System.out.println("Future cancelled: " + future.cancel(true));
            Thread.sleep(1000);
        }
    }
}
