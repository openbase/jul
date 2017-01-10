package org.openbase.jul.storage.file;

/*
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
import java.io.FileFilter;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.storage.file.filter.JSonFileFilter;
import static org.openbase.jul.storage.file.filter.JSonFileFilter.FILE_SUFFIX;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ProtoBufJSonFileProvider implements FileProvider<Identifiable<String>> {

    @Override
    public String getFileName(Identifiable<String> context) throws CouldNotPerformException {
        try {
            if (context == null) {
                throw new NotAvailableException("context");
            }

            return FileNameConverter.convertIntoValidFileName(context.getId().replaceAll("/", "_")) + FILE_SUFFIX;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not generate file name!", ex);
        }
    }

    @Override
    public String getFileType() {
        return JSonFileFilter.FILE_TYPE;
    }

    @Override
    public FileFilter getFileFilter() {
        return new JSonFileFilter();
    }
}
