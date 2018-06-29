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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.rsb.com.TransactionIdProvider;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.AbstractSynchronizationFuture;
import rst.domotic.communication.TransactionValueType.TransactionValue;

import java.util.concurrent.Future;

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class TransactionVerificationFuture<DATA_PROVIDER extends DataProvider<?> & TransactionIdProvider> extends AbstractSynchronizationFuture<TransactionValue, DATA_PROVIDER> {

    /**
     * @param internalFuture
     * @param dataProvider
     */
    public TransactionVerificationFuture(Future<TransactionValue> internalFuture, DATA_PROVIDER dataProvider) {
        super(internalFuture, dataProvider);
        init();
    }

    @Override
    protected boolean check(TransactionValue transactionValue) throws CouldNotPerformException {
        return dataProvider.getTransactionId() >= transactionValue.getTransactionId();
    }
}
