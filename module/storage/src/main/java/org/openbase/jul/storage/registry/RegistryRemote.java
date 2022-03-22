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
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.pattern.controller.Remote;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <M>
 */
public interface RegistryRemote<M> extends Remote<M>, RegistryService {

    /**
     * Method initializes the remote with the default registry connection scope.
     *
     * @throws InitializationException {@inheritDoc}
     * @throws java.lang.InterruptedException {@inheritDoc}
     */
    void init() throws InitializationException, InterruptedException;
    
    /**
     * Reinitialize the remote with the default registry connection scope.
     * This is only possible for the maintainer of this remote.
     * 
     * @param maintainer the current maintainer of the remote
     * @throws InterruptedException thrown if the process is interrupted
     * @throws CouldNotPerformException thrown if the process could not be performed
     * @throws VerificationFailedException thrown if the given maintainer is incorrect
     */
    void reinit(final Object maintainer) throws InterruptedException, CouldNotPerformException, VerificationFailedException;

    default String getName() throws NotAvailableException {
        return getClass().getSimpleName().replace(Remote.class.getSimpleName(), "");
    }
}
