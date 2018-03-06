package org.openbase.jul.visual.javafx.geometry.svg.provider;

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

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.GlyphsDude;
import javafx.scene.shape.Shape;

public interface GlyphIconShapeProvider extends ShapeProvider<GlyphIcons> {

    /**
     * {@inheritDoc}
     * @param glyphIcons {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    default Shape getShape(final GlyphIcons glyphIcons) {
        return GlyphsDude.createIcon(glyphIcons);
    }

    /**
     * {@inheritDoc}
     * @param glyphIcons {@inheritDoc}
     * @param size {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    default Shape getShape(final GlyphIcons glyphIcons, final double size) {
        return GlyphsDude.createIcon(glyphIcons, String.valueOf(size));
    }
}
