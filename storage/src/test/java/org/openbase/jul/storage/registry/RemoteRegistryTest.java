package org.openbase.jul.storage.registry;

/*-
 * #%L
 * JUL Storage
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RemoteRegistryTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RemoteRegistryTest.class);

    private static RemoteRegistry remoteRegistry;

    public RemoteRegistryTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Throwable {
        try {
            JPService.setupJUnitTestMode();
            remoteRegistry = new RemoteRegistry();
        } catch (Throwable ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(ex, LOGGER);
        }
    }

    @AfterAll
    public static void tearDownClass() throws Throwable {
        try {
            remoteRegistry.shutdown();
        } catch (Throwable ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(ex, LOGGER);
        }
    }

    /**
     * Test of notifyRegistryUpdate method, of class RemoteRegistry.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testNotifyRegistryUpdate() throws Exception {
        System.out.println("notifyRegistryUpdate");
        remoteRegistry.notifyRegistryUpdate(new ArrayList());
    }
}
