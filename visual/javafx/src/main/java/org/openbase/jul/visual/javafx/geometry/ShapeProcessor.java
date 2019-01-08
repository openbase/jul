package org.openbase.jul.visual.javafx.geometry;

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

import javafx.scene.shape.Shape;

public class ShapeProcessor {

    /**
     * Method resize the given {@code Shape} with respect of its original aspect ratio but independent of its original size.
     *
     * Note: Out of performance reasons the given object will directly be manipulated.
     *
     * @param shape the shape to scale.
     * @param size the size of the new shape.
     * @return the scaled {@code Shape} instance.
     */
    public static <S extends Shape> S resize(final S shape, final double size) {
        return resize(shape, size, size);
    }

    /**
     * Method resize the given {@code Shape} with respect of its original aspect ratio but independent of its original size.
     *
     * Note: Out of performance reasons the given object will directly be manipulated.
     *
     * @param shape the shape to scale.
     * @param width the width of the new shape.
     * @param height the height of the new shape.
     * @return the scaled {@code Shape} instance.
     */
    public static <S extends Shape> S resize(final S shape, final double width, final double height) {

        // compute original size
        final double originalWidth = shape.prefWidth(-1);
        final double originalHeight = shape.prefHeight(-1);

        final double scalingFactor = Math.min(width/originalWidth, height/originalHeight);

        // rescale
        shape.setScaleX(scalingFactor);
        shape.setScaleY(scalingFactor);

        return shape;
    }
}
