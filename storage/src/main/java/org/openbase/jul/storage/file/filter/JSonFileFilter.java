package org.openbase.jul.storage.file.filter;

import java.io.File;
import org.apache.commons.io.filefilter.AbstractFileFilter;

/**
 *
  * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JSonFileFilter extends AbstractFileFilter {

    public static final String FILE_TYPE = "json";
    public static final String FILE_SUFFIX = "." + FILE_TYPE;

    @Override
    public boolean accept(File file) {
        if (file == null) {
            return false;
        }
        return (!file.isHidden()) && file.isFile() && file.getName().toLowerCase().endsWith(FILE_SUFFIX);
    }
}
