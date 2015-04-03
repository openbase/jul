/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.file;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.iface.Identifiable;
import java.io.File;
import java.io.FileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 */
public class ProtoBufJSonFileProvider implements FileProvider<Identifiable<String>> {

        public static final String FILE_TYPE = "json";
        public static final String FILE_SUFFIX = "." + FILE_TYPE;

        @Override
        public String getFileName(Identifiable<String> context) throws CouldNotPerformException {
            try {
                return context.getId().replaceAll("/", "_") + FILE_SUFFIX;
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