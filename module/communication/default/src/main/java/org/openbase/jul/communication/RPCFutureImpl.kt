package org.openbase.jul.communication

import java.lang.InterruptedException
import org.openbase.jul.communication.exception.RPCException
import org.openbase.jul.communication.exception.RPCResolvedException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/*-
 * #%L
 * JUL Communication Default
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
 * @param <V> The result type returned by this Future's `get` method
</V> */
class RPCFutureImpl<V>(val internalFuture: Future<V>) : Future<V> {
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return internalFuture.cancel(mayInterruptIfRunning)
    }

    override fun isCancelled(): Boolean {
        return internalFuture.isCancelled
    }

    override fun isDone(): Boolean {
        return internalFuture.isDone
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    override fun get(): V {
        return try {
            internalFuture.get()
        } catch (ex: ExecutionException) {
            if (ex.cause is RPCException) {
                throw ExecutionException(RPCResolvedException(ex.cause as RPCException?))
            }
            throw ex
        }
    }

    @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    override fun get(timeout: Long, unit: TimeUnit): V {
        return try {
            internalFuture[timeout, unit]
        } catch (ex: ExecutionException) {
            if (ex.cause is RPCException) {
                throw ExecutionException(RPCResolvedException("Remote task failed!", ex.cause as RPCException?))
            }
            throw ex
        }
    }
}
