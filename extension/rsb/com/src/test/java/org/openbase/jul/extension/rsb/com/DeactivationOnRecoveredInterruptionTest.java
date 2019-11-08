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
import rsb.patterns.Callback;
import rsb.patterns.LocalServer;
import rsb.patterns.RemoteServer;

public class DeactivationOnRecoveredInterruptionTest {
    public static void main(String[] args) {
        LocalServer server = Factory.getInstance().createLocalServer("/test/scope");
        RemoteServer remote = Factory.getInstance().createRemoteServer("/test/scope");

        try {
            server.addMethod("mymethod", new Callback() {
                @Override
                public Event internalInvoke(Event request) throws UserCodeException {
                    System.out.println("process task");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        // Thread.currentThread().interrupt();
                        System.out.println("interrupt task");
                        throw new UserCodeException(ex);
                    }
                    return request;
                }
            });
        } catch (RSBException e) {
            e.printStackTrace();
        }

        System.out.println("activate");
        try {
            server.activate();
        } catch (RSBException e) {
            e.printStackTrace();
        }
        try {
            remote.activate();
        } catch (RSBException e) {
            e.printStackTrace();
        }

        System.out.println("trigger server task");
        try {
            remote.callAsync("mymethod");
        } catch (RSBException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("deactivate");
        try {
            server.deactivate();
        } catch (RSBException | InterruptedException e) {
            e.printStackTrace();
        }
        try {
            remote.deactivate();
        } catch (RSBException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("deactivate finished");
    }
}
