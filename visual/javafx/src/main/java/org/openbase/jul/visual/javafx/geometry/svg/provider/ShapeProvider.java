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

import javafx.scene.shape.Shape;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.visual.javafx.geometry.ShapeProcessor;

/**
 * Generic JavaFX Shape provider.
 */
public interface ShapeProvider<SOURCE> {

    /**
     * Returns the shape of this instance.
     * @throws NotAvailableException is thrown in case the shape is not available or could not be loaded.
     */
    Shape getShape(final SOURCE source) throws NotAvailableException;

    /**
     * Returns the shape of this instance in the given size.
     * @throws NotAvailableException is thrown in case the shape is not available or could not be loaded.
     */
    default Shape getShape(final SOURCE source, final double size) throws NotAvailableException {
        return ShapeProcessor.resize(getShape(source), size);
    }
}
