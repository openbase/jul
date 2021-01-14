package org.openbase.jul.extension.rsb.com;

/*-
 * #%L
 * JUL Extension RSB Communication
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

import rsb.*;
import rsb.patterns.RemoteServer;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class RSBTest {
    public static void main(String[] args) {
//        LocalServer server = Factory.getInstance().createLocalServer("/preset/scope");
        RemoteServer remote = Factory.getInstance().createRemoteServer("/preset/scope");

//        try {
//            server.addMethod("mymethod", new Callback() {
//                @Override
//                public Event internalInvoke(Event request) throws UserCodeException {
//                    System.out.println("process task");
//                    try {
//                        while(true) {
//                            Thread.sleep(100);
//                        }
//                    } catch (InterruptedException ex) {
//                        Thread.currentThread().interrupt();
//                        System.out.println("interrupt task");
////                        throw new UserCodeException(ex);
//                    }
//                    return new Event(Void.class);
//                }
//            });
//        } catch (RSBException e) {
//            e.printStackTrace();
//        }

        System.out.println("activate");
//        try {
//            server.activate();
//        } catch (RSBException e) {
//            e.printStackTrace();
//        }
        try {
            remote.activate();
        } catch (RSBException e) {
            e.printStackTrace();
        }

        System.out.println("trigger server tast");
        new Thread(() -> {
            try {
                remote.callAsync("mymethod").get();
            } catch (RSBException | ExecutionException | InterruptedException | CancellationException e) {
                e.printStackTrace();
            }
        }).start();

//        System.out.println("kill spread now to reproduce the bug.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("deactivate");
//        try {
//            if (server.isActive()) {
//                server.deactivate();
//            }
//        } catch (RSBException | InterruptedException e) {
//            e.printStackTrace();
//        }
        try {
            if (remote.isActive()) {
                remote.deactivate();
            }
        } catch (RSBException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("done");
    }
}
