package org.openbase.jul.communication;

/*-
 * #%L
 * JUL Extension RPC Communication
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
 */

import org.openbase.jul.communication.exception.RPCException;
import org.openbase.jul.communication.exception.RPCResolvedException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @param <V> The result type returned by this Future's {@code get} method
 */
public class RPCFutureImpl<V> implements Future<V> {

    final Future<V> internalFuture;


    public RPCFutureImpl(final Future<V> internalFuture) {
        this.internalFuture = internalFuture;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return internalFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return internalFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return internalFuture.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            return internalFuture.get();
        } catch (final ExecutionException ex) {
            if (ex.getCause() instanceof RPCException) {
                throw new ExecutionException(new RPCResolvedException((RPCException) ex.getCause()));
            }
            throw ex;
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return internalFuture.get(timeout, unit);
        } catch (final ExecutionException ex) {
            if (ex.getCause() instanceof RPCException) {
                throw new ExecutionException(new RPCResolvedException("Remote task failed!", (RPCException) ex.getCause()));
            }
            throw ex;
        }
    }
}
