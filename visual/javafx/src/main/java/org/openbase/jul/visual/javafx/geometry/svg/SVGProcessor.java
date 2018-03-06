package org.openbase.jul.visual.javafx.geometry.svg;

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

import javafx.scene.shape.SVGPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of util methods for processing SVG files or string.
 */
public class SVGProcessor {

    /**
     * The regex for matching "d=" elements.
     */
    public static final String PATH_REGEX_PREFIX = ".*d=\"([^\"]+)\"";

    /**
     * Method tries to build one or more SVGPaths out of the given uri.
     * By this the content is interpreted as svg xml and new SVGPath instances are generated for each found path element
     *
     * @param uri the svg xml file
     *
     * @return a list of SVGPaths instances where each is representing one found path element
     *
     * @throws CouldNotPerformException is thrown if the file does not exist or it does not contain any path elements.
     */
    public static List<SVGPath> loadSVGIconFromUri(final String uri, final Class clazz) throws CouldNotPerformException {
        try {

            InputStream inputStream = clazz.getResourceAsStream(uri);
            if (inputStream == null) {
                inputStream = clazz.getClassLoader().getResourceAsStream(uri);
                if (inputStream == null) {
                    throw new NotAvailableException(uri);
                }
            }
            return generateSvgPathList(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        } catch (final Exception ex) {
            throw new CouldNotPerformException("Could not load URI[" + uri + "]", ex);
        }
    }

    /**
     * Method tries to build one or more SVGPaths out of the passed file.
     * By this the file content is interpreted as svg xml and new SVGPath instances are generated for each found path element
     *
     * @param file the svg xml file
     *
     * @return a list of SVGPaths instances where each is representing one found path element
     *
     * @throws CouldNotPerformException is thrown if the file does not exist or it does not contain any path elements.
     */
    public static List<SVGPath> loadSVGIconFromFile(final File file) throws CouldNotPerformException {
        try {
            if (!file.exists()) {
                throw new NotAvailableException(file.getAbsolutePath());
            }
            return generateSvgPathList(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
        } catch (final Exception ex) {
            throw new CouldNotPerformException("Could not load path File[" + file + "]", ex);
        }
    }

    /**
     * This method extracts all svg paths out of the given xml string.
     * Its done by returning all value of each \"d" entry. All additional style definitions are ignored.
     *
     * @param xml the xml string containing at least one icon path.
     *
     * @return a list of paths.
     *
     * @throws NotAvailableException is thrown if no path could be detected.
     */
    public static List<String> parseSVGPath(final String xml) throws NotAvailableException {
        final List<String> pathList = new ArrayList<>();
        final Matcher matcher = Pattern.compile(PATH_REGEX_PREFIX).matcher(xml);
        while (matcher.find()) {
            // add group one of match to collection because the regex only defines one.
            pathList.add(matcher.group(1));
        }
        // fail if no pattern was found.
        if (pathList.isEmpty()) {
            throw new NotAvailableException("Path");
        }

        // clear prefix and suffix
        return pathList;
    }

    private static List<SVGPath> generateSvgPathList(final String input) throws NotAvailableException {
        final List<SVGPath> svgPathList = new ArrayList<>();
        for (final String pathString : parseSVGPath(input)) {
            final SVGPath svgPath = new SVGPath();
            svgPath.setContent(pathString);
            svgPathList.add(svgPath);
        }
        return svgPathList;
    }
}
