package org.openbase.jul.extension.openhab.binding.interfaces;

/*
 * #%L
 * JUL Extension OpenHAB
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.iface.Manageable;
import rst.domotic.binding.openhab.OpenhabCommandType.OpenhabCommand;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public interface OpenHABRemote extends Manageable<String> {

    @Override
    public void init(String itemFilter) throws InitializationException, InterruptedException;

    public void internalReceiveUpdate(final OpenhabCommand command) throws CouldNotPerformException;

    public void internalReceiveCommand(OpenhabCommand command) throws CouldNotPerformException;

    public Future<Void> sendCommand(OpenhabCommand command) throws CouldNotPerformException;

    public Future<Void> postCommand(final OpenhabCommand command) throws CouldNotPerformException;

    public Future<Void> postUpdate(final OpenhabCommand command) throws CouldNotPerformException;
}
