package org.openbase.jul.pattern.trigger

import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.InstantiationException
import org.openbase.jul.exception.NotAvailableException
import org.openbase.jul.exception.printer.ExceptionPrinter
import org.openbase.jul.extension.type.processing.TimestampProcessor
import org.openbase.jul.iface.Shutdownable
import org.openbase.jul.pattern.ObservableImpl
import org.openbase.jul.pattern.Observer
import org.openbase.type.domotic.state.ActivationStateType
import org.slf4j.LoggerFactory

/*-
 * #%L
 * JUL Pattern Trigger
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
 */ /**
 *
 * @author [Timo Michalski](mailto:tmichalski@techfak.uni-bielefeld.de)
 */
abstract class AbstractTrigger : Shutdownable, Trigger {
    private val triggerObservable: ObservableImpl<Trigger, ActivationStateType.ActivationState>

    init {
        triggerObservable = ObservableImpl(this)
        try {
            triggerObservable.notifyObservers(
                TimestampProcessor.updateTimestampWithCurrentTime(
                    ActivationStateType.ActivationState.newBuilder().setValue(
                        ActivationStateType.ActivationState.State.UNKNOWN
                    ).build()
                )
            )
        } catch (ex: CouldNotPerformException) {
            throw InstantiationException("Could not set initial state", ex)
        }
    }

    @get:Throws(NotAvailableException::class)
    override val activationState: ActivationStateType.ActivationState
        get() = triggerObservable.value

    override fun addObserver(observer: Observer<Trigger, ActivationStateType.ActivationState>?) {
        triggerObservable.addObserver(observer)
    }

    override fun removeObserver(observer: Observer<Trigger, ActivationStateType.ActivationState>?) {
        triggerObservable.removeObserver(observer)
    }

    @Throws(CouldNotPerformException::class)
    protected fun notifyChange(newState: ActivationStateType.ActivationState) {
        triggerObservable.notifyObservers(newState)
    }

    override fun shutdown() {
        triggerObservable.shutdown()
        try {
            deactivate()
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (ex: CouldNotPerformException) {
            ExceptionPrinter.printHistory("Could not shutdown $this", ex, LoggerFactory.getLogger(javaClass))
        }
    }
}
