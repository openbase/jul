package org.openbase.jul.iface

import org.openbase.jul.exception.CouldNotPerformException

/*
 * #%L
 * JUL Interface
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
 *
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 * This interface can be implemented by any applications which should be launchable by a Launcher instance.
 * @param <CONFIG> the configuration type of this launchable.
 *
 */
interface Launchable<CONFIG> : Manageable<CONFIG>, DefaultInitializableImpl<CONFIG> {
    /**
     * Method starts the referred application.
     *
     * @return returns true if the application was successfully started in case the application was started with restrictions false will be returned.
     * @throws CouldNotPerformException Is thrown in case any error occurs during the startup phase.
     * @throws InterruptedException is thrown in case the thread was externally interrupted.
     */
    @Throws(CouldNotPerformException::class, InterruptedException::class)
    @JvmDefault
    fun launch(): Boolean {
        try {
            init()
            activate()
        } catch (ex: CouldNotPerformException) {
            throw CouldNotPerformException("Could not launch $this", ex)
        }
        return true
    }
}
