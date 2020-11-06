package org.openbase.jul.extension.type.transform;

/*-
 * #%L
 * JUL Extension RST Transform
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.type.processing.LabelProcessor;
import org.openbase.type.vision.ColorType.Color;
import org.openbase.type.vision.ColorType.Color.Builder;
import org.openbase.type.vision.ColorType.Color.Type;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ColorStateToLabelTransformerTest {

    @Test
    void computeColorLabelFromColor() throws NotAvailableException {

        final Builder colorBuilder = Color.newBuilder().setType(Type.HSB);
        colorBuilder.getHsbColorBuilder().setHue(0d);
        colorBuilder.getHsbColorBuilder().setSaturation(1.0d);
        colorBuilder.getHsbColorBuilder().setBrightness(1.0d);
        Assert.assertEquals("Color Label does not match!", LabelProcessor.getBestMatch(Locale.ENGLISH, ColorStateToLabelTransformer.computeColorLabelFromColor(colorBuilder.build())), "red");

        colorBuilder.getHsbColorBuilder().setHue(0d);
        colorBuilder.getHsbColorBuilder().setSaturation(0.5d);
        colorBuilder.getHsbColorBuilder().setBrightness(1.0d);
        Assert.assertEquals("Color Label does not match!", LabelProcessor.getBestMatch(Locale.ENGLISH, ColorStateToLabelTransformer.computeColorLabelFromColor(colorBuilder.build())), "salmon");

        colorBuilder.getHsbColorBuilder().setHue(0d);
        colorBuilder.getHsbColorBuilder().setSaturation(1.0d);
        colorBuilder.getHsbColorBuilder().setBrightness(0.5d);
        Assert.assertEquals("Color Label does not match!", LabelProcessor.getBestMatch(Locale.ENGLISH, ColorStateToLabelTransformer.computeColorLabelFromColor(colorBuilder.build())), "maroon");

        colorBuilder.getHsbColorBuilder().setHue(50d);
        colorBuilder.getHsbColorBuilder().setSaturation(1.0d);
        colorBuilder.getHsbColorBuilder().setBrightness(1.0d);
        Assert.assertEquals("Color Label does not match!", LabelProcessor.getBestMatch(Locale.ENGLISH, ColorStateToLabelTransformer.computeColorLabelFromColor(colorBuilder.build())), "gold");

        colorBuilder.getHsbColorBuilder().setHue(150d);
        colorBuilder.getHsbColorBuilder().setSaturation(1.0d);
        colorBuilder.getHsbColorBuilder().setBrightness(1.0d);
        Assert.assertEquals("Color Label does not match!", LabelProcessor.getBestMatch(Locale.ENGLISH, ColorStateToLabelTransformer.computeColorLabelFromColor(colorBuilder.build())), "spring green");
    }
}
