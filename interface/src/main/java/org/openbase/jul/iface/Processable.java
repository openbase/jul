package org.openbase.jul.iface;

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
 */

import org.openbase.jul.exception.CouldNotPerformException;

/**
 * Interface is used to process any data of type {@code <I>} into data of type {@code <O>}.
 *
 * @param <I> Input type needed for processing.
 * @param <O> Output type defining the process result.
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface Processable<I, O> {

    /**
     * @param input the input data to process.
     *
     * @return the result.
     *
     * @throws CouldNotPerformException is thrown if the processing failed.
     * @throws InterruptedException     is thrown if the thread was externally interrupted.
     */
    O process(final I input) throws CouldNotPerformException, InterruptedException;

}
