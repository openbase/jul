package org.openbase.jul.visual.javafx.transform;

/*-
 * #%L
 * JUL Visual JavaFX
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
import javafx.scene.paint.Color;
import org.openbase.jul.exception.CouldNotTransformException;
import rst.vision.HSBColorType.HSBColor;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JFXColorToHSBColorTransformer {

    public static Color transform(final HSBColor hsbColor) throws CouldNotTransformException {
        try {
            return Color.hsb(hsbColor.getHue(), hsbColor.getSaturation() / 100, hsbColor.getBrightness() / 100);
        } catch (final IllegalArgumentException ex) {
            throw new CouldNotTransformException(hsbColor, Color.class, ex);
        }
    }

    public static HSBColor transform(final Color color) throws CouldNotTransformException {
        return HSBColor.newBuilder().setHue(color.getHue()).setSaturation(color.getSaturation() * 100).setBrightness(color.getBrightness() * 100).build();
    }
}
