package org.openbase.jul.iface.provider;

/*-
 * #%L
 * JUL Interface
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

import java.util.concurrent.Future;

public interface PingProvider {
    /**
     * Method triggers a ping between this client instance and its server instance and
     * returns the calculated connection delay.
     *
     * @return the connection delay in milliseconds.
     */
    public Future<Long> ping();

    /**
     * Method returns the result of the latest connection ping between this
     * client and its server instance.
     *
     * @return the latest connection delay in milliseconds.
     */
    public Long getPing();
}
