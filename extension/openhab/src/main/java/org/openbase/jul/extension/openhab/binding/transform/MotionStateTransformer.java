package org.openbase.jul.extension.openhab.binding.transform;

/*
 * #%L
 * JUL Extension OpenHAB
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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.TypeNotSupportedException;
import rst.domotic.state.MotionStateType.MotionState;
import rst.domotic.state.MotionStateType.MotionState.State;
import static rst.domotic.state.MotionStateType.MotionState.State.MOTION;
import static rst.domotic.state.MotionStateType.MotionState.State.NO_MOTION;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class MotionStateTransformer {

    public static MotionState transform(final Double decimalType) throws CouldNotTransformException {
        MotionState.Builder motionState = MotionState.newBuilder();
        try {
            if (decimalType.intValue() == 0) {
                motionState.setValue(State.NO_MOTION);
            } else {
                motionState.setValue(State.MOTION);
            }
            return motionState.build();
        } catch (Exception ex) {
            throw new CouldNotTransformException("Could not transform " + Double.class.getName() + "! " + Double.class.getSimpleName() + "[" + decimalType + "] is unknown!", ex);
        }
    }

    public static Double transform(final MotionState motionState) throws CouldNotTransformException {
        try {
            switch (motionState.getValue()) {
                case NO_MOTION:
                    return 0d;
                case MOTION:
                    return 1d;
                case UNKNOWN:
                    throw new InvalidStateException("Unknown state is invalid!");
                default:
                    throw new TypeNotSupportedException(MotionState.State.class, Double.class);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotTransformException("Could not transform " + MotionState.State.class.getName() + "!", ex);
        }
    }
}
