package org.openbase.jul.pattern.trigger

import org.openbase.jul.exception.NotAvailableException
import org.openbase.jul.iface.Activatable
import org.openbase.jul.pattern.Observer
import org.openbase.type.domotic.state.ActivationStateType

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
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
interface Trigger : Activatable {
    @get:Throws(NotAvailableException::class)
    val activationState: ActivationStateType.ActivationState
    fun removeObserver(observer: Observer<Trigger, ActivationStateType.ActivationState>?)
    fun addObserver(observer: Observer<Trigger, ActivationStateType.ActivationState>?)

    var priority: TriggerPriority
}
