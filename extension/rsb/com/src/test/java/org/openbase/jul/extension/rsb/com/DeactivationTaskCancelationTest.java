package org.openbase.jul.extension.rsb.com;

/*-
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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
