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
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.exception.TypeNotSupportedException;
import rst.domotic.binding.openhab.OnOffHolderType.OnOffHolder;
import rst.domotic.state.ButtonStateType.ButtonState;
import rst.domotic.state.ButtonStateType.ButtonState.State;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ButtonStateTransformer {

    public static ButtonState transform(final OnOffHolder.OnOff onOffType) throws CouldNotTransformException {
        switch (onOffType) {
            case OFF:
                return ButtonState.newBuilder().setValue(State.RELEASED).build();
            case ON:
                return ButtonState.newBuilder().setValue(State.PRESSED).build();
            default:
                throw new CouldNotTransformException("Could not transform " + OnOffHolder.OnOff.class.getName() + "! " + OnOffHolder.OnOff.class.getSimpleName() + "[" + onOffType.name() + "] is unknown!");
        }
    }

    public static OnOffHolder transform(final ButtonState buttonState) throws TypeNotSupportedException, CouldNotTransformException {
        switch (buttonState.getValue()) {
            case RELEASED:
                return OnOffHolder.newBuilder().setState(OnOffHolder.OnOff.OFF).build();
            case PRESSED:
                return OnOffHolder.newBuilder().setState(OnOffHolder.OnOff.ON).build();
            case UNKNOWN:
                throw new TypeNotSupportedException(buttonState, OnOffHolder.OnOff.class);
            default:
                throw new CouldNotTransformException("Could not transform " + ButtonState.State.class.getName() + "! " + ButtonState.State.class.getSimpleName() + "[" + buttonState.getValue().name() + "] is unknown!");
        }
    }
}
