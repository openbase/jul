package org.openbase.jul.communication.iface

import org.openbase.type.communication.EventType.Event
import java.util.concurrent.Future
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

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
 * * @author Divine [Divine](mailto:DivineThreepwood@gmail.com)
 */
interface RPCClient : RPCCommunicator {

    fun <RETURN: Any> callMethod(
        methodName: String,
        return_clazz: KClass<RETURN>,
        vararg parameters: Any): Future<RETURN>

    fun <RETURN: Any> callMethodRaw(
        methodName: String,
        return_clazz: KClass<RETURN>,
        vararg parameters: Any): Future<Event>

    fun <RETURN: Any> callMethod(
        methodName: String,
        return_clazz: Class<RETURN>,
        vararg parameters: Any): Future<RETURN> {
        return callMethod(
            methodName = methodName,
            return_clazz = Reflection.getOrCreateKotlinClass(return_clazz) as KClass<RETURN>,
            parameters = parameters
        )
    }

    fun <RETURN: Any> callMethodRaw(
        methodName: String,
        return_clazz: Class<RETURN>,
        vararg parameters: Any): Future<Event> {
        return callMethodRaw(
            methodName = methodName,
            return_clazz = Reflection.getOrCreateKotlinClass(return_clazz) as KClass<RETURN>,
            parameters = parameters
        )
    }
}
