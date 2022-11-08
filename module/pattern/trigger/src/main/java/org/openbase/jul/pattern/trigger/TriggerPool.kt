package org.openbase.jul.pattern.trigger

import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.InstantiationException
import org.openbase.jul.exception.NotAvailableException
import org.openbase.jul.exception.printer.ExceptionPrinter
import org.openbase.jul.extension.type.processing.TimestampProcessor
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
 * @author [Timo Michalski](mailto:tmichalski@techfak.uni-bielefeld.de)
 */
class TriggerPool: AbstractTrigger() {
    enum class TriggerAggregation {
        AND, OR
    }

    private var active: Boolean
    private val triggerListAND: MutableList<Trigger>
    private val triggerListOR: MutableList<Trigger>
    private val triggerAndObserver: Observer<Trigger, ActivationStateType.ActivationState>
    private val triggerOrObserver: Observer<Trigger, ActivationStateType.ActivationState>

    init {
        triggerListAND = ArrayList()
        triggerListOR = ArrayList()
        active = false
        triggerAndObserver =
            Observer { source: Trigger?, data: ActivationStateType.ActivationState? -> verifyCondition() }
        triggerOrObserver =
            Observer { source: Trigger?, data: ActivationStateType.ActivationState? -> verifyCondition() }
        try {
            notifyChange(
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

    @Throws(CouldNotPerformException::class)
    fun addTrigger(trigger: Trigger, triggerAggregation: TriggerAggregation) {
        if (triggerAggregation == TriggerAggregation.AND) {
            triggerListAND.add(trigger)
        } else {
            triggerListOR.add(trigger)
        }
        if (active) {
            when (triggerAggregation) {
                TriggerAggregation.OR -> trigger.addObserver(triggerOrObserver)
                TriggerAggregation.AND -> trigger.addObserver(triggerAndObserver)
            }
            try {
                trigger.activate()
            } catch (ex: InterruptedException) {
                throw CouldNotPerformException("Could not activate Trigger.", ex)
            }
            try {
                verifyCondition()
            } catch (ex: NotAvailableException) {
                //ExceptionPrinter.printHistory("Data not available " + trigger, ex, LoggerFactory.getLogger(getClass()));
            }
        }
    }

    fun removeTrigger(trigger: AbstractTrigger) {
        if (triggerListAND.contains(trigger)) {
            trigger.removeObserver(triggerAndObserver)
            triggerListAND.remove(trigger)
        } else if (triggerListOR.contains(trigger)) {
            trigger.removeObserver(triggerOrObserver)
            triggerListOR.remove(trigger)
        }
    }

    @Throws(CouldNotPerformException::class)
    private fun verifyCondition() {
        if (verifyOrCondition() || verifyAndCondition()) {
            if (activationState.value != ActivationStateType.ActivationState.State.ACTIVE) {
                notifyChange(
                    TimestampProcessor.updateTimestampWithCurrentTime(
                        ActivationStateType.ActivationState.newBuilder().setValue(
                            ActivationStateType.ActivationState.State.ACTIVE
                        ).build()
                    )
                )
            }
        } else {
            if (activationState.value != ActivationStateType.ActivationState.State.INACTIVE) {
                notifyChange(
                    TimestampProcessor.updateTimestampWithCurrentTime(
                        ActivationStateType.ActivationState.newBuilder().setValue(
                            ActivationStateType.ActivationState.State.INACTIVE
                        ).build()
                    )
                )
            }
        }
    }

    @Throws(CouldNotPerformException::class)
    private fun verifyAndCondition(): Boolean {
        return if (triggerListAND.isEmpty()) {
            false
        } else triggerListAND
            .stream()
            .allMatch { trigger: Trigger ->
                try {
                    return@allMatch trigger.activationState.value == ActivationStateType.ActivationState.State.ACTIVE
                } catch (exception: NotAvailableException) {
                    return@allMatch false
                }
            }
    }

    @Throws(CouldNotPerformException::class)
    private fun verifyOrCondition(): Boolean {
        return triggerListOR
            .stream()
            .anyMatch { trigger: Trigger ->
                try {
                    return@anyMatch trigger.activationState.value == ActivationStateType.ActivationState.State.ACTIVE
                } catch (exception: NotAvailableException) {
                    return@anyMatch false
                }
            }
    }

    /**
     * return the total amount of registered triggers.
     *
     * @return the total amount of trigger.
     */
    val size: Int
        get() = triggerListAND.size + triggerListOR.size

    /**
     * Check if pool contains any trigger.
     *
     * @return true if no trigger are registered.
     */
    val isEmpty: Boolean
        get() = triggerListAND.isEmpty() && triggerListOR.isEmpty()

    @Throws(CouldNotPerformException::class, InterruptedException::class)
    override fun activate() {
        for (trigger in triggerListAND) {
            trigger.addObserver(triggerAndObserver)
            trigger.activate()
        }
        for (trigger in triggerListOR) {
            trigger.addObserver(triggerOrObserver)
            trigger.activate()
        }
        verifyCondition()
        active = true
    }

    @Throws(CouldNotPerformException::class, InterruptedException::class)
    override fun deactivate() {
        for (trigger in triggerListAND) {
            trigger.removeObserver(triggerAndObserver)
            trigger.deactivate()
        }
        for (trigger in triggerListOR) {
            trigger.removeObserver(triggerOrObserver)
            trigger.deactivate()
        }
        notifyChange(
            TimestampProcessor.updateTimestampWithCurrentTime(
                ActivationStateType.ActivationState.newBuilder().setValue(
                    ActivationStateType.ActivationState.State.UNKNOWN
                ).build()
            )
        )
        active = false
    }

    override fun isActive(): Boolean {
        return active
    }

    @Throws(CouldNotPerformException::class)
    fun forceNotification() {
        try {
            if (verifyOrCondition() || verifyAndCondition()) {
                notifyChange(
                    TimestampProcessor.updateTimestampWithCurrentTime(
                        ActivationStateType.ActivationState.newBuilder().setValue(
                            ActivationStateType.ActivationState.State.ACTIVE
                        ).build()
                    )
                )
            } else {
                notifyChange(
                    TimestampProcessor.updateTimestampWithCurrentTime(
                        ActivationStateType.ActivationState.newBuilder().setValue(
                            ActivationStateType.ActivationState.State.INACTIVE
                        ).build()
                    )
                )
            }
        } catch (ex: CouldNotPerformException) {
            throw CouldNotPerformException("Could not force notify trigger pool!", ex)
        }
    }

    override fun shutdown() {
        try {
            deactivate()
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (ex: CouldNotPerformException) {
            ExceptionPrinter.printHistory("Could not shutdown $this", ex, LoggerFactory.getLogger(javaClass))
        }
        super.shutdown()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AbstractTrigger::class.java)
    }
}
