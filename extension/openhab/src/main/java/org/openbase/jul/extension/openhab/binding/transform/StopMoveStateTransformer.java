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
import rst.domotic.binding.openhab.StopMoveHolderType.StopMoveHolder;
import rst.domotic.state.BlindStateType.BlindState;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class StopMoveStateTransformer {

    public static BlindState transform(final StopMoveHolder.StopMove stopMoveType) throws CouldNotTransformException {
        switch (stopMoveType) {
            case STOP:
                return BlindState.newBuilder().setMovementState(BlindState.MovementState.STOP).build();
            case MOVE:
                return BlindState.newBuilder().setMovementState(BlindState.MovementState.UNKNOWN).build();
            default:
                throw new CouldNotTransformException("Could not transform " + StopMoveHolder.StopMove.class.getName() + "! " + StopMoveHolder.StopMove.class.getSimpleName() + "[" + stopMoveType.name() + "] is unknown!");
        }
    }

    public static StopMoveHolder transform(BlindState blindState) throws TypeNotSupportedException, CouldNotTransformException {
        switch (blindState.getMovementState()) {
            case STOP:
                return StopMoveHolder.newBuilder().setState(StopMoveHolder.StopMove.STOP).build();
            case UP:
                return StopMoveHolder.newBuilder().setState(StopMoveHolder.StopMove.MOVE).build();
            case DOWN:
                return StopMoveHolder.newBuilder().setState(StopMoveHolder.StopMove.MOVE).build();
            case UNKNOWN:
                throw new TypeNotSupportedException(blindState, StopMoveHolder.StopMove.class);
            default:
                throw new CouldNotTransformException("Could not transform " + BlindState.class.getName() + "! " + BlindState.class.getSimpleName() + "[" + blindState.getMovementState().name() + "] is unknown!");
        }
    }
}
