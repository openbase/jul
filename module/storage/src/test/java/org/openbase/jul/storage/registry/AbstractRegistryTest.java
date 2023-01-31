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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class AbstractRegistryTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractRegistryTest.class);

    public AbstractRegistryTest() {
    }

    @BeforeAll
    @Timeout(30)
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    @Test
    @Timeout(5)
    public void testDependentRegistryLocking() throws Exception {
        AbstractRegistry mainRegistry = new AbstractRegistryImpl();
        AbstractRegistry dependentRegistry = new AbstractRegistryImpl();
        AbstractRegistry dependentFromAllRegistry = new AbstractRegistryImpl();

        mainRegistry.setName("MainRegistry");
        dependentRegistry.setName("DependentRegistry");
        dependentFromAllRegistry.setName("DependentFromAllRegistry");

        dependentRegistry.registerDependency(mainRegistry);

        dependentFromAllRegistry.registerDependency(mainRegistry);
        dependentFromAllRegistry.registerDependency(dependentRegistry);

        System.out.println("Locking Dependent...");
        dependentRegistry.lock();
        System.out.println("Dependent locked");
        assertTrue(
                mainRegistry.isWriteLockedByCurrentThread(),
                "MainRegistry should be locked");
        assertTrue(
                dependentRegistry.isWriteLockedByCurrentThread(),
                "DependentRegistry should be locked");
        assertFalse(
                dependentFromAllRegistry.isWriteLockedByCurrentThread(),
                "DependentFromAllRegistry should be locked");

        Thread lockThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    System.out.println("Locking DependentFromAll...");
                    dependentFromAllRegistry.lock();
                    System.out.println("DependentFromAll locked");
                    assertTrue(
                            mainRegistry.isWriteLockedByCurrentThread(),
                            "MainRegistry should be locked");
                    assertTrue(
                            dependentRegistry.isWriteLockedByCurrentThread(),
                            "DependentRegistry should be locked");
                    assertTrue(
                            dependentFromAllRegistry.isWriteLockedByCurrentThread(),
                            "DependentFromAllRegistry should be locked");

                    System.out.println("Unlocking DependentFromAll...");
                    dependentFromAllRegistry.unlock();
                    System.out.println("DependentFromAll unlocked");
                    assertFalse(mainRegistry.isWriteLockedByCurrentThread(), "MainRegistry should be unlocked");
                    assertFalse(dependentRegistry.isWriteLockedByCurrentThread(), "DependentRegistry should be unlocked");
                    assertFalse(dependentFromAllRegistry.isWriteLockedByCurrentThread(), "DependentFromAllRegistry should be unlocked");
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistoryAndExit("Exception while locking", ex, logger);
                }
            }
        });
        lockThread.start();

        System.out.println("Unlocking Dependent...");
        dependentRegistry.unlock();
        System.out.println("Dependent unlocked");
        assertFalse(mainRegistry.isWriteLockedByCurrentThread(), "MainRegistry should be unlocked");
        assertFalse(dependentRegistry.isWriteLockedByCurrentThread(), "DependentRegistry should be unlocked");
        assertFalse(dependentFromAllRegistry.isWriteLockedByCurrentThread(), "DependentFromAllRegistry should be unlocked");

        System.out.println("Locking main...");
        mainRegistry.lock();
        System.out.println("Main locked");
        assertTrue(mainRegistry.isWriteLockedByCurrentThread(), "MainRegistry should be locked");
        assertFalse(dependentRegistry.isWriteLockedByCurrentThread(), "DependentRegistry should be unlocked");
        assertFalse(dependentFromAllRegistry.isWriteLockedByCurrentThread(), "DependentFromAllRegistry should be unlocked");

        System.out.println("Unlocking Main...");
        mainRegistry.unlock();
        System.out.println("Main unlocked");
        assertFalse(mainRegistry.isWriteLockedByCurrentThread(), "MainRegistry should be unlocked");
        assertFalse(dependentRegistry.isWriteLockedByCurrentThread(), "DependentRegistry should be unlocked");
        assertFalse(dependentFromAllRegistry.isWriteLockedByCurrentThread(), "DependentFromAllRegistry should be unlocked");

    }

    public class AbstractRegistryImpl extends AbstractRegistry {

        public AbstractRegistryImpl() throws InstantiationException {
            this(new HashMap());
        }

        public AbstractRegistryImpl(Map entryMap) throws InstantiationException {
            super(entryMap);
        }
    }

}
