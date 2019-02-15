package org.openbase.jul.pattern.provider;

/*-
 * #%L
 * JUL Pattern Default
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ObservableDataProviderAdapter<D> implements DataProvider<D> {

    private final Class<D> dataClass;
    private final ObservableImpl<DataProvider<D>, D> observable;

    public ObservableDataProviderAdapter(final ObservableImpl<DataProvider<D>, D> observable, final Class<D> dataClass) {
        this.observable = observable;
        this.dataClass = dataClass;
    }

    protected ObservableImpl<DataProvider<D>, D> getObservable() {
        return observable;
    }

    @Override
    public boolean isDataAvailable() {
        return observable.isValueAvailable();
    }

    @Override
    public Class<D> getDataClass() {
        return dataClass;
    }

    @Override
    public D getData() throws NotAvailableException {
        return observable.getValue();
    }

    @Override
    public Future<D> getDataFuture() {
        return observable.getValueFuture();
    }

    @Override
    public void addDataObserver(Observer<DataProvider<D>, D> observer) {
        observable.addObserver(observer);
    }

    @Override
    public void removeDataObserver(Observer<DataProvider<D>, D> observer) {
        observable.removeObserver(observer);
    }

    @Override
    public void waitForData() throws CouldNotPerformException, InterruptedException {
        observable.waitForValue();
    }

    @Override
    public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
        observable.waitForValue(timeout, timeUnit);
    }
}
