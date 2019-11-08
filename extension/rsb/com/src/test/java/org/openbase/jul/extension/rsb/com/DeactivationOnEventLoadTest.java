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

import rsb.Event;
import rsb.Factory;
import rsb.RSBException;
import rsb.config.ParticipantConfig;
import rsb.patterns.Callback;
import rsb.patterns.LocalServer;
import rsb.patterns.RemoteServer;
import rsb.transport.spread.InPushConnectorFactoryRegistry;
import rsb.transport.spread.SharedInPushConnectorFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeactivationOnEventLoadTest {

    // ----- setup test parameter ---------------------------------------
    public static final boolean EXIT_ON_ERROR               = false;
    public static final boolean STOP_PRINT_ON_ERROR         = true;
    public static final boolean ENABLE_CONNECTION_SHARING   = false;
    public static final long    SLEEP_TIME_BETWEEN_MESSES   = 10;
    public static final Level   LOG_LEVEL                   = Level.ALL;
    // ------------------------------------------------------------------

    // other static fields
    private static final String KEY_IN_PUSH_FACTORY = "shareIfPossible";
    private static final String METTHOD = "testmethod";

    public static void main(String[] args) throws InterruptedException {

        Logger.getLogger("").setLevel(LOG_LEVEL);

        // setup participant config
        int runCounter = 0;
        final ParticipantConfig participantConfig = Factory.getInstance().getDefaultParticipantConfig();
        if (ENABLE_CONNECTION_SHARING) {
            InPushConnectorFactoryRegistry.getInstance().registerFactory(KEY_IN_PUSH_FACTORY, new SharedInPushConnectorFactory());

            // instruct the spread transport to use your newly registered factory
            // for creating in push connector instances
            participantConfig.getOrCreateTransport("spread")
                    .getOptions()
                    .setProperty("transport.spread.java.infactory", KEY_IN_PUSH_FACTORY);
        }

        // start test
        mainLoop:
        while (!Thread.interrupted()) {

            // prepare run
            runCounter++;
            int serverRemotePairCount = runCounter * 10;
            final ShutdownHandler shutdownHandler = new ShutdownHandler(serverRemotePairCount);

            System.out.println("Start run " + runCounter + " with " + serverRemotePairCount * 2 + " participants...");
            for (int i = 0; i < serverRemotePairCount; i++) {
                final String id = Integer.toString(i);

                // create server
                new Thread("ServerThread-" + id) {
                    @Override
                    public void run() {
                        try {
                            LocalServer server = Factory.getInstance().createLocalServer("/test/scope/" + id, participantConfig);
                            server.addMethod("testmethod", new Callback() {
                                @Override
                                public Event internalInvoke(Event request) {
                                    return new Event(Void.class, null);
                                }
                            });
                            server.activate();
                            shutdownHandler.activeParticipants++;

                            while (!shutdownHandler.shutdown) {
                                Thread.sleep(SLEEP_TIME_BETWEEN_MESSES);
                            }

                            server.deactivate();
                            shutdownHandler.activeParticipants--;
                        } catch (RSBException | InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }.start();

                // create remote
                new Thread("RemoteThread-" + id) {

                    @Override
                    public void run() {
                        try {
                            RemoteServer remote = Factory.getInstance().createRemoteServer("/test/scope/" + id, participantConfig);

                            remote.activate();
                            shutdownHandler.activeParticipants++;

                            while (!shutdownHandler.shutdown) {
                                remote.callAsync(METTHOD, new Event(Void.class, null));
                                Thread.sleep(SLEEP_TIME_BETWEEN_MESSES);
                            }

                            remote.deactivate();
                            shutdownHandler.activeParticipants--;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }.start();
            }

            System.out.println("Wait until pool is active...");
            while (shutdownHandler.activeParticipants != serverRemotePairCount * 2) {
                Thread.yield();
            }

            // wait some additional time to increase message load
            Thread.sleep(2000);

            System.out.println("Initiate Shutdown...");
            shutdownHandler.shutdown = true;

            int lastCount = -1;
            while (shutdownHandler.activeParticipants != 0) {

                System.out.println("Shutdown progress: " + shutdownHandler.getProgress() + "% " + (shutdownHandler.activeParticipants == lastCount ? "(deactivation stuck detected)" : ""));

                if (lastCount == shutdownHandler.activeParticipants) {
                    Thread.sleep(2000);
                    if ((STOP_PRINT_ON_ERROR || EXIT_ON_ERROR) && lastCount == shutdownHandler.activeParticipants) {
                        System.err.println("Shutdown failed in run " + runCounter + " with " + serverRemotePairCount * 2 + " participants where " + shutdownHandler.activeParticipants + " could not be deactivated!");
                        break mainLoop;
                    }
                }
                lastCount = shutdownHandler.activeParticipants;
                Thread.sleep(200);
            }
            System.out.println("Shutdown progress: 100% (successful)");
        }
        System.out.println("Exit test. Probably some threads do not terminate because of the deactivation failure.");

        if (EXIT_ON_ERROR) {
            System.exit(200);
        }
    }

    static class ShutdownHandler {
        private boolean shutdown = false;
        private transient int activeParticipants = 0;
        private final int totalParticipantCount;

        public ShutdownHandler(int totalParticipantCount) {
            this.totalParticipantCount = totalParticipantCount;
        }

        public int getProgress() {
            return (100 - (int) ((activeParticipants / (totalParticipantCount * 2d)) * 100d));
        }
    }
}
