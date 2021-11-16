package org.openbase.jul.communication.iface

import com.google.protobuf.Any
import com.google.protobuf.Message
import org.openbase.jul.communication.iface.Communicator
import java.lang.InterruptedException
import org.openbase.jul.communication.iface.RPCCommunicator
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.RPCClient
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.type.communication.EventType.Event
import org.openbase.type.communication.ScopeType.Scope

/*
 * #%L
 * JUL Communication Default
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
 */ /**
 *
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
interface Publisher : Communicator {
    /**
     * Send an [Event] to all subscriber.
     *
     * @param event the event to send.
     * @return modified event with set timing information.
     * @throws CouldNotPerformException is thrown in case the message could not be sent.
     * @throws InterruptedException thrown in case the current thread was internally interrupted.
     */
    @Throws(CouldNotPerformException::class, InterruptedException::class)
    fun publish(event: Event): Event

    /**
     * Send data (of type T) to all subscriber.
     *
     * @param data data to send with default setting from the publisher.
     * @return generated event
     * @throws CouldNotPerformException is thrown in case the message could not be sent.
     * @throws InterruptedException thrown in case the current thread was internally interrupted.
     */
    @Throws(CouldNotPerformException::class, InterruptedException::class)
    fun publish(data: Message) = publish(Event.newBuilder().setPayload(Any.pack(data)).build())

    /**
     * Send an [Event] to all subscriber.
     *
     * @param event the event to send.
     * @param scope the scope of the event to send.
     * @return modified event with set timing information.
     * @throws CouldNotPerformException is thrown in case the message could not be sent.
     * @throws InterruptedException thrown in case the current thread was internally interrupted.
     */
    @Throws(CouldNotPerformException::class, InterruptedException::class)
    fun publish(event: Event, scope: Scope): Event

    /**
     * Send data (of type T) to all subscriber.
     *
     * @param data data to send with default setting from the publisher.
     * @param scope the scope of the event to send.
     * @return generated event
     * @throws CouldNotPerformException is thrown in case the message could not be sent.
     * @throws InterruptedException thrown in case the current thread was internally interrupted.
     */
    @Throws(CouldNotPerformException::class, InterruptedException::class)
    fun publish(data: Message, scope: Scope) = publish(Event.newBuilder().setPayload(Any.pack(data)).build(), scope)
}
