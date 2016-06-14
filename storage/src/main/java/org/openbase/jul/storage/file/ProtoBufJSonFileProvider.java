package org.openbase.jul.storage.file;

/*
 * #%L
 * JUL Storage
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Identifiable;
import java.io.File;
import java.io.FileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class ProtoBufJSonFileProvider implements FileProvider<Identifiable<String>> {

    public static final String FILE_TYPE = "json";
    public static final String FILE_SUFFIX = "." + FILE_TYPE;

    @Override
    public String getFileName(Identifiable<String> context) throws CouldNotPerformException {
        try {
            if(context == null) {
                throw new NotAvailableException("context");
            }
            
            return FileNameConverter.convertIntoValidFileName(context.getId().replaceAll("/", "_")) + FILE_SUFFIX;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not generate file name!", ex);
        }
    }

    @Override
    public String getFileType() {
        return FILE_TYPE;
    }

    @Override
    public FileFilter getFileFilter() {
        return new FileFileFilter() {
            @Override
            public boolean accept(File file) {
                if (file == null) {
                    return false;
                }
                return (!file.isHidden()) && file.isFile() && file.getName().toLowerCase().endsWith(FILE_SUFFIX);
            }
        };
    }
}
