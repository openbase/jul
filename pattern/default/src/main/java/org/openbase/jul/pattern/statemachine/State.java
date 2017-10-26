package org.openbase.jul.pattern.statemachine;

/*-
 * #%L
 * JUL Pattern Default
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
import java.util.concurrent.Callable;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 * Represents and executes a state of a task.
 *
 * @author malinke
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface State extends Callable<Class<? extends State>> {

    /**
     * Executes the task in this state.
     *
     * Example:
     * {@code
     *      WaitTenSeconds implements State {
     *          public Class&lt;State&gt; call() {
     *              try {
     *                  Thread.sleep(10000);
     *              } catch (InterruptedException ex) {
     *                  // handle interruption as shutdown
     *                  return FinalState.class;
     *              }
     *              return NextStateToProcessClass.class;
     *          }
     *      }
     * }
     *
     * @return the next state to execute
     * @throws CouldNotPerformException is thrown if the state execution has failed.
     */
    @Override
    Class<? extends State> call() throws CouldNotPerformException;
}
