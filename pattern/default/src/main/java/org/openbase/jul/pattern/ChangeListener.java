package org.openbase.jul.pattern;

/*
 * #%L
 * JUL Pattern Default
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 * Class offers the ChangeListener pattern. Where this interface describes the listener part.
 */
public interface ChangeListener {

    /**
     * Informs the {@code ChangeListener} about an state change.
     *
     * @throws CouldNotPerformException can be thrown if the notification did not reach the target.
     * @throws InterruptedException should only be thrown if the thread was externally interrupted.
     */
    void notifyChange() throws CouldNotPerformException, InterruptedException;
}
