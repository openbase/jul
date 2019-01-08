package org.openbase.jul.visual.javafx.geometry.svg;

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


import javafx.scene.shape.SVGPath;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.visual.javafx.geometry.svg.provider.SVGPathShapeProvider;
import org.openbase.jul.visual.javafx.geometry.svg.provider.SVGPathShapeProviderImpl;

import java.io.File;
import java.util.List;

/**
 * This class provides an icon pane using a svg path as input which can be loaded via an uri, file or icon provider.
 * Icons can be displayed, colorized and animated on the foreground and on the background layer.
 */
public class SVGPathIcon extends SVGIcon<SVGPath, SVGPathShapeProvider> {

    /**
     * Constructor instantiates an new svg icon pane.
     *
     * @param foregroundIconProvider the icon to display on the foreground layer.
     * @param size                   the size of the icon.
     * @param styled                 flag to define if this icon should be auto styled.
     */
    public SVGPathIcon(final SVGPath foregroundIconProvider, final double size, final boolean styled) {
        super(foregroundIconProvider, new SVGPathShapeProviderImpl(), size, styled);
    }

    /**
     * Constructor instantiates an new svg icon pane.
     *
     * @param backgroundIconProvider the icon to display on the background layer.
     * @param foregroundIconProvider the icon to display on the foreground layer.
     * @param size                   the size of the icon.
     */
    public SVGPathIcon(final SVGPath backgroundIconProvider, final SVGPath foregroundIconProvider, final double size) {
        super(backgroundIconProvider, foregroundIconProvider, new SVGPathShapeProviderImpl(), size);
    }

    /**
     * Constructor instantiates an new svg icon pane.
     *
     * @param foregroundIconFile the icon to display on the foreground layer.
     * @param size               the size of the icon.
     * @param styled             flag to define if this icon should be auto styled.
     *
     * @throws InstantiationException
     */
    public SVGPathIcon(final File foregroundIconFile, final double size, final boolean styled) throws InstantiationException {
        this(generateSVGPathForSVGPathIcon(foregroundIconFile).get(0), size, styled);
    }

    /**
     * Constructor instantiates an new svg icon pane.
     *
     * @param backgroundIconFile the icon to display on the background layer.
     * @param foregroundIconFile the icon to display on the foreground layer.
     * @param size               the size of the icon.
     *
     * @throws InstantiationException
     */
    public SVGPathIcon(final File backgroundIconFile, final File foregroundIconFile, final double size) throws InstantiationException {
        this(generateSVGPathForSVGPathIcon(backgroundIconFile).get(0), generateSVGPathForSVGPathIcon(foregroundIconFile).get(0), size);
    }

    /**
     * Constructor instantiates an new svg icon pane.
     * e
     *
     * @param iconFile the icon to display providing two svg path where the first is used as background and the second is used as foreground icon.
     * @param size     the size of the icon.
     *
     * @throws InstantiationException
     */
    public SVGPathIcon(final File iconFile, final double size) throws InstantiationException {
        this(generateSVGPathForSVGPathIcon(iconFile).get(0), generateSVGPathForSVGPathIcon(iconFile).get(1), size);
    }

    /**
     * Constructor instantiates an new svg icon pane.
     *
     * @param foregroundIconUri the icon to display on the foreground layer.
     * @param uriProvider       this class is used for loading the given icon uri via its class loader.
     * @param size              the size of the icon.
     * @param styled            flag to define if this icon should be auto styled.
     *
     * @throws InstantiationException
     */
    public SVGPathIcon(final String foregroundIconUri, final Class uriProvider, final double size, final boolean styled) throws InstantiationException {
        this(generateSVGPathForSVGPathIcon(foregroundIconUri, uriProvider).get(0), size, styled);
    }

    /**
     * Constructor instantiates an new svg icon pane.
     *
     * @param backgroundIconUri the icon to display on the background layer.
     * @param foregroundIconUri the icon to display on the foreground layer.
     * @param uriProvider       this class is used for loading the given icon uri via its class loader.
     * @param size              the size of the icon.
     *
     * @throws InstantiationException
     */
    public SVGPathIcon(final String backgroundIconUri, final String foregroundIconUri, final Class uriProvider, final double size) throws InstantiationException {
        this(generateSVGPathForSVGPathIcon(backgroundIconUri, uriProvider).get(0), generateSVGPathForSVGPathIcon(foregroundIconUri, uriProvider).get(0), size);
    }

    /**
     * Constructor instantiates an new svg icon pane.
     *
     * @param iconUri     the icon to display providing two svg path where the first is used as background and the second is used as foreground icon.
     * @param uriProvider this class is used for loading the given icon uri via its class loader.
     * @param size        the size of the icon.
     *
     * @throws InstantiationException
     */
    public SVGPathIcon(final String iconUri, final Class uriProvider, final double size) throws InstantiationException {
        this(generateSVGPathForSVGPathIcon(iconUri, uriProvider).get(0), generateSVGPathForSVGPathIcon(iconUri, uriProvider).get(1), size);
    }

    private static List<SVGPath> generateSVGPathForSVGPathIcon(final String uri, final Class clazz) throws InstantiationException {
        try {
            return SVGProcessor.loadSVGIconFromUri(uri, clazz);
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(SVGPathIcon.class, ex);
        }
    }

    private static List<SVGPath> generateSVGPathForSVGPathIcon(final File file) throws InstantiationException {
        try {
            return SVGProcessor.loadSVGIconFromFile(file);
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(SVGPathIcon.class, ex);
        }
    }
}
