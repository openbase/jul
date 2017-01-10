package org.openbase.jul.storage.file.filter;

/*-
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
