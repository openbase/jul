/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.MultiException.ExceptionStack;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.processing.FileProcessor;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <VALUE>
 */
public class SynchronizedRegistry<KEY, VALUE extends Identifiable<KEY>> extends Registry<KEY, VALUE> {

    private final File databaseDirectory;
    private final Map<KEY, FileSynchronizer<VALUE>> fileSynchronizerMap;
    private final FileProcessor<VALUE> fileProcessor;
    private final FileNameProvider<VALUE> fileNameProvider;

    public SynchronizedRegistry(final File databaseDirectory, final FileProcessor<VALUE> fileProcessor, final FileNameProvider<VALUE> fileNameProvider) {
        this(new HashMap<KEY, VALUE>(), databaseDirectory, fileProcessor, fileNameProvider);
    }

    public SynchronizedRegistry(final Map<KEY, VALUE> registry, final File databaseDirectory, final FileProcessor<VALUE> fileProcessor, final FileNameProvider<VALUE> fileNameProvider) {
        super(registry);
        this.databaseDirectory = databaseDirectory;
        this.fileSynchronizerMap = new HashMap<>();
        this.fileProcessor = fileProcessor;
        this.fileNameProvider = fileNameProvider;
    }

    @Override
    public VALUE register(VALUE entry) throws CouldNotPerformException {
        super.register(entry);
        fileSynchronizerMap.put(entry.getId(), new FileSynchronizer<>(entry, new File(databaseDirectory, fileNameProvider.getFileName(entry)), FileSynchronizer.InitMode.CREATE, fileProcessor));
        return entry;
    }

    @Override
    public VALUE update(VALUE entry) throws CouldNotPerformException {
        super.update(entry);
        fileSynchronizerMap.get(entry.getId()).save(entry);
        return entry;
    }

    @Override
    public VALUE remove(VALUE entry) throws CouldNotPerformException {
        VALUE removedValue = super.remove(entry);
        fileSynchronizerMap.get(entry.getId()).delete();
        fileSynchronizerMap.remove(entry.getId());
        return removedValue;
    }

    @Override
    public void clean() {
        super.clean();
        fileSynchronizerMap.clear();
    }

    public void loadRegistry() throws CouldNotPerformException {
        assert databaseDirectory != null;
        logger.info("Load registry out of " + databaseDirectory + "...");
        ExceptionStack exceptionStack = null;

        File[] listFiles;

            listFiles = databaseDirectory.listFiles(new JSonFileFilter());
            if(listFiles == null) {
                throw new NotAvailableException("Could not load registry because database directory["+databaseDirectory.getAbsolutePath()+"] is empty!");
            }

        for (File file : listFiles) {
            try {
                FileSynchronizer<VALUE> fileSynchronizer = new FileSynchronizer<>(file, fileProcessor);
                VALUE entry = fileSynchronizer.getData();
                fileSynchronizerMap.put(entry.getId(), fileSynchronizer);
                super.register(entry);

            } catch (CouldNotPerformException ex) {
                MultiException.push(this, ex, exceptionStack);
            }
        }
        MultiException.checkAndThrow("Could not load all registry enties!", exceptionStack);
    }

    public void saveRegistry() throws MultiException {
        logger.info("Save registry into " + databaseDirectory + "...");
        ExceptionStack exceptionStack = null;
        for (FileSynchronizer<VALUE> fileSynchronizer : fileSynchronizerMap.values()) {
            try {
                fileSynchronizer.save();
            } catch (CouldNotPerformException ex) {
                MultiException.push(this, ex, exceptionStack);
            }
        }
        MultiException.checkAndThrow("Could not save all registry enties!", exceptionStack);
    }

    public class JSonFileFilter implements FileFilter {

        private static final String JSON_FILE_TYPE = "json";

        @Override
        public boolean accept(File file) {
            if (file == null) {
                return false;
            }

            return (!file.isHidden()) && file.isFile() && file.getName().toLowerCase().endsWith(JSON_FILE_TYPE);
        }
    }

    @Override
    public void checkAccess() throws InvalidStateException {
        super.checkAccess();
        if (!databaseDirectory.canWrite()) {
            throw new InvalidStateException("DatabaseDirectory[" + databaseDirectory.getAbsolutePath() + "] not writable!");
        }
    }
}
