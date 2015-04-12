/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.storage.file.FileSynchronizer;
import de.citec.jul.storage.file.FileProvider;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.MultiException.ExceptionStack;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.processing.FileProcessor;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <VALUE>
 * @param <MAP>
 * @param <R>
 */
public class FileSynchronizedRegistry<KEY, VALUE extends Identifiable<KEY>, MAP extends Map<KEY, VALUE>, R extends FileSynchronizedRegistryInterface<KEY, VALUE, R>> extends AbstractRegistry<KEY, VALUE, MAP, R> implements FileSynchronizedRegistryInterface<KEY, VALUE, R> {

    private final File databaseDirectory;
    private final Map<KEY, FileSynchronizer<VALUE>> fileSynchronizerMap;
    private final FileProcessor<VALUE> fileProcessor;
    private final FileProvider<Identifiable<KEY>> fileProvider;

    public FileSynchronizedRegistry(final MAP registry, final File databaseDirectory, final FileProcessor<VALUE> fileProcessor, final FileProvider<Identifiable<KEY>> fileProvider) {
        super(registry);
        this.databaseDirectory = databaseDirectory;
        this.fileSynchronizerMap = new HashMap<>();
        this.fileProcessor = fileProcessor;
        this.fileProvider = fileProvider;
    }

    @Override
    public VALUE register(final VALUE entry) throws CouldNotPerformException {
        super.register(entry);
        fileSynchronizerMap.put(entry.getId(), new FileSynchronizer<>(entry, new File(databaseDirectory, fileProvider.getFileName(entry)), FileSynchronizer.InitMode.CREATE, fileProcessor));
        return entry;
    }

    @Override
    public VALUE update(final VALUE entry) throws CouldNotPerformException {
        super.update(entry);
        
        // ignore update during registration process.
        if(!fileSynchronizerMap.containsKey(entry.getId())) {
            logger.debug("Ignore update during registration process of entry "+entry);
            return entry;
        }
        
        fileSynchronizerMap.get(entry.getId()).save(entry);
        return entry;
    }

    @Override
    public VALUE remove(final VALUE entry) throws CouldNotPerformException {
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

    @Override
    public void loadRegistry() throws CouldNotPerformException {
        assert databaseDirectory != null;
        logger.info("Load registry out of " + databaseDirectory + "...");
        ExceptionStack exceptionStack = null;

        File[] listFiles;

            listFiles = databaseDirectory.listFiles(fileProvider.getFileFilter());
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
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }
        MultiException.checkAndThrow("Could not load all registry enties!", exceptionStack);
    }

    @Override
    public void saveRegistry() throws MultiException {
        logger.info("Save registry into " + databaseDirectory + "...");
        ExceptionStack exceptionStack = null;
        for (FileSynchronizer<VALUE> fileSynchronizer : fileSynchronizerMap.values()) {
            try {
                fileSynchronizer.save();
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }
        MultiException.checkAndThrow("Could not save all registry enties!", exceptionStack);
    }

    @Override
    public void checkAccess() throws InvalidStateException {
        super.checkAccess();
        if (!databaseDirectory.canWrite()) {
            throw new InvalidStateException("DatabaseDirectory[" + databaseDirectory.getAbsolutePath() + "] not writable!");
        }
    }

	@Override
	public void shutdown() {
		try {
			saveRegistry();
		} catch (MultiException ex) {
			ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Final save failed!", ex));
		}
		super.shutdown();
	}
}
