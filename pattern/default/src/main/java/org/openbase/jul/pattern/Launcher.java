package org.openbase.jul.pattern;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.iface.Launchable;

import java.util.concurrent.Future;

/*
 * #%L
 * JUL Pattern Default
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
 * @param <L> the launchable to launch by this launcher.
 */
public interface Launcher<L extends Launchable> {

    /**
     * starts the launchable.
     */
    Future<Void> launch();

    /**
     * Restarts the launchable.
     *
     * @throws CouldNotPerformException
     * @throws java.lang.InterruptedException
     */
    void relaunch() throws CouldNotPerformException, InterruptedException;

    /**
     * Stops the launchable.
     */
    void stop();
    
    /**
     * Get the uptime of the launchable.
     *
     * @return time in milliseconds.,
     */
    long getUpTime();
    
    /**
     * Get the uptime of the launchable.
     *
     * @return time in milliseconds.,
     */
    long getLaunchTime();
    
    /**
     * Flag is set if the application was successfully verified after launching.
     * In case the verification has failed, may some application functions are restricted.
     * @return Returns true if the application verification was successful after launching.
     */
    boolean isVerified();

}
