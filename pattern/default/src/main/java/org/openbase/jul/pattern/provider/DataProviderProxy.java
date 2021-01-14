package org.openbase.jul.pattern.provider;

/*-
 * #%L
 * JUL Pattern Default
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.pattern.Observer;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DataProviderProxy<D> implements DataProvider<D> {

    private final DataProvider<D> internalDataProvider;

    public DataProviderProxy(final DataProvider<D> internalDataProvider) {
        this.internalDataProvider = internalDataProvider;
    }

    @Override
    public boolean isDataAvailable() {
        return internalDataProvider.isDataAvailable();
    }

    @Override
    public void validateData() throws InvalidStateException {
        internalDataProvider.validateData();
    }

    @Override
    public Class<D> getDataClass() {
        return internalDataProvider.getDataClass();
    }

    @Override
    public D getData() throws NotAvailableException {
        return internalDataProvider.getData();
    }

    @Override
    public Future<D> getDataFuture() {
        return internalDataProvider.getDataFuture();
    }

    @Override
    public void addDataObserver(Observer<DataProvider<D>, D> observer) {
        internalDataProvider.addDataObserver(observer);
    }

    @Override
    public void removeDataObserver(Observer<DataProvider<D>, D> observer) {
        internalDataProvider.removeDataObserver(observer);
    }

    @Override
    public void waitForData() throws CouldNotPerformException, InterruptedException {
        internalDataProvider.waitForData();
    }

    @Override
    public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
        internalDataProvider.waitForData(timeout, timeUnit);
    }
}
