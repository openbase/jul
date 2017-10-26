package org.openbase.jul.storage.registry;

/*-
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class AbstractRegistryTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractRegistryTest.class);

    public AbstractRegistryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
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
        assertTrue("MainRegistry should be locked", mainRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentRegistry should be locked", dependentRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentFromAllRegistry should be locked", !dependentFromAllRegistry.isWriteLockedByCurrentThread());

        Thread lockThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    System.out.println("Locking DependentFromAll...");
                    dependentFromAllRegistry.lock();
                    System.out.println("DependentFromAll locked");
                    assertTrue("MainRegistry should be locked", mainRegistry.isWriteLockedByCurrentThread());
                    assertTrue("DependentRegistry should be locked", dependentRegistry.isWriteLockedByCurrentThread());
                    assertTrue("DependentFromAllRegistry should be locked", dependentFromAllRegistry.isWriteLockedByCurrentThread());

                    System.out.println("Unlocking DependentFromAll...");
                    dependentFromAllRegistry.unlock();
                    System.out.println("DependentFromAll unlocked");
                    assertTrue("MainRegistry should be unlocked", !mainRegistry.isWriteLockedByCurrentThread());
                    assertTrue("DependentRegistry should be unlocked", !dependentRegistry.isWriteLockedByCurrentThread());
                    assertTrue("DependentFromAllRegistry should be unlocked", !dependentFromAllRegistry.isWriteLockedByCurrentThread());
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistoryAndExit("Exception while locking", ex, logger);
                }
            }
        });
        lockThread.start();

        System.out.println("Unlocking Dependent...");
        dependentRegistry.unlock();
        System.out.println("Dependent unlocked");
        assertTrue("MainRegistry should be unlocked", !mainRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentRegistry should be unlocked", !dependentRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentFromAllRegistry should be unlocked", !dependentFromAllRegistry.isWriteLockedByCurrentThread());

        System.out.println("Locking main...");
        mainRegistry.lock();
        System.out.println("Main locked");
        assertTrue("MainRegistry should be locked", mainRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentRegistry should be unlocked", !dependentRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentFromAllRegistry should be unlocked", !dependentFromAllRegistry.isWriteLockedByCurrentThread());

        System.out.println("Unlocking Main...");
        mainRegistry.unlock();
        System.out.println("Main unlocked");
        assertTrue("MainRegistry should be unlocked", !mainRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentRegistry should be unlocked", !dependentRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentFromAllRegistry should be unlocked", !dependentFromAllRegistry.isWriteLockedByCurrentThread());

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
