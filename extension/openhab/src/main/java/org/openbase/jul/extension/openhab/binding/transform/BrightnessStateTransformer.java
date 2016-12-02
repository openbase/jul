package org.openbase.jul.extension.openhab.binding.transform;

import rst.domotic.state.BrightnessStateType.BrightnessState;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class BrightnessStateTransformer {

    /**
     * Transform a number to a brightness state by setting the number as the brightness value.
     *
     * @param value the brightness value
     * @return the corresponding brightness state
     */
    public static BrightnessState transform(final Double value) {
        BrightnessState.Builder state = BrightnessState.newBuilder();
        state.setBrightness(value);
        return state.build();
    }

    /**
     * Get the brightness value.
     *
     * @param brightnessState the state
     * @return the current brightness value
     */
    public static Double transform(BrightnessState brightnessState) {
        return brightnessState.getBrightness();
    }
}
