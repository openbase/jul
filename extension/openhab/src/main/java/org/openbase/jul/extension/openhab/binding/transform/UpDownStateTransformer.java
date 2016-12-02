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
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.exception.TypeNotSupportedException;
import rst.domotic.binding.openhab.UpDownHolderType.UpDownHolder;
import rst.domotic.state.BlindStateType.BlindState;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class UpDownStateTransformer {

    public static BlindState transform(final UpDownHolder.UpDown upDownType) throws CouldNotTransformException {
        switch (upDownType) {
            case DOWN:
                return BlindState.newBuilder().setMovementState(BlindState.MovementState.DOWN).build();
            case UP:
                return BlindState.newBuilder().setMovementState(BlindState.MovementState.UP).build();
            default:
                throw new CouldNotTransformException("Could not transform " + UpDownHolder.UpDown.class.getName() + "! " + UpDownHolder.UpDown.class.getSimpleName() + "[" + upDownType.name() + "] is unknown!");
        }
    }

    public static UpDownHolder transform(final BlindState blindState) throws TypeNotSupportedException, CouldNotTransformException {
        switch (blindState.getMovementState()) {
            case DOWN:
                return UpDownHolder.newBuilder().setState(UpDownHolder.UpDown.DOWN).build();
            case UP:
                return UpDownHolder.newBuilder().setState(UpDownHolder.UpDown.UP).build();
            case UNKNOWN:
                throw new TypeNotSupportedException(blindState, UpDownHolder.UpDown.class);
            default:
                throw new CouldNotTransformException("Could not transform " + BlindState.class.getName() + "! " + BlindState.class.getSimpleName() + "[" + blindState.getMovementState().name() + "] is unknown!");
        }
    }
}
