/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.file;

import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.preset.JPTestMode;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.EnumNotSupportedException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.pattern.Observable;
import org.dc.jul.processing.FileProcessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.dc.jul.exception.InvalidStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 * @param <D> data type
 */
public class FileSynchronizer<D> extends Observable<D> {

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
     * @throws org.dc.jul.exception.InstantiationException
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
            throw new org.dc.jul.exception.InstantiationException(this, ex);
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
                logger.warn("Skip data save because " + JPTestMode.class.getSimpleName() + " is enabled!");
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
                logger.warn("Skip file creation because " + JPTestMode.class.getSimpleName() + " is enabled!");
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
                logger.warn("Skip file deletion because " + JPTestMode.class.getSimpleName() + " is enabled!");
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
