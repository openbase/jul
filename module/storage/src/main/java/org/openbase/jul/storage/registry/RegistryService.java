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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.annotation.RPCMethod;
import org.openbase.jul.exception.NotAvailableException;

import java.util.concurrent.Future;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface RegistryService {

    /**
     * This method checks if the registry is not handling any tasks and is currently consistent.
     *
     * Note: Because this check is fully synchronized with the controller instance, high repeating remote checks should be avoided.
     *
     * @return Returns and future object which delivers true if this registry is consistent and not busy.
     */
    @RPCMethod
    Boolean isReady();

    /**
     * Method blocks until the registry is not handling any tasks and is currently consistent.
     *
     * Note: If you have just modified the registry this method can maybe return immediately if the task is not yet received by the registry controller.
     * So you should prefer the futures of the modification methods for synchronization tasks.
     *
     * @throws InterruptedException is thrown in case the thread was externally interrupted.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown if the wait could not be performed.
     */
    @RPCMethod
    void waitUntilReady() throws InterruptedException, CouldNotPerformException;

    /**
     * Method blocks until the registry is not handling any tasks and is currently consistent.
     *
     * Note: If you have just modified the registry this method can maybe return immediately if the task is not yet received by the registry controller.
     * So you should prefer the futures of the modification methods for synchronization tasks.
     *
     * @return a future which is finished if the registry is ready.
     */
    Future<Void> waitUntilReadyFuture();

    /**
     * Test if all internal registries managed by this service are consistent.
     *
     * Note: If the consistency cannot be tested, true is returned.
     * @return true if all managed registries are consistent or not available and else false.
     */
    Boolean isConsistent();
}
