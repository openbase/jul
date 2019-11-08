package org.openbase.jul.extension.rsb.com.test;

import rsb.Event;
import rsb.Factory;
import rsb.RSBException;
import rsb.patterns.Callback;
import rsb.patterns.LocalServer;
import rsb.patterns.RemoteServer;

public class DeactivationWithoutSpread {

    public static void main(String[] args) {
        LocalServer server = Factory.getInstance().createLocalServer("/test/scope");
        RemoteServer remote = Factory.getInstance().createRemoteServer("/test/scope");

        try {
            server.addMethod("mymethod", new Callback() {
                @Override
                public Event internalInvoke(Event request) throws UserCodeException {
                    System.out.println("process task");
                    try {
                        while(!Thread.interrupted()) {
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
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
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("deactivate");
        try {
            if (server.isActive()) {
                server.deactivate();
            }
        } catch (RSBException | InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (remote.isActive()) {
                remote.deactivate();
            }
        } catch (RSBException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
