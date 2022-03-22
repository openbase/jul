package org.openbase.jul.iface;

/*-
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
import org.openbase.jul.exception.FatalImplementationErrorException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Interface is used to process any data of type {@code <I>} into data of type {@code <O>} while the processing is limited in time.
 *
 * @param <I> Input type needed for processing.
 * @param <O> Output type defining the process result.
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface TimedProcessable<I, O> extends Processable<I, O> {

    /**
     * Using Long.MAX_VALUE as infinity timeout is not practical because in any calculations using this timeout like adding +1 causes a value overrun.
     * Therefore, this constant is introduced to use a infinity timeout which represents in fact 3170 years which should covers at least some human generations ;)
     *
     * The unit of the {@code INFINITY_TIMEOUT} is in milliseconds.
     */
    long INFINITY_TIMEOUT = 100000000000000L;

    /**
     * @param input    the input data to process.
     * @param timeout  the timeout of the processing.
     * @param timeUnit the timeunit of the timeout.
     *
     * @return the result.
     *
     * @throws CouldNotPerformException is thrown if the processing failed.
     * @throws InterruptedException     is thrown if the thread was externally interrupted.
     * @throws TimeoutException         in thrown if the timeout was reached and the task is still not done.
     */
    O process(final I input, final long timeout, final TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException, TimeoutException;

    @Override
    default O process(final I input) throws CouldNotPerformException, InterruptedException {
        try {
            return process(input, INFINITY_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new FatalImplementationErrorException(this, e);
        }
    }
}
