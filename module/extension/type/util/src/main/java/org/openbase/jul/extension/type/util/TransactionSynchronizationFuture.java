package org.openbase.jul.extension.type.util;

/*-
 * #%L
 * JUL Extension RST Util
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
 */

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.Message;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.protobuf.processing.ProtoBufFieldProcessor;
import org.openbase.jul.extension.type.iface.TransactionIdProvider;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.AbstractSynchronizationFuture;

import java.util.concurrent.Future;

/**
 * A future that verifies synchronization by comparing transaction ids from the given data provider and the
 * return value of the internal future.
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class TransactionSynchronizationFuture<T extends Message, REMOTE extends DataProvider<?> & TransactionIdProvider> extends AbstractSynchronizationFuture<T, REMOTE> {

    /**
     * The transaction id field of the message returned by the internal future.
     */
    private FieldDescriptor transactionIdField;

    /**
     * Create a new transaction synchronization future.
     *
     * @param internalFuture the internal future returning a message with transaction id
     * @param dataProvider   the data provider
     */
    public TransactionSynchronizationFuture(final Future<T> internalFuture, final REMOTE dataProvider) {
        super(internalFuture, dataProvider);
    }

    /**
     * Verify that the internal future has a transaction id field and that this field is of type long.
     *
     * @param message the returned message from the internal future
     *
     * @throws CouldNotPerformException if the message does not have a transaction id field
     */
    @Override
    protected void beforeWaitForSynchronization(final T message) throws CouldNotPerformException {

        if (message == null) {
            throw new NotAvailableException("message");
        }

        transactionIdField = ProtoBufFieldProcessor.getFieldDescriptor(message, TransactionIdProvider.TRANSACTION_ID_FIELD_NAME);

        if (transactionIdField == null) {
            throw new NotAvailableException("transaction id field for message[" + message.getClass().getSimpleName() + "]");
        }

        if (transactionIdField.getType() != Type.UINT64) {
            throw new CouldNotPerformException("Transaction id field of message[" + message.getClass().getSimpleName() + "] has an unexpected type[" + transactionIdField.getType().name() + "]");
        }
    }

    /**
     * Verify that the transaction id of the data provider is greater or equal to the transaction id in
     * the given message.
     *
     * @param message the return value of the internal future
     *
     * @return true if the transaction id of the data provider is greater or equal than the one in the internal message
     *
     * @throws CouldNotPerformException if the transaction id of the data provider is not available
     */
    @Override
    protected boolean check(T message) throws CouldNotPerformException {
        // get transaction id from message
        final long transactionId = (long) message.getField(transactionIdField);

        // to work with older versions where no transaction id has been accept empty ids and print a warning
        if (!message.hasField(transactionIdField) || transactionId == 0) {
            logger.warn("Received return value without transactionId");
            return true;
        }

        // check that the received transaction id has been reached by the provider
        final boolean result = dataProvider.getTransactionId() >= transactionId;

        if (!result) {
            logger.trace("Outdated transition {} received, waiting for {} of {}", dataProvider.getTransactionId(), transactionId, dataProvider);
        }

        return result;
    }
}
