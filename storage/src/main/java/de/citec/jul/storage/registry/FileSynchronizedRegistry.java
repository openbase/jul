/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.storage.registry.plugin.FileRegistryPlugin;
import de.citec.jps.core.JPService;
import de.citec.jul.storage.file.FileSynchronizer;
import de.citec.jul.storage.file.FileProvider;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.MultiException.ExceptionStack;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.processing.FileProcessor;
import de.citec.jul.storage.registry.jp.JPInitializeDB;
import de.citec.jul.storage.registry.jp.JPResetDB;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <VALUE>
 * @param <MAP>
 * @param <R>
 */
public class FileSynchronizedRegistry<KEY, VALUE extends Identifiable<KEY>, MAP extends Map<KEY, VALUE>, R extends FileSynchronizedRegistryInterface<KEY, VALUE, R>> extends AbstractRegistry<KEY, VALUE, MAP, R, FileRegistryPlugin> implements FileSynchronizedRegistryInterface<KEY, VALUE, R> {

    private final File databaseDirectory;
    private final Map<KEY, FileSynchronizer<VALUE>> fileSynchronizerMap;
    private final FileProcessor<VALUE> fileProcessor;
    private final FileProvider<Identifiable<KEY>> fileProvider;

    public FileSynchronizedRegistry(final MAP entryMap, final File databaseDirectory, final FileProcessor<VALUE> fileProcessor, final FileProvider<Identifiable<KEY>> fileProvider) throws InstantiationException {
        super(entryMap);
        try {
            this.databaseDirectory = databaseDirectory;
            this.fileSynchronizerMap = new HashMap<>();
            this.fileProcessor = fileProcessor;
            this.fileProvider = fileProvider;
            this.prepareDB();
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    private void prepareDB() throws CouldNotPerformException {
        // clear db if reset property is set.
        if (JPService.getProperty(JPResetDB.class).getValue()) {
            try {
                FileUtils.deleteDirectory(databaseDirectory);
                FileUtils.forceMkdir(databaseDirectory);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not reset db!", ex);
            }
        }
    }

    @Override
    public VALUE register(final VALUE entry) throws CouldNotPerformException {
        super.register(entry);

        FileSynchronizer<VALUE> fileSynchronizer = new FileSynchronizer<>(entry, new File(databaseDirectory, fileProvider.getFileName(entry)), FileSynchronizer.InitMode.CREATE, fileProcessor);
        fileSynchronizerMap.put(entry.getId(), fileSynchronizer);

        for (FileRegistryPlugin plugin : pluginList) {
            plugin.afterRegister(fileSynchronizer);
        }

        return entry;
    }

    @Override
    public VALUE update(final VALUE entry) throws CouldNotPerformException {
        super.update(entry);

        // ignore update during registration process.
        if (!fileSynchronizerMap.containsKey(entry.getId())) {
            logger.debug("Ignore update during registration process of entry " + entry);
            return entry;
        }

        FileSynchronizer<VALUE> fileSynchronizer = fileSynchronizerMap.get(entry.getId());

        for (FileRegistryPlugin plugin : pluginList) {
            plugin.beforeUpdate(fileSynchronizer);
        }

        fileSynchronizer.save(entry);

        for (FileRegistryPlugin plugin : pluginList) {
            plugin.afterUpdate(fileSynchronizer);
        }

        return entry;
    }

    @Override
    public VALUE remove(final VALUE entry) throws CouldNotPerformException {
        VALUE removedValue = super.remove(entry);

        FileSynchronizer<VALUE> fileSynchronizer = fileSynchronizerMap.get(entry.getId());

        for (FileRegistryPlugin plugin : pluginList) {
            plugin.beforeRemove(fileSynchronizer);
        }

        fileSynchronizer.delete();
        fileSynchronizerMap.remove(entry.getId());

        for (FileRegistryPlugin plugin : pluginList) {
            plugin.afterRemove(fileSynchronizer);
        }

        return removedValue;
    }

    @Override
    public void clear() throws CouldNotPerformException {
        super.clear();

        for (FileRegistryPlugin plugin : pluginList) {
            plugin.beforeClear();
        }

        fileSynchronizerMap.clear();

        for (FileRegistryPlugin plugin : pluginList) {
            plugin.afterClear();
        }
    }

    @Override
    public void loadRegistry() throws CouldNotPerformException {
        assert databaseDirectory != null;

        if (JPService.getProperty(JPInitializeDB.class).getValue()) {
            return;
        }

        logger.info("Load registry out of " + databaseDirectory + "...");
        ExceptionStack exceptionStack = null;

        File[] listFiles;

        listFiles = databaseDirectory.listFiles(fileProvider.getFileFilter());

        if (listFiles == null) {
            throw new NotAvailableException("Could not load registry because database directory[" + databaseDirectory.getAbsolutePath() + "] is empty!");
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

        logger.info("====== " + size() + " entries successfully loaded. " + MultiException.size(exceptionStack) + " skipped. ======");

        checkConsistency();
        MultiException.checkAndThrow("Could not load all registry entries!", exceptionStack);
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
        MultiException.checkAndThrow("Could not save all registry entries!", exceptionStack);
    }

    @Override
    public void checkAccess() throws InvalidStateException {
        super.checkAccess();
        if (!databaseDirectory.canWrite()) {
            throw new InvalidStateException("DatabaseDirectory[" + databaseDirectory.getAbsolutePath() + "] not writable!");
        }

        for (FileRegistryPlugin plugin : pluginList) {
            plugin.checkAccess();
        }
    }

    @Override
    public void shutdown() {
        try {
            saveRegistry();
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Final save failed!", ex));
        }

        fileSynchronizerMap.clear();
        super.shutdown();
    }

    public File getDatabaseDirectory() {
        return databaseDirectory;
    }
}
