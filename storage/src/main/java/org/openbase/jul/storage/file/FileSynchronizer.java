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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.preset.JPTestMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.EnumNotSupportedException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.processing.FileProcessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.openbase.jul.exception.InvalidStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <D> data type
 */
public class FileSynchronizer<D> extends ObservableImpl<D> {

    public enum InitMode {

        AUTO, CREATE, LOAD, REPLACE
    };

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final FileProcessor<D> fileProcessor;
    private D data;
    private final File file;

    /**
     * Creates a new file with the given data and starts the synchronization.
     *
     * @param file
     * @param fileProcessor
     * @throws org.openbase.jul.exception.InstantiationException
     */
    public FileSynchronizer(final File file, final FileProcessor<D> fileProcessor) throws InstantiationException {
        this(null, file, InitMode.LOAD, fileProcessor);
    }

    public FileSynchronizer(final D data, final File file, InitMode initMode, final FileProcessor<D> fileProcessor) throws InstantiationException {
        this.fileProcessor = fileProcessor;
        this.file = file;

        try {
            switch (initMode) {
                case CREATE:
                    create(data);
                    break;
                case LOAD:
                    load();
                    break;
                case AUTO:
                    if (file.exists()) {
                        load();
                    } else {
                        create(data);
                    }
                    break;
                case REPLACE:
                    try {
                        delete();
                    } catch (Exception ex) {
                    }
                    create(data);
                    break;
                default:
                    throw new EnumNotSupportedException(initMode, null);

            }
        } catch (CouldNotPerformException ex) {
            throw new org.openbase.jul.exception.InstantiationException(this, ex);
        }
    }

    public final D load() throws CouldNotPerformException {
        logger.debug("Load " + file);
        data = fileProcessor.deserialize(file);
        return data;
    }

    public final File save(final D data) throws CouldNotPerformException {
        logger.debug("Save " + data + " into " + file);

        try {
            if (JPService.getProperty(JPTestMode.class).getValue()) {
                logger.debug("Skip data save because " + JPTestMode.class.getSimpleName() + " is enabled!");
                return file;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        try {
            if (data == null) {
                throw new NotAvailableException("data");
            }

            this.data = data;

            if (!file.exists()) {
                throw new NotAvailableException(File.class, file, new InvalidStateException("File does not exist!"));
            }

            return fileProcessor.serialize(data, file);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not save " + data + "!", ex);
        }
    }

    public final File save() throws CouldNotPerformException {
        logger.debug("Save " + data + " into " + file);
        return save(data);
    }

    private File create(D data) throws CouldNotPerformException {
        logger.debug("Create " + file);

        try {
            if (JPService.getProperty(JPTestMode.class).getValue()) {
                logger.debug("Skip file creation because " + JPTestMode.class.getSimpleName() + " is enabled!");
                return file;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        try {
            if (data == null) {
                throw new NotAvailableException("data");
            }
            if (!file.createNewFile()) {
                throw new CouldNotPerformException("Could not create " + file + "!");
            }
        } catch (IOException | CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not create data entry for " + data + "!", ex);
        }
        return save(data);
    }

    public void delete() throws CouldNotPerformException {

        try {
            if (JPService.getProperty(JPTestMode.class).getValue()) {
                logger.debug("Skip file deletion because " + JPTestMode.class.getSimpleName() + " is enabled!");
                return;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        try {
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
            if (!file.delete()) {
                throw new CouldNotPerformException("Could not delete File[" + file.getAbsolutePath() + "]!");
            }
        } catch (FileNotFoundException | CouldNotPerformException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not delete database " + file + "!", ex);
        }
    }

    public D getData() throws NotAvailableException {
        if (data == null) {
            throw new NotAvailableException("data");
        }
        return data;
    }

    public File getFile() {
        return file;
    }
}
