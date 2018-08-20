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
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.NotAvailableException.ContextType;

public class GenericShapeProvider implements ShapeProvider<Object> {

    private final static GlyphIconShapeProvider GLYPH_ICON_SHAPE_PROVIDER = new GlyphIconProviderImpl();
    private final static SVGPathShapeProvider SVG_PATH_SHAPE_PROVIDER = new SVGPathShapeProviderImpl();

    /**
     * {@inheritDoc}
     * @param o {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public Shape getShape(final Object o) throws NotAvailableException {
        if (o instanceof GlyphIcons) {
            return GLYPH_ICON_SHAPE_PROVIDER.getShape((GlyphIcons) o);
        }

        if (o instanceof Shape) {
            return SVG_PATH_SHAPE_PROVIDER.getShape((SVGPath) o);
        }

        throw new NotAvailableException(ContextType.USE_ID_AS_CONTEXT, "ShapeProvider", new InvalidStateException("Shape description " + o + " is not supported!"));
    }

    /**
     * {@inheritDoc}
     * @param o {@inheritDoc}
     * @param size {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public Shape getShape(final Object o, final double size) throws NotAvailableException {
        if (o instanceof GlyphIcons) {
            return GLYPH_ICON_SHAPE_PROVIDER.getShape((GlyphIcons) o);
        }

        if (o instanceof Shape) {
            return SVG_PATH_SHAPE_PROVIDER.getShape((SVGPath) o);
        }

        throw new NotAvailableException(ContextType.USE_ID_AS_CONTEXT, "ShapeProvider", new InvalidStateException("Shape description " + o + " is not supported!"));
    }
}
