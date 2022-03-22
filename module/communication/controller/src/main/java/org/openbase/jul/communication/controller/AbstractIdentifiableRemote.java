package org.openbase.jul.communication.controller;

/*
 * #%L
 * JUL Extension Controller
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

import com.google.protobuf.Message;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;

import org.openbase.jul.extension.type.processing.ScopeProcessor;
import org.openbase.jul.pattern.controller.IdentifiableRemote;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <M>
 */
public abstract class AbstractIdentifiableRemote<M extends Message> extends AbstractRemoteClient<M> implements IdentifiableRemote<String, M> {

    public AbstractIdentifiableRemote(final Class<M> dataClass) {
        super(dataClass);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public String getId() throws NotAvailableException {
        try {
            String id = (String) getDataField(TYPE_FIELD_ID);
            if (id.isEmpty()) {
                throw new InvalidStateException("data.id is empty!");
            }
            return id;
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("data.id", ex);
        }
    }

    /**
     * Method prints a class instance representation.
     *
     * @return the class string representation.
     */
    @Override
    public String toString() {
        try {
            return getClass().getSimpleName() + "[scope:" + ScopeProcessor.generateStringRep(scope) + "]";
        } catch (CouldNotPerformException ex) {
            try {
                return getClass().getSimpleName() + "[id:" + getId() + "]";
            } catch (CouldNotPerformException exx) {
                return super.toString();
            }
        }
    }
}
