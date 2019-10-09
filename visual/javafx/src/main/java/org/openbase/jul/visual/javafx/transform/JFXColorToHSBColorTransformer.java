package org.openbase.jul.visual.javafx.transform;

/*-
 * #%L
 * JUL Visual JavaFX
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
import javafx.scene.paint.Color;
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.type.vision.HSBColorType.HSBColor;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JFXColorToHSBColorTransformer {

    public static Color transform(final HSBColor hsbColor, double opacity) throws CouldNotTransformException {
        try {
            return Color.hsb(hsbColor.getHue(), hsbColor.getSaturation(), hsbColor.getBrightness(), opacity);
        } catch (final IllegalArgumentException ex) {
            throw new CouldNotTransformException(hsbColor, Color.class, ex);
        }
    }

    public static Color transform(final HSBColor hsbColor) throws CouldNotTransformException {
        return transform(hsbColor, 1.0);
    }

    public static HSBColor transform(final Color color) {
        return HSBColor.newBuilder().setHue(color.getHue()).setSaturation(color.getSaturation()).setBrightness(color.getBrightness()).build();
    }
}
