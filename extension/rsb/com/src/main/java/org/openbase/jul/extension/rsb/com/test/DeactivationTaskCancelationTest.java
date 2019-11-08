package org.openbase.jul.extension.rsb.com.test;

import rsb.Factory;
import rsb.RSBException;
import rsb.patterns.RemoteServer;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class DeactivationTaskCancelationTest {

    public static void main(String[] args) {
        RemoteServer remote = Factory.getInstance().createRemoteServer("/test/scope");

        System.out.println("activate");
        try {
            remote.activate();
        } catch (RSBException e) {
            e.printStackTrace();
        }

        System.out.println("trigger server task");
        new Thread(() -> {
            try {
                remote.callAsync("mymethod").get();
            } catch (RSBException | ExecutionException | InterruptedException | CancellationException e) {
                e.printStackTrace();
            }
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("deactivate");
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
