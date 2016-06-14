package org.openbase.jul.processing;

/*
 * #%L
 * JUL Processing
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class StringProcessor {

    public static String insertSpaceBetweenCamelCase(String input) {
        String output = "";
        String[] split = input.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        for (int i = 0; i < split.length; i++) {
            output += (i + 1 < split.length && !split[i].endsWith(" ")) ? split[i] + " " : split[i];
        }
        return output;
    }

    public static String removeDoubleWhiteSpaces(String input) {
        return input.replaceAll("\\s+", " ");
    }

    public static String formatHumanReadable(String input) {
        return removeDoubleWhiteSpaces(insertSpaceBetweenCamelCase(input)).trim();
    }

    public static String transformUpperCaseToCamelCase(final String input) {
        String output = "";
        for (String component : input.split("(_| )")) {
            if (component.isEmpty()) {
                continue;
            }
            output += component.substring(0, 1).toUpperCase() + component.substring(1).toLowerCase();
        }
        return output;
    }

    public static String replaceHyphenWithUnderscore(String input) {
        return input.replaceAll("-", "_");
    }

    public static String transformToUpperCase(String input) {
        input = removeDoubleWhiteSpaces(input.trim());
        String output = input.replaceAll("([a-z])([A-Z])", "$1_$2");
        output = output.replaceAll(" ", "_");
        return output.replaceAll("__", "_").toUpperCase();
    }
    
    public static String transformFirstCharToUpperCase(final String input) {
        if(input.isEmpty()) {
            return "";
        }
        
        if(input.length() == 1) {
            return input.toUpperCase();
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String fillWithSpaces(String input, int size) {
        String spaces = "";
        for(int i = size - input.length() ; i>0 ; i--) {
            spaces += " ";
        }
        return input + spaces;

    }

    public static String transformToIdString(String input) {
        input = removeDoubleWhiteSpaces(input);
        input = input.replaceAll("ä", "ae");
        input = input.replaceAll("ö", "oe");
        input = input.replaceAll("ü", "ue");
        input = input.replaceAll("ß", "ss");
        input = input.replaceAll("[^0-9a-zA-Z-_]+", "_");
        return input;
    }
}
