package org.openbase.jul.extension.type.transform;

/*-
 * #%L
 * JUL Extension RST Transform
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import org.junit.jupiter.api.Test;
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.type.vision.HSBColorType;
import org.openbase.type.vision.RGBColorType;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HSBColorToRGBColorTransformerTest {

    private static final int TEST_RUNS = 100;
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @Test
    public void testTransformHSBtoRGB() throws CouldNotTransformException {

        final HSBColorType.HSBColor.Builder inputHSBColorBuilder = HSBColorType.HSBColor.newBuilder();

        double hueDelta = 0;
        double saturationDelta = 0;
        double brightnessDelta = 0;

        for (int i = 0; i < TEST_RUNS; i++) {
            inputHSBColorBuilder.setHue(RANDOM.nextInt(360));
            inputHSBColorBuilder.setSaturation(RANDOM.nextDouble());
            inputHSBColorBuilder.setBrightness(RANDOM.nextDouble());

            final HSBColorType.HSBColor resultingHSBColor = HSBColorToRGBColorTransformer.transform(HSBColorToRGBColorTransformer.transform(inputHSBColorBuilder.build()));

            hueDelta += Math.abs(inputHSBColorBuilder.getHue() - resultingHSBColor.getHue());
            saturationDelta += Math.abs(inputHSBColorBuilder.getSaturation() - resultingHSBColor.getSaturation());
            brightnessDelta += Math.abs(inputHSBColorBuilder.getBrightness() - resultingHSBColor.getBrightness());
        }
        assertEquals(0d, hueDelta, 1d, "Color transformation hue delta to high!");
        assertEquals(0d, saturationDelta, 1d, "Color transformation saturation delta to high!");
        assertEquals(0d, brightnessDelta, 1d, "Color transformation brightness delta to high!");
    }

    @Test
    public void testTransformRGBtoHSB() throws CouldNotTransformException {
        final RGBColorType.RGBColor.Builder inputRGBColorBuilder = RGBColorType.RGBColor.newBuilder();
        for (int i = 0; i < TEST_RUNS; i++) {
            inputRGBColorBuilder.setRed(RANDOM.nextDouble());
            inputRGBColorBuilder.setGreen(RANDOM.nextDouble());
            inputRGBColorBuilder.setBlue(RANDOM.nextDouble());
            final RGBColorType.RGBColor resultingRGBColor = HSBColorToRGBColorTransformer.transform(HSBColorToRGBColorTransformer.transform(inputRGBColorBuilder.build()));
            assertEquals(inputRGBColorBuilder.getRed(), resultingRGBColor.getRed(), 0.000001d, "Color transformation red delta to high!");
            assertEquals(inputRGBColorBuilder.getGreen(), resultingRGBColor.getGreen(), 0.000001d, "Color transformation green delta to high!");
            assertEquals(inputRGBColorBuilder.getBlue(), resultingRGBColor.getBlue(), 0.000001d, "Color transformation blue delta to high!");
        }
    }
}
