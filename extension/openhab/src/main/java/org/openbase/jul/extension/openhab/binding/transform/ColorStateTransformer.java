/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.openhab.binding.transform;

import org.openbase.jul.exception.CouldNotTransformException;
import rst.homeautomation.openhab.HSBType;
import rst.homeautomation.state.ColorStateType.ColorState;
import rst.vision.ColorType.Color;
import rst.vision.HSBColorType.HSBColor;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class ColorStateTransformer {

    public static ColorState transform(HSBType.HSB hsbColor) throws CouldNotTransformException {
        try {
            HSBColor hsbColorBuilder = HSBColor.newBuilder().setHue(hsbColor.getHue()).setSaturation(hsbColor.getSaturation()).setBrightness(hsbColor.getBrightness()).build();
            Color color = Color.newBuilder().setHsbColor(hsbColorBuilder).setType(Color.Type.HSB).build();
            return ColorState.newBuilder().setColor(color).build();
        } catch (Exception ex) {
            throw new CouldNotTransformException("Could not transform " + HSBType.HSB.class.getName() + " to " + ColorState.class.getName() + "!", ex);
        }
    }

    public static HSBType.HSB transform(ColorState colorState) throws CouldNotTransformException {
        try {
            HSBColor color = colorState.getColor().getHsbColor();
            return HSBType.HSB.newBuilder().setHue(color.getHue()).setSaturation(color.getSaturation()).setBrightness(color.getBrightness()).build();
        } catch (Exception ex) {
            throw new CouldNotTransformException("Could not transform " + HSBColor.class.getName() + " to " + HSBType.class.getName() + "!", ex);
        }
    }
}
