package org.openbase.jul.processing;

/*
 * #%L
 * JUL Processing Default
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Transformer;
import org.openbase.jul.pattern.Filter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.regex.PatternSyntaxException;

/**
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class StringProcessor {

    public static String insertSpaceBetweenPascalCase(String input) {
        String output = (input.isEmpty() ? "" : Character.toString(input.charAt(0)));
        for (int i = 1; i < input.length(); i++) {
            if (Character.isLowerCase(input.charAt(i - 1)) && Character.isUpperCase(input.charAt(i))) {
                output += " " + input.charAt(i);
                continue;
            }
            output += input.charAt(i);
        }
        return output;
    }

    public static String removeDoubleWhiteSpaces(String input) {
        return input.replaceAll("\\s+", " ");
    }

    /**
     * Remove all white spaces (spaces, tabs, ...) from the input string.
     *
     * @param input the string from which white spaces are removed.
     *
     * @return the input with removed white spaces
     */
    public static String removeWhiteSpaces(String input) {
        return input.replaceAll("\\s+", "");
    }

    public static String formatHumanReadable(String input) {
        return removeDoubleWhiteSpaces(insertSpaceBetweenPascalCase(input).replaceAll("_", " ").replaceAll("-", " ")).trim();
    }

    public static String transformUpperCaseToPascalCase(final String input) {
        String output = "";
        for (String component : input.split("(_| )")) {
            if (component.isEmpty()) {
                continue;
            }
            output += component.substring(0, 1).toUpperCase() + component.substring(1).toLowerCase();
        }
        return output;
    }

    public static String transformToPascalCase(final String input) {
        return transformUpperCaseToPascalCase(transformToUpperCase(replaceHyphenWithUnderscore(input)));
    }

    public static String transformToCamelCase(final String input) {
        return transformFirstCharToLowerCase(transformToPascalCase(input));
    }

    public static String transformToKebabCase(final String input) {
        return transformToUpperCase(replaceHyphenWithUnderscore(input)).replace("_", "-").toLowerCase();
    }

    public static String replaceHyphenWithUnderscore(String input) {
        return input.replaceAll("-", "_");
    }

    public static String transformToUpperCase(String input) {
        input = removeDoubleWhiteSpaces(input.trim());
        String output = input.replaceAll("([a-z])([A-Z])", "$1_$2");
        output = output.replaceAll("_", " ");
        output = output.replaceAll("-", " ");
        output = removeDoubleWhiteSpaces(output).trim();
        output = output.replaceAll(" ", "_");
        return output.replaceAll("__", "_").toUpperCase();
    }

    public static String transformFirstCharToUpperCase(final String input) {
        if (input.isEmpty()) {
            return "";
        }

        if (input.length() == 1) {
            return input.toUpperCase();
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String transformFirstCharToLowerCase(final String input) {
        if (input.isEmpty()) {
            return "";
        }

        if (input.length() == 1) {
            return input.toLowerCase();
        }
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    /**
     * Method fills the given input string with width-spaces until the given string length is reached.
     * <p>
     * Note: The origin input string will aligned to the left.
     *
     * @param input  the original input string
     * @param lenght the requested input string length.
     *
     * @return the extended input string
     */
    public static String fillWithSpaces(String input, int lenght) {
        return fillWithSpaces(input, lenght, Alignment.LEFT);
    }

    /**
     * Method fills the given input string with width-spaces until the given string length is reached.
     * <p>
     * Note: The origin input string will aligned to the left.
     *
     * @param input         the original input string
     * @param lenght        the requested input string length.
     * @param textAlignment the alignment of the origin input string.
     *
     * @return the extended input string
     */
    public static String fillWithSpaces(String input, int lenght, final Alignment textAlignment) {
        String spaces = "";
        for (int i = lenght - input.length(); i > 0; i--) {
            spaces += " ";
        }
        switch (textAlignment) {
            case RIGHT:
                return spaces + input;
            case CENTER:
                final int half_spaces_size = (lenght - input.length()) / 2;
                return spaces.substring(0, half_spaces_size - 1) + input + spaces.substring(half_spaces_size);
            case LEFT:
            default:
                return input + spaces;
        }
    }

    public static String transformToIdString(String input) {
        try {
            input = removeDoubleWhiteSpaces(input);
            input = input.replaceAll("ä", "ae");
            input = input.replaceAll("ö", "oe");
            input = input.replaceAll("ü", "ue");
            input = input.replaceAll("ß", "ss");
            input = input.replaceAll("[^0-9a-zA-Z-_]+", "_");

            // cleanup
            input = input.replaceAll("[_]+", "_");
            if (input.startsWith("_")) {
                input = input.substring(1);
            }
            if (input.endsWith("_")) {
                input = input.substring(0, input.length() - 1);
            }
            return input;
        } catch (PatternSyntaxException ex) {
            new FatalImplementationErrorException("Could not transform [" + input + "] to id string!", StringProcessor.class, ex);
            return input;
        }
    }

    /**
     * Method normalizes a string into a simple file name by removing duplicated path limiters.
     *
     * @param filename the file name origin
     *
     * @return the normalized file name.
     */
    public static String transformToNormalizedFileName(final String filename) {
        return new File(filename).getPath();
    }

    /**
     * Method calls toString on each entry of the given collection and builds a string where each entry is separated by the given separator.
     *
     * @param collection the collection to repesent as string.
     * @param separator  the separator between each entry.
     * @param filters a set of filters to skip specific entries.
     *
     * @return the string representation of the collection.
     */
    public static <ENTRY>  String transformCollectionToString(final Collection<ENTRY> collection, final String separator, final Filter<ENTRY> ... filters) {
        String stringRepresentation = "";

        if(collection == null) {
            return stringRepresentation;
        }

        for (ENTRY entry : collection) {

            if (entry == null) {
                continue;
            }

            for (Filter<ENTRY> filter : filters) {
                if(filter.match(entry)) {
                    continue;
                }
            }

            final String entryString = entry.toString();

            if (entryString.isEmpty()) {
                continue;
            }

            if (!stringRepresentation.isEmpty()) {
                stringRepresentation += separator;
            }

            stringRepresentation += entryString;
        }
        return stringRepresentation;
    }

    /**
     * Method calls toString on each entry of the given collection and builds a string where each entry is separated by the given separator.
     *
     * @param collection the collection providing entries to print.
     * @param transformer provides a transformation from an entry to the string representation.
     * @param separator  the separator between each entry to use.
     * @param filters a set of filters to skip specific entries.
     *
     * @return the string representation of the collection.
     */
    public static <ENTRY> String transformCollectionToString(final Collection<ENTRY> collection, final Transformer<ENTRY, String> transformer, final String separator, final Filter<ENTRY> ... filters) {
        String stringRepresentation = "";
        outer: for (ENTRY entry : collection) {

            if (entry == null) {
                continue;
            }

            for (Filter<ENTRY> filter : filters) {
                if(filter.match(entry)) {
                    continue outer;
                }
            }

            final String entryString;
            try {
                entryString = transformer.transform(entry);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Skip Entry["+entry+"]!", ex, LoggerFactory.getLogger(StringProcessor.class), LogLevel.WARN);
                continue;
            }

            if (entryString.isEmpty()) {
                continue;
            }

            if (!stringRepresentation.isEmpty()) {
                stringRepresentation += separator;
            }

            stringRepresentation += entryString;
        }
        return stringRepresentation;
    }

    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }
}
