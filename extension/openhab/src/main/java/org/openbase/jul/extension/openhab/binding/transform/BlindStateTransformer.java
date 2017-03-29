package org.openbase.jul.extension.openhab.binding.transform;

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
     * @param brightnessState the state
     * @return the current brightness value
     */
    public static Double transform(BlindState blindState) {
        return blindState.getOpeningRatio();
    }
}
