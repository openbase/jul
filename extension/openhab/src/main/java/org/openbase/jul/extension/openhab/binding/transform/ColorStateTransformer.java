/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.openhab.binding.transform;

/*-
 * #%L
 * JUL Extension OpenHAB
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
