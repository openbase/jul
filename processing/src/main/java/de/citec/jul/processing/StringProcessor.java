/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.processing;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
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
}
