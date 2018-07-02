package org.openbase.jul.extension.rsb.com.future;

/*-
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import org.openbase.jul.extension.rsb.com.TransactionIdProvider;
import org.openbase.jul.pattern.provider.DataProvider;
import rst.domotic.communication.TransactionValueType.TransactionValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class TransactionVerificationConversionFuture<T extends Message> implements Future<T> {

    private final TransactionVerificationFuture<?> internalFuture;
    private final Class<T> messageClass;

    public TransactionVerificationConversionFuture(final TransactionVerificationFuture<?> internalFuture, final Class<T> messageClass) {
        this.internalFuture = internalFuture;
        this.messageClass = messageClass;
    }

    public <DATA_PROVIDER extends DataProvider<?> & TransactionIdProvider> TransactionVerificationConversionFuture(final Future<TransactionValue> internalFuture, DATA_PROVIDER provider, final Class<T> messageClass) {
        this(new TransactionVerificationFuture<>(internalFuture, provider), messageClass);
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
    public T get() throws InterruptedException, ExecutionException {
        return invokeParseFrom(internalFuture.get().getValue());
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return invokeParseFrom(internalFuture.get(timeout, unit).getValue());
    }

    private T invokeParseFrom(final ByteString bytes) throws ExecutionException {
        final Method parseFrom;
        try {
            parseFrom = messageClass.getMethod("parseFrom", ByteString.class);
            return (T) parseFrom.invoke(null, bytes);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new ExecutionException("Could not parse byte string into message class[" + messageClass.getSimpleName() + "]", ex);
        }
    }
}
