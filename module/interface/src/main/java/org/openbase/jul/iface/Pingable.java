package org.openbase.jul.iface;

/*
 * #%L
 * JUL Interface
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
import java.util.concurrent.Future;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.annotation.RPCMethod;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface Pingable {

    /**
     * Method can be used to calculate connection ping.
     * The given timestamp argument is just returned from the local server to calculate the delay on client side.
     *
     * @param timestamp the current time in milliseconds.
     * @return the timestamp returned by the controller.
     */
    @RPCMethod(priority = RPCMethod.Priority.HIGH)
    Future<Long> ping(final Long timestamp);
}
