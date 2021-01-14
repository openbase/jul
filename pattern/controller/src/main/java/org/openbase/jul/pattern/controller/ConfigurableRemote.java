package org.openbase.jul.pattern.controller;

/*
 * #%L
 * JUL Pattern Controller
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
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Manageable;
import org.openbase.jul.pattern.Observer;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <ID> Identifier
 * @param <M> Message
 * @param <CONFIG> Configuration
 */
public interface ConfigurableRemote<ID, M, CONFIG> extends IdentifiableRemote<ID, M>, Manageable<CONFIG>, Remote<M> {

    /**
     * Method returns the current configuration of this remote instance.
     *
     * @return the current configuration
     * @throws NotAvailableException if the configuration is not available
     */
    CONFIG getConfig() throws NotAvailableException;

    /**
     * Check if the config object is already available.
     *
     * @return true if config is available
     */
    default boolean isConfigAvailable() {
        try {
            getConfig();
        } catch (NotAvailableException e) {
            return false;
        }
        return true;
    }

    /**
     * Method returns the class of the configuration instance.
     *
     * @return the class of the configuration.
     */
    Class<CONFIG> getConfigClass();

    /**
     * This method allows the registration of config observers to get informed about config updates.
     *
     * @param observer the observer added
     */
    void addConfigObserver(final Observer<ConfigurableRemote<ID, M, CONFIG>, CONFIG> observer);

    /**
     * This method removes already registered config observers.
     *
     * @param observer the observer removed
     */
    void removeConfigObserver(final Observer<ConfigurableRemote<ID, M, CONFIG>, CONFIG> observer);
}
