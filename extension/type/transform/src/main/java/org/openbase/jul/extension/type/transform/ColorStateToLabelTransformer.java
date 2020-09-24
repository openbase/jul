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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.EnumNotSupportedException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.type.processing.LabelProcessor;
import org.openbase.type.domotic.state.ColorStateType.ColorState;
import org.openbase.type.language.LabelType.Label;
import org.openbase.type.vision.ColorType.Color;
import org.openbase.type.vision.RGBColorType.RGBColor;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Color State to Label Transformer
 * <p>
 * The part of looking up a color name from the rgb values is edited from
 * Color mapping based on https://gist.github.com/nightlark/6482130#file-gistfile1-java by Ryan Mast (nightlark)
 *
 * @author Divine Threepwood
 */


public class ColorStateToLabelTransformer {

    private static final ArrayList<ColorLabelEntry> colorList = new ArrayList<>();

    private static final Label UNKNOWN_COLOR_LABEL = LabelProcessor.addLabel(Label.newBuilder(), Locale.ENGLISH, "?").build();

    /*
     * Initialize the color - label mapping
     */
    static {
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "AliceBlue", 0xF0, 0xF8, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "AntiqueWhite", 0xFA, 0xEB, 0xD7));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Aqua", 0x00, 0xFF, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Aquamarine", 0x7F, 0xFF, 0xD4));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Azure", 0xF0, 0xFF, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Beige", 0xF5, 0xF5, 0xDC));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Bisque", 0xFF, 0xE4, 0xC4));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Black", 0x00, 0x00, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "BlanchedAlmond", 0xFF, 0xEB, 0xCD));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Blue", 0x00, 0x00, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "BlueViolet", 0x8A, 0x2B, 0xE2));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Brown", 0xA5, 0x2A, 0x2A));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "BurlyWood", 0xDE, 0xB8, 0x87));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "CadetBlue", 0x5F, 0x9E, 0xA0));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Chartreuse", 0x7F, 0xFF, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Chocolate", 0xD2, 0x69, 0x1E));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Coral", 0xFF, 0x7F, 0x50));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "CornflowerBlue", 0x64, 0x95, 0xED));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Cornsilk", 0xFF, 0xF8, 0xDC));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Crimson", 0xDC, 0x14, 0x3C));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Cyan", 0x00, 0xFF, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkBlue", 0x00, 0x00, 0x8B));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkCyan", 0x00, 0x8B, 0x8B));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkGoldenRod", 0xB8, 0x86, 0x0B));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkGray", 0xA9, 0xA9, 0xA9));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkGreen", 0x00, 0x64, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkKhaki", 0xBD, 0xB7, 0x6B));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkMagenta", 0x8B, 0x00, 0x8B));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkOliveGreen", 0x55, 0x6B, 0x2F));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkOrange", 0xFF, 0x8C, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkOrchid", 0x99, 0x32, 0xCC));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkRed", 0x8B, 0x00, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkSalmon", 0xE9, 0x96, 0x7A));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkSeaGreen", 0x8F, 0xBC, 0x8F));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkSlateBlue", 0x48, 0x3D, 0x8B));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkSlateGray", 0x2F, 0x4F, 0x4F));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkTurquoise", 0x00, 0xCE, 0xD1));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DarkViolet", 0x94, 0x00, 0xD3));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DeepPink", 0xFF, 0x14, 0x93));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DeepSkyBlue", 0x00, 0xBF, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DimGray", 0x69, 0x69, 0x69));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "DodgerBlue", 0x1E, 0x90, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "FireBrick", 0xB2, 0x22, 0x22));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "FloralWhite", 0xFF, 0xFA, 0xF0));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "ForestGreen", 0x22, 0x8B, 0x22));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Fuchsia", 0xFF, 0x00, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Gainsboro", 0xDC, 0xDC, 0xDC));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "GhostWhite", 0xF8, 0xF8, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Gold", 0xFF, 0xD7, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "GoldenRod", 0xDA, 0xA5, 0x20));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Gray", 0x80, 0x80, 0x80));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Green", 0x00, 0x80, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "GreenYellow", 0xAD, 0xFF, 0x2F));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "HoneyDew", 0xF0, 0xFF, 0xF0));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "HotPink", 0xFF, 0x69, 0xB4));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Indigo", 0x4B, 0x00, 0x82));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "IndianRed", 0xCD, 0x5C, 0x5C));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Ivory", 0xFF, 0xFF, 0xF0));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Khaki", 0xF0, 0xE6, 0x8C));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Lavender", 0xE6, 0xE6, 0xFA));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LavenderBlush", 0xFF, 0xF0, 0xF5));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LawnGreen", 0x7C, 0xFC, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LemonChiffon", 0xFF, 0xFA, 0xCD));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightBlue", 0xAD, 0xD8, 0xE6));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightCoral", 0xF0, 0x80, 0x80));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightCyan", 0xE0, 0xFF, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightGoldenRodYellow", 0xFA, 0xFA, 0xD2));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightGray", 0xD3, 0xD3, 0xD3));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightGreen", 0x90, 0xEE, 0x90));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightPink", 0xFF, 0xB6, 0xC1));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightSalmon", 0xFF, 0xA0, 0x7A));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightSeaGreen", 0x20, 0xB2, 0xAA));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightSkyBlue", 0x87, 0xCE, 0xFA));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightSlateGray", 0x77, 0x88, 0x99));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightSteelBlue", 0xB0, 0xC4, 0xDE));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LightYellow", 0xFF, 0xFF, 0xE0));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Lime", 0x00, 0xFF, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "LimeGreen", 0x32, 0xCD, 0x32));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Linen", 0xFA, 0xF0, 0xE6));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Magenta", 0xFF, 0x00, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Maroon", 0x80, 0x00, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MediumAquaMarine", 0x66, 0xCD, 0xAA));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MediumBlue", 0x00, 0x00, 0xCD));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MediumOrchid", 0xBA, 0x55, 0xD3));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MediumPurple", 0x93, 0x70, 0xDB));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MediumSeaGreen", 0x3C, 0xB3, 0x71));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MediumSlateBlue", 0x7B, 0x68, 0xEE));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MediumSpringGreen", 0x00, 0xFA, 0x9A));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MediumTurquoise", 0x48, 0xD1, 0xCC));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MediumVioletRed", 0xC7, 0x15, 0x85));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MidnightBlue", 0x19, 0x19, 0x70));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MintCream", 0xF5, 0xFF, 0xFA));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "MistyRose", 0xFF, 0xE4, 0xE1));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Moccasin", 0xFF, 0xE4, 0xB5));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "NavajoWhite", 0xFF, 0xDE, 0xAD));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Navy", 0x00, 0x00, 0x80));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "OldLace", 0xFD, 0xF5, 0xE6));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Olive", 0x80, 0x80, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "OliveDrab", 0x6B, 0x8E, 0x23));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Orange", 0xFF, 0xA5, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "OrangeRed", 0xFF, 0x45, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Orchid", 0xDA, 0x70, 0xD6));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "PaleGoldenRod", 0xEE, 0xE8, 0xAA));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "PaleGreen", 0x98, 0xFB, 0x98));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "PaleTurquoise", 0xAF, 0xEE, 0xEE));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "PaleVioletRed", 0xDB, 0x70, 0x93));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "PapayaWhip", 0xFF, 0xEF, 0xD5));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "PeachPuff", 0xFF, 0xDA, 0xB9));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Peru", 0xCD, 0x85, 0x3F));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Pink", 0xFF, 0xC0, 0xCB));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Plum", 0xDD, 0xA0, 0xDD));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "PowderBlue", 0xB0, 0xE0, 0xE6));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Purple", 0x80, 0x00, 0x80));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Red", 0xFF, 0x00, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "RosyBrown", 0xBC, 0x8F, 0x8F));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "RoyalBlue", 0x41, 0x69, 0xE1));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "SaddleBrown", 0x8B, 0x45, 0x13));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Salmon", 0xFA, 0x80, 0x72));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "SandyBrown", 0xF4, 0xA4, 0x60));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "SeaGreen", 0x2E, 0x8B, 0x57));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "SeaShell", 0xFF, 0xF5, 0xEE));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Sienna", 0xA0, 0x52, 0x2D));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Silver", 0xC0, 0xC0, 0xC0));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "SkyBlue", 0x87, 0xCE, 0xEB));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "SlateBlue", 0x6A, 0x5A, 0xCD));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "SlateGray", 0x70, 0x80, 0x90));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Snow", 0xFF, 0xFA, 0xFA));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "SpringGreen", 0x00, 0xFF, 0x7F));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "SteelBlue", 0x46, 0x82, 0xB4));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Tan", 0xD2, 0xB4, 0x8C));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Teal", 0x00, 0x80, 0x80));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Thistle", 0xD8, 0xBF, 0xD8));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Tomato", 0xFF, 0x63, 0x47));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Turquoise", 0x40, 0xE0, 0xD0));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Violet", 0xEE, 0x82, 0xEE));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Wheat", 0xF5, 0xDE, 0xB3));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "White", 0xFF, 0xFF, 0xFF));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "WhiteSmoke", 0xF5, 0xF5, 0xF5));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "Yellow", 0xFF, 0xFF, 0x00));
        colorList.add(new ColorLabelEntry(Locale.ENGLISH, "YellowGreen", 0x9A, 0xCD, 0x32));
    }

    /**
     * Computes the closest known color that matches the given one at most.
     *
     * @param red   red value.
     * @param green green value.
     * @param blue  blue value.
     *
     * @return the label of the matching color.
     */
    public static Label computeColorLabelFromRgb(final double red, final double green, final double blue) {
        return computeColorLabelFromRgb((float) red, (float) green, (float) blue);
    }

    /**
     * Computes the closest known color that matches the given one at most.
     *
     * @param red   red value.
     * @param green green value.
     * @param blue  blue value.
     *
     * @return the label of the matching color.
     */
    public static Label computeColorLabelFromRgb(final float red, final float green, final float blue) {

        ColorLabelEntry closestMatch = null;
        float minMSE = Float.MAX_VALUE;
        float mse;

        for (ColorLabelEntry color : colorList) {
            mse = color.computeColorMSE(red, green, blue);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = color;
            }
        }

        if (closestMatch != null) {
            return closestMatch.getLabel();
        } else {
            return UNKNOWN_COLOR_LABEL;
        }
    }

    /**
     * Computes the closest known color that matches the given one at most.
     *
     * @param hexColor the color represented as hex value.
     *
     * @return the label of the matching color.
     */
    public Label computeColorLabelFromHex(int hexColor) {
        float r = (hexColor & 0xFF0000) >> 16;
        float g = (hexColor & 0xFF00) >> 8;
        float b = (hexColor & 0xFF);
        return computeColorLabelFromRgb(r, g, b);
    }

    /**
     * Computes the closest known color that matches the given one at most.
     *
     * @param colorState the color that should be used for the computation.
     *
     * @return the label of the matching color.
     */
    public Label computeColorLabelFromColorState(final ColorState colorState) throws NotAvailableException {
        return computeColorLabelFromColor(colorState.getColor());
    }

    /**
     * Computes the closest known color that matches the given one at most.
     *
     * @param color the color that should be used for the computation.
     *
     * @return the label of the matching color.
     */
    public Label computeColorLabelFromColor(final Color color) throws NotAvailableException {
        try {
            switch (color.getType()) {
                case HSB:
                    final RGBColor rgbColor = HSBColorToRGBColorTransformer.transform(color.getHsbColor());
                    return computeColorLabelFromRgb(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue());
                case RGB:
                    return computeColorLabelFromRgb(color.getRgbColor().getRed(), color.getRgbColor().getGreen(), color.getRgbColor().getBlue());
                case RGB24:
                    return computeColorLabelFromRgb(color.getRgb24Color().getRed(), color.getRgb24Color().getGreen(), color.getRgb24Color().getBlue());
                default:
                    throw new EnumNotSupportedException(color.getType(), this);
            }
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("Color Label", ex);
        }
    }

    /**
     * Color Label Entity which defines a color label mapping.
     */
    public static class ColorLabelEntry {

        public float red;
        public float green;
        public float blue;
        public Label.Builder labelBuilder;

        public ColorLabelEntry(final Locale languageCode, final String label, final int red, final int green, final int blue) {
            this(languageCode, label, (float) red, (float) green, (float) blue);
        }

        public ColorLabelEntry(final Locale languageCode, final String label, final float red, final float green, final float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.addLabel(languageCode, label);
        }

        /**
         * Compute Mean squared error between to colors which can be seen as color distance.
         *
         * @param red the red color ratio.
         * @param green the green color ratio.
         * @param blue the blue color ratio.
         *
         * @return the mean squared error
         */
        public float computeColorMSE(final float red, final float green, final float blue) {
            final float diffRed = red - this.red;
            final float diffGreen = green - this.green;
            final float diffBlue = blue - this.blue;
            return (diffRed * diffRed
                    + diffGreen * diffGreen
                    + diffBlue * diffBlue)
                    / 3; // n = 3
        }

        /**
         * Adds a new label to this entry.
         *
         * @param languageCode the locale of the label.
         * @param label        the label itself.
         *
         * @return the modified color label entry.
         */
        public ColorLabelEntry addLabel(final Locale languageCode, final String label) {
            LabelProcessor.addLabel(labelBuilder, languageCode, label);
            return this;
        }

        /**
         * Returns the label of this color.
         *
         * @return the multi language label.
         */
        public Label getLabel() {
            return labelBuilder.build();
        }
    }
}
