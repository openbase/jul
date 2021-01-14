package org.openbase.jul.extension.rsb.iface;

/*
 * #%L
 * JUL Extension RSB Interface
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
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Activatable;
import rsb.Factory;
import rsb.ParticipantId;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface RSBParticipant extends Activatable {

    String getKind() throws NotAvailableException;

    Class<?> getDataType() throws NotAvailableException;

    ParticipantId getId() throws NotAvailableException;

    Scope getScope() throws NotAvailableException;

    ParticipantConfig getConfig() throws NotAvailableException;

    void setObserverManager(Factory.ParticipantObserverManager observerManager) throws CouldNotPerformException;

}
