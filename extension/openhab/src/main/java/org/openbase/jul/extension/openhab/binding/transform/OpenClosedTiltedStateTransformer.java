package org.openbase.jul.extension.openhab.binding.transform;

/*
 * #%L
 * COMA DeviceManager Binding OpenHAB
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import org.openbase.jul.exception.TypeNotSupportedException;
import rst.homeautomation.state.HandleStateType.HandleState;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class OpenClosedTiltedStateTransformer {

    public static HandleState transform(final String stringType) throws CouldNotTransformException {
        switch (stringType) {
            case "CLOSED":
                return HandleState.newBuilder().setPosition(0).build();
            case "OPEN":
                return HandleState.newBuilder().setPosition(90).build();
            case "TILTED":
                return HandleState.newBuilder().setPosition(180).build();
            default:
                throw new CouldNotTransformException("Could not transform " + String.class.getName() + "! " + String.class.getSimpleName() + "[" + stringType + "] is unknown!");
        }
    }

    public static String transform(final HandleState handleState) throws CouldNotTransformException {
        try {
            switch (handleState.getPosition()) {
                case 0:
                    return "CLOSED";
                case 90:
                    return "OPEN";
                case 180:
                    return "TILTED";
                default:
                    throw new TypeNotSupportedException(handleState, String.class);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotTransformException("Could not transform " + HandleState.class.getName() + "!", ex);
        }
    }
}
