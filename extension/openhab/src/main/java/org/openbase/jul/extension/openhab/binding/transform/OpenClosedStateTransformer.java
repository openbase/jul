package org.openbase.jul.extension.openhab.binding.transform;

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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.TypeNotSupportedException;
import rst.domotic.state.ContactStateType.ContactState;
import rst.domotic.binding.openhab.OpenClosedHolderType.OpenClosedHolder;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class OpenClosedStateTransformer {

    public static ContactState transform(OpenClosedHolder.OpenClosed openClosedType) throws CouldNotTransformException {
        switch (openClosedType) {
            case CLOSED:
                return ContactState.newBuilder().setValue(ContactState.State.CLOSED).build();
            case OPEN:
                return ContactState.newBuilder().setValue(ContactState.State.OPEN).build();
            default:
                throw new CouldNotTransformException("Could not transform " + OpenClosedHolder.OpenClosed.class.getName() + "! " + OpenClosedHolder.OpenClosed.class.getSimpleName() + "[" + openClosedType.name() + "] is unknown!");
        }
    }

    public static OpenClosedHolder transform(ContactState contactState) throws CouldNotTransformException {
        try {
            switch (contactState.getValue()) {
                case CLOSED:
                    return OpenClosedHolder.newBuilder().setState(OpenClosedHolder.OpenClosed.CLOSED).build();
                case OPEN:
                    return OpenClosedHolder.newBuilder().setState(OpenClosedHolder.OpenClosed.OPEN).build();
                case UNKNOWN:
                    throw new InvalidStateException("Unknown state is invalid!");
                default:
                    throw new TypeNotSupportedException(contactState, OpenClosedHolder.class);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotTransformException("Could not transform " + ContactState.class.getName() + "!", ex);
        }

    }
}
