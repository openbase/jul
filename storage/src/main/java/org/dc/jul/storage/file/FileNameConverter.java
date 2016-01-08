package org.dc.jul.storage.file;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class FileNameConverter {
    public static String convertIntoValidFileName(final String filename) {
        return filename.replaceAll("[^0-9a-zA-Z-äöüÄÖÜéàèÉÈßÄ\\.\\-\\_\\[\\]\\#\\$]+", "_");
    }
}
