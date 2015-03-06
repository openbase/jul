/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage;

import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.EnumNotSupportedException;
import de.citec.jul.pattern.Observable;
import de.citec.jul.processing.FileProcessor;
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

        AUTO, CREATE, LOAD
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

    public FileSynchronizer(final D data, final File file, final InitMode initMode, final FileProcessor<D> fileProcessor) throws InstantiationException {
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
                default:
                    throw new EnumNotSupportedException(initMode, null);

            }
        } catch (CouldNotPerformException ex) {
            throw new de.citec.jul.exception.InstantiationException(this, ex);
        }
    }

    public final D load() throws CouldNotPerformException {
        return data = fileProcessor.deserialize(file, data);
    }

    public final File save(final D data) throws CouldNotPerformException {
        try {
            if (!file.exists()) {
                throw new NotAvailableException(file, "File not found!");
            }
            return fileProcessor.serialize(data, file);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Coudl not save " + data + "!", ex);
        }
    }

    public final File save() throws CouldNotPerformException {
        return save(data);
    }

    private File create(D data) throws CouldNotPerformException {
        if (data == null) {
            throw new NotAvailableException("data");
        }
        try {
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
            if (file.delete()) {
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
