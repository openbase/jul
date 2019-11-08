package org.openbase.jul.extension.rsb.com;

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
    }
}
