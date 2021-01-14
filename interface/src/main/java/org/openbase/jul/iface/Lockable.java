package org.openbase.jul.iface;

/*-
 * #%L
 * JUL Interface
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.VerificationFailedException;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface Lockable {

    /**
     * Method verifies if this instance can be maintained.
     *
     * @throws VerificationFailedException is thrown if the instance is currently maintained by another instance.
     */
    void verifyMaintainability() throws VerificationFailedException;

    /**
     * This method allows to lock this instance to make sure no other instances can maintain these one.
     * This could for example be useful if the management of this instance should be restricted to an instance pool.
     *
     * Note: After a successfully lock only the maintainer is able to unlock this instance.
     *
     * @param maintainer
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown if the remote could not be locked.
     */
    void lock(final Object maintainer) throws CouldNotPerformException;

    /**
     * Method checks if the this instance is currently locked by another instance.
     *
     * @return true if this instance is locked.
     */
    boolean isLocked();

    /**
     * Method unlocks this instance.
     * Only the maintainer who has previously locked this instance is able to unlock the instance by this method.
     *
     * @param maintainer the instance which currently holds the lock.
     * @throws CouldNotPerformException is thrown if the instance could not be unlocked.
     */
    void unlock(final Object maintainer) throws CouldNotPerformException;
}
