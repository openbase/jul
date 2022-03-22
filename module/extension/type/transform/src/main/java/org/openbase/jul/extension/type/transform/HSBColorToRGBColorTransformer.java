package org.openbase.jul.extension.type.transform;

/*
 * #%L
 * JUL Extension RST Transform
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
import org.openbase.type.vision.HSBColorType.HSBColor;
import org.openbase.type.vision.RGBColorType.RGBColor;

import java.awt.*;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class HSBColorToRGBColorTransformer {

    public static HSBColor transform(final RGBColor rgbColor) throws CouldNotTransformException {
        try {
            double hue, saturation, brightness;
            double red = rgbColor.getRed();
            double green = rgbColor.getGreen();
            double blue = rgbColor.getBlue();

            brightness = Math.max(red, Math.max(green, blue));
            double delta = brightness - Math.min(red, Math.min(green, blue));

            if (brightness != 0d) {
                saturation = delta / brightness;
            } else {
                saturation = 0d;
            }

            if (saturation == 0d) {
                hue = 0d;
            } else {

                double redRatio = (brightness - red) / delta;
                double greenRatio = (brightness - green) / delta;
                double blueRatio = (brightness - blue) / delta;

                if (red == brightness) {
                    hue = blueRatio - greenRatio;
                } else if (green == brightness) {
                    hue = 2.0d + redRatio - blueRatio;
                } else {
                    hue = 4.0d + greenRatio - redRatio;
                }

                hue = hue / 6.0d;

                if (hue < 0) {
                    hue = hue + 1.0d;
                }
            }

            hue *= 360.0d;

            return HSBColor.newBuilder().setHue(hue).setSaturation(saturation).setBrightness(brightness).build();
        } catch (Exception ex) {
            throw new CouldNotTransformException("Could not transform " + RGBColor.class.getName() + " to " + HSBColor.class.getName() + "!", ex);
        }
    }

    public static RGBColor transform(final HSBColor hsbColor) throws CouldNotTransformException {
        try {
            double hue = (((hsbColor.getHue() % 360) + 360) % 360) / 360;
            double saturation = hsbColor.getSaturation();
            double brightness = hsbColor.getBrightness();
            double red = 0, green = 0, blue = 0;

            if (saturation == 0) {
                red = green = blue = brightness;
            } else {
                double h = (hue - Math.floor(hue)) * 6.0;
                double f = h - java.lang.Math.floor(h);
                switch ((int) h) {
                    case 0:
                        red = brightness;
                        green = brightness * (1.0 - (saturation * (1.0 - f)));
                        blue = brightness * (1.0 - saturation);
                        break;
                    case 1:
                        red = brightness * (1.0 - saturation * f);
                        green = brightness;
                        blue = brightness * (1.0 - saturation);
                        break;
                    case 2:
                        red = brightness * (1.0 - saturation);
                        green = brightness;
                        blue = brightness * (1.0 - (saturation * (1.0 - f)));
                        break;
                    case 3:
                        red = brightness * (1.0 - saturation);
                        green = brightness * (1.0 - saturation * f);
                        blue = brightness;
                        break;
                    case 4:
                        red = brightness * (1.0 - (saturation * (1.0 - f)));
                        green = brightness * (1.0 - saturation);
                        blue = brightness;
                        break;
                    case 5:
                        red = brightness;
                        green = brightness * (1.0 - saturation);
                        blue = brightness * (1.0 - saturation * f);
                        break;
                }
            }

            return RGBColor.newBuilder().setRed(red).setGreen(green).setBlue(blue).build();
        } catch (Exception ex) {
            throw new CouldNotTransformException("Could not transform " + HSBColor.class.getName() + " to " + RGBColor.class.getName() + "!", ex);
        }
    }
}
