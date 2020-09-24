package org.openbase.jul.extension.type.transform;

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
        Assert.assertEquals("Color Label does not match!", LabelProcessor.getBestMatch(Locale.ENGLISH, ColorStateToLabelTransformer.computeColorLabelFromColor(colorBuilder.build())), "Red");

        colorBuilder.getHsbColorBuilder().setHue(0d);
        colorBuilder.getHsbColorBuilder().setSaturation(0.5d);
        colorBuilder.getHsbColorBuilder().setBrightness(1.0d);
        Assert.assertEquals("Color Label does not match!", LabelProcessor.getBestMatch(Locale.ENGLISH, ColorStateToLabelTransformer.computeColorLabelFromColor(colorBuilder.build())), "Salmon");

        colorBuilder.getHsbColorBuilder().setHue(0d);
        colorBuilder.getHsbColorBuilder().setSaturation(1.0d);
        colorBuilder.getHsbColorBuilder().setBrightness(0.5d);
        Assert.assertEquals("Color Label does not match!", LabelProcessor.getBestMatch(Locale.ENGLISH, ColorStateToLabelTransformer.computeColorLabelFromColor(colorBuilder.build())), "Maroon");

        colorBuilder.getHsbColorBuilder().setHue(50d);
        colorBuilder.getHsbColorBuilder().setSaturation(1.0d);
        colorBuilder.getHsbColorBuilder().setBrightness(1.0d);
        Assert.assertEquals("Color Label does not match!", LabelProcessor.getBestMatch(Locale.ENGLISH, ColorStateToLabelTransformer.computeColorLabelFromColor(colorBuilder.build())), "Gold");

        colorBuilder.getHsbColorBuilder().setHue(150d);
        colorBuilder.getHsbColorBuilder().setSaturation(1.0d);
        colorBuilder.getHsbColorBuilder().setBrightness(1.0d);
        Assert.assertEquals("Color Label does not match!", LabelProcessor.getBestMatch(Locale.ENGLISH, ColorStateToLabelTransformer.computeColorLabelFromColor(colorBuilder.build())), "SpringGreen");

    }
}