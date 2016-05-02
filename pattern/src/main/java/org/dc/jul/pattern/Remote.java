package org.dc.jul.pattern;

/*
 * #%L
 * JUL Pattern
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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
import java.util.concurrent.Future;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.iface.Activatable;
import org.dc.jul.iface.Shutdownable;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <M>
 */
public interface Remote<M> extends Shutdownable, Activatable, Observable<M> {

    public Object callMethod(String methodName) throws CouldNotPerformException, InterruptedException;

    public <R, T extends Object> R callMethod(String methodName, T type) throws CouldNotPerformException;

    public Future<Object> callMethodAsync(String methodName) throws CouldNotPerformException;

    public <R, T extends Object> Future<R> callMethodAsync(String methodName, T type) throws CouldNotPerformException;

    public void init(final String scope) throws InitializationException, InterruptedException;

    public boolean isConnected();

    /**
     * public * Triggers a server - remote data sync and returns the new
     * acquired data. All server data changes are synchronized automatically to
     * all remote instances. In case you have triggered many server public *
     * changes, you can use this method to get instantly a data object with all
     * applied changes. public * public * Note: This method blocks until the new
     * data is acquired! public * public * @return fresh synchronized data
     * object. public * @throws CouldNotPerformException public
     *
     * @return
     * @throws org.dc.jul.exception.CouldNotPerformException
     */
    public Future<M> requestStatus() throws CouldNotPerformException;
}
