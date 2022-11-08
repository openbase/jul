package org.openbase.jul.storage.registry;

/*-
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.RejectedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:pLeminoq@openbase.org">Tamino Huxohl</a>
 */
public class RegistryLockingTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RegistryLockingTest.class);

    public RegistryLockingTest() {
    }

    @BeforeAll
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    @Test
    @Timeout(5)
    public void registryLockingTest() throws Exception {
        System.out.println("registryLockingTest");
        final AbstractRegistry registry1 = new AbstractRegistryImpl();
        final AbstractRegistry registry2 = new AbstractRegistryImpl();
        final AbstractRegistry registry3 = new AbstractRegistryImpl();
        final AbstractRegistry registry4 = new AbstractRegistryImpl();

        final RemoteRegistry remoteRegistry1 = new RemoteRegistry(new HashMap());
        final RemoteRegistry remoteRegistry2 = new RemoteRegistry(new HashMap());

        registry1.setName("Registry 1");
        registry2.setName("Registry 2");
        registry3.setName("Registry 3");
        registry4.setName("Registry 4");

        remoteRegistry1.setName("RemoteRegistry 1");
        remoteRegistry2.setName("RemoteRegistry 2");

        registry1.registerDependency(remoteRegistry1);
        registry2.registerDependency(registry1);
        registry3.registerDependency(registry2);
        registry3.registerDependency(remoteRegistry2);
        registry4.registerDependency(registry2);
        registry4.registerDependency(registry3);

        remoteRegistry1.lock();
        remoteRegistry1.unlock();

        registry1.lock();

        Thread testingThread = new Thread(() -> {
            try {
                assertFalse(registry1.tryLockRegistry(), "Registry1 still lockable");
                assertTrue(registry2.tryLockRegistry(), "Registry2 still lockable");
                assertTrue(registry3.tryLockRegistry(), "Registry3 still lockable");
                assertTrue(registry4.tryLockRegistry(), "Registry4 still lockable");
                assertFalse(remoteRegistry1.internalTryLockRegistry(), "RemoteRegistry1 still lockable");
                assertTrue(remoteRegistry2.internalTryLockRegistry(), "RemoteRegistry2 still lockable");
                registry2.unlockRegistry();
                registry3.unlockRegistry();
                registry4.unlockRegistry();
                remoteRegistry2.internalUnlockRegistry();
            } catch (RejectedException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER);
                assertTrue(false);
            }
        });

        registry2.lock();
        registry2.unlock();

        testingThread.start();
        testingThread.join();

        registry1.unlock();

        testingThread = new Thread(() -> {
            try {
                assertTrue(registry1.tryLockRegistry());
                assertTrue(registry2.tryLockRegistry());
                assertTrue(registry3.tryLockRegistry());
                assertTrue(registry4.tryLockRegistry());
                assertTrue(remoteRegistry1.internalTryLockRegistry());
                assertTrue(remoteRegistry2.internalTryLockRegistry());
                registry1.unlockRegistry();
                registry2.unlockRegistry();
                registry3.unlockRegistry();
                registry4.unlockRegistry();
                remoteRegistry1.internalUnlockRegistry();
                remoteRegistry2.internalUnlockRegistry();
            } catch (RejectedException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER);
                assertTrue(false);
            }
        });

        testingThread.start();
        testingThread.join();

        registry1.lock();
        registry1.lock();
        registry1.unlock();
        registry1.unlock();


    }

    public class AbstractRegistryImpl extends AbstractRegistry {

        public AbstractRegistryImpl() throws org.openbase.jul.exception.InstantiationException {
            this(new HashMap());
        }

        public AbstractRegistryImpl(Map entryMap) throws org.openbase.jul.exception.InstantiationException {
            super(entryMap);
        }
    }
}
