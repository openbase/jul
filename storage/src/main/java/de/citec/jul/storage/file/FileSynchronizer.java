/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.file;

import de.citec.jps.core.JPService;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.EnumNotSupportedException;
import de.citec.jul.pattern.Observable;
import de.citec.jul.processing.FileProcessor;
import de.citec.jul.storage.jp.JPInitializeDB;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author mpohling
 * @param <D> data type
 */
public class FileSynchronizer<D> extends Observable<D> {

    public enum InitMode {

        AUTO, CREATE, LOAD, REPLACE
    };

    private final FileProcessor<D> fileProcessor;
    private D data;
    private final File file;

    /**
     * Creates a new file with the given data and starts the synchronization.
     *
     * @param file
     * @param fileProcessor
     * @throws de.citec.jul.exception.InstantiationException
     */
    public FileSynchronizer(final File file, final FileProcessor<D> fileProcessor) throws InstantiationException {
        this(null, file, InitMode.LOAD, fileProcessor);
    }

    public FileSynchronizer(final D data, final File file, InitMode initMode, final FileProcessor<D> fileProcessor) throws InstantiationException {
        this.fileProcessor = fileProcessor;
        this.file = file;

        if (JPService.getProperty(JPInitializeDB.class).getValue()) {
            initMode = InitMode.REPLACE;
        }

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
                default:
                    throw new EnumNotSupportedException(initMode, null);

            }
        } catch (CouldNotPerformException ex) {
            throw new de.citec.jul.exception.InstantiationException(this, ex);
        }
    }

    public final D load() throws CouldNotPerformException {
        logger.info("Load " + file);
        return data = fileProcessor.deserialize(file);
    }

    public final File save(final D data) throws CouldNotPerformException {
        logger.info("Save " + data + " into " + file);
        try {
            if (data == null) {
                throw new NotAvailableException("data");
            }

            if (!file.exists()) {
                throw new NotAvailableException(file, "File not found!");
            }
            return fileProcessor.serialize(data, file);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not save " + data + "!", ex);
        }
    }

    public final File save() throws CouldNotPerformException {
        logger.info("Save " + data + " into " + file);
        return save(data);
    }

    private File create(D data) throws CouldNotPerformException {
        logger.info("Create " + file);
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
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
            if (!file.delete()) {
                throw new CouldNotPerformException("Could not delete File[" + file.getAbsolutePath() + "]!");
            }
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not delete database " + file + "!", ex);
        }
    }

    public D getData() {
        return data;
    }

    public File getFile() {
        return file;
    }
}
