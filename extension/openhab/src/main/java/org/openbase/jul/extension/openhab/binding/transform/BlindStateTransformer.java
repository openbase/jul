package org.openbase.jul.extension.openhab.binding.transform;

/*-
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
import rst.domotic.state.BlindStateType.BlindState;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class BlindStateTransformer {

    /**
     * Transform a number to a brightness state by setting the number as the brightness value.
     *
     * @param value the brightness value
     * @return the corresponding brightness state
     */
    public static BlindState transform(final Double value) {
        BlindState.Builder state = BlindState.newBuilder();
        state.setOpeningRatio(value);
        return state.build();
    }

    /**
     * Get the brightness value.
     *
     * @param blindState the state to transform
     * @return the current brightness value
     */
    public static Double transform(BlindState blindState) {
        return blindState.getOpeningRatio();
    }
}
