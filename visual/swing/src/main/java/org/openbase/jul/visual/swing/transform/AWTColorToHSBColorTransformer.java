package org.openbase.jul.visual.swing.transform;

/*-
 * #%L
 * JUL Visual Swing
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import java.awt.Color;
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.exception.TypeNotSupportedException;
import org.openbase.type.vision.HSBColorType.HSBColor;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class AWTColorToHSBColorTransformer {

    public static HSBColor transform(final Color color) throws CouldNotTransformException {
        try {
            float[] hsb = new float[3];
            Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
            return HSBColor.newBuilder().setHue(hsb[0] * 360).setSaturation(hsb[1]).setBrightness(hsb[2]).build();
        } catch (Exception ex) {
            throw new CouldNotTransformException("Could not transform " + Color.class.getName() + " to " + HSBColor.class.getName() + "!", ex);
        }
    }

    public static Color transform(final HSBColor color) throws TypeNotSupportedException, CouldNotTransformException {
        try {
            return Color.getHSBColor((((float) color.getHue()) / 360f), (float) color.getSaturation(), (float) color.getBrightness());
        } catch (Exception ex) {
            throw new CouldNotTransformException("Could not transform " + HSBColor.class.getName() + " to " + Color.class.getName() + "!", ex);
        }
    }
}
