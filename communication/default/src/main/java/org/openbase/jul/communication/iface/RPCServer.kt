package org.openbase.jul.communication.iface

import com.google.protobuf.Message
import org.openbase.jul.communication.iface.Communicator
import java.lang.InterruptedException
import org.openbase.jul.communication.iface.RPCCommunicator
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.RPCClient
import org.openbase.jul.exception.CouldNotPerformException
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.internal.impl.metadata.jvm.deserialization.JvmMemberSignature

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
interface RPCServer : RPCCommunicator {

    fun registerMethod(method: KFunction<*>, instance: Any)
}
