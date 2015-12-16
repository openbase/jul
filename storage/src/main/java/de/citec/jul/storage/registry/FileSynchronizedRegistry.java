/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jps.core.JPService;
import de.citec.jps.exception.JPServiceException;
import de.citec.jps.preset.JPTestMode;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.MultiException.ExceptionStack;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.RejectedException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.processing.FileProcessor;
import de.citec.jul.storage.file.FileProvider;
import de.citec.jul.storage.file.FileSynchronizer;
import de.citec.jul.storage.registry.jp.JPResetDB;
import de.citec.jul.storage.registry.plugin.FileRegistryPlugin;
import de.citec.jul.storage.registry.plugin.FileRegistryPluginPool;
import de.citec.jul.storage.registry.version.DBVersionControl;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <R>
 */
public class FileSynchronizedRegistry<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends FileSynchronizedRegistryInterface<KEY, ENTRY, R>> extends AbstractRegistry<KEY, ENTRY, MAP, R, FileRegistryPlugin<KEY, ENTRY>> implements FileSynchronizedRegistryInterface<KEY, ENTRY, R> {

    private final File databaseDirectory;
    private final Map<KEY, FileSynchronizer<ENTRY>> fileSynchronizerMap;
    private final FileProcessor<ENTRY> fileProcessor;
    private final FileProvider<Identifiable<KEY>> fileProvider;
    private final FileRegistryPluginPool<KEY, ENTRY, FileRegistryPlugin<KEY, ENTRY>> filePluginPool;
    private DBVersionControl versionControl;

    public FileSynchronizedRegistry(final MAP entryMap, final File databaseDirectory, final FileProcessor<ENTRY> fileProcessor, final FileProvider<Identifiable<KEY>> fileProvider) throws InstantiationException {
        this(entryMap, databaseDirectory, fileProcessor, fileProvider, new FileRegistryPluginPool<>());
    }

    public FileSynchronizedRegistry(final MAP entryMap, final File databaseDirectory, final FileProcessor<ENTRY> fileProcessor, final FileProvider<Identifiable<KEY>> fileProvider, final FileRegistryPluginPool<KEY, ENTRY, FileRegistryPlugin<KEY, ENTRY>> filePluginPool) throws InstantiationException {
        super(entryMap, new FileRegistryPluginPool<>());
        try {
            this.databaseDirectory = databaseDirectory;
            this.fileSynchronizerMap = new HashMap<>();
            this.fileProcessor = fileProcessor;
            this.fileProvider = fileProvider;
            this.filePluginPool = filePluginPool;
//            this.prepareDB();
        } catch (NullPointerException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    // should be done by JPDatabaseDirectory.
//    private void prepareDB() throws CouldNotPerformException {
//        // clear db if reset property is set.
//        if (JPService.getProperty(JPResetDB.class).getValue()) {
//            try {
//                FileUtils.deleteDirectory(databaseDirectory);
//                FileUtils.forceMkdir(databaseDirectory);
//            } catch (Exception ex) {
//                throw new CouldNotPerformException("Could not reset db!", ex);
//            }
//        }
//    }
    /**
     * This method activates the version control unit of the underlying registry db. The version check and db upgrade is automatically performed during the registry db loading phrase. The db will be
     * upgraded to the latest db format provided by the given converter package. The converter package should contain only classes implementing the DBVersionConverter interface. To fully support
     * outdated db upgrade make sure that the converter pipeline covers the whole version range!
     *
     * Activate version control before loading the registry. Please provide within the converter package only converter with the naming structure
     * [$(EntryType)_$(VersionN)_To_$(VersionN+1)_DBConverter].
     *
     * Example:
     *
     * converter package myproject.db.converter containing the converter pipeline
     *
     * myproject.db.converter.DeviceConfig_0_To_1_DBConverter.class myproject.db.converter.DeviceConfig_1_To_2_DBConverter.class myproject.db.converter.DeviceConfig_2_To_3_DBConverter.class
     *
     * Would support the db upgrade from version 0 till the latest db version 3.
     *
     * @param entryType
     * @param converterPackage the package containing all converter which provides db entry updates from the first to the latest db version.
     * @throws CouldNotPerformException in case of an invalid converter pipeline or initialization issues.
     */
    public void activateVersionControl(final String entryType, final Package converterPackage) throws CouldNotPerformException {
        if (!isEmpty()) {
            throw new CouldNotPerformException("Could not activate version control because registry already loaded! Please activate version control before loading the registry.");
        }
        versionControl = new DBVersionControl(entryType, fileProvider, converterPackage, databaseDirectory);
    }

    @Override
    public ENTRY register(final ENTRY entry) throws CouldNotPerformException {
        super.register(entry);

        FileSynchronizer<ENTRY> fileSynchronizer = new FileSynchronizer<>(entry, new File(databaseDirectory, fileProvider.getFileName(entry)), FileSynchronizer.InitMode.CREATE, fileProcessor);
        fileSynchronizerMap.put(entry.getId(), fileSynchronizer);
        filePluginPool.afterRegister(entry, fileSynchronizer);

        return entry;
    }

    @Override
    public ENTRY update(final ENTRY entry) throws CouldNotPerformException {
        super.update(entry);

        // ignore update during registration process.
        if (!fileSynchronizerMap.containsKey(entry.getId())) {
            logger.debug("Ignore update during registration process of entry " + entry);
            return entry;
        }

        FileSynchronizer<ENTRY> fileSynchronizer = fileSynchronizerMap.get(entry.getId());

        filePluginPool.beforeUpdate(entry, fileSynchronizer);
        fileSynchronizer.save(entry);
        filePluginPool.afterUpdate(entry, fileSynchronizer);

        return entry;
    }

    @Override
    public ENTRY remove(final ENTRY entry) throws CouldNotPerformException {
        ENTRY removedValue = super.remove(entry);

        FileSynchronizer<ENTRY> fileSynchronizer = fileSynchronizerMap.get(entry.getId());

        filePluginPool.beforeRemove(entry, fileSynchronizer);
        fileSynchronizer.delete();
        fileSynchronizerMap.remove(entry.getId());
        filePluginPool.afterRemove(entry, fileSynchronizer);

        return removedValue;
    }

    @Override
    public void clear() throws CouldNotPerformException {
        super.clear();
        fileSynchronizerMap.clear();
    }

    @Override
    public void loadRegistry() throws CouldNotPerformException {
        assert databaseDirectory != null;

        // check db version
        if (versionControl != null) {
            versionControl.validateAndUpgradeDBVersion();
        }

        try {
            if (JPService.getProperty(JPResetDB.class).getValue()) {
                return;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
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
                FileSynchronizer<ENTRY> fileSynchronizer = new FileSynchronizer<>(file, fileProcessor);
                ENTRY entry = fileSynchronizer.getData();
                fileSynchronizerMap.put(entry.getId(), fileSynchronizer);
                super.load(entry);
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }

        logger.info("====== " + size() + " entries successfully loaded. " + MultiException.size(exceptionStack) + " skipped. ======");

        MultiException.checkAndThrow("Could not load all registry entries!", exceptionStack);

        // register and apply db version specific consistency handler
        if (versionControl != null) {

            List<ConsistencyHandler> versionConsistencyHandlers = versionControl.loadDBVersionConsistencyHandlers();

            for (ConsistencyHandler handler : versionConsistencyHandlers) {
                try {
                    registerConsistencyHandler(handler);
                    versionControl.registerConsistencyHandlerExecution(handler);
                } catch (CouldNotPerformException ex) {
                    throw new CouldNotPerformException("FATAL ERROR: During VersionConsistencyHandler[" + handler.getClass().getSimpleName() + "] execution!", ex);
                }
            }
        }

        notifyObservers();
    }

    @Override
    public void saveRegistry() throws MultiException {
        logger.info("Save registry into " + databaseDirectory + "...");
        ExceptionStack exceptionStack = null;

        // save all changes.
        for (FileSynchronizer<ENTRY> fileSynchronizer : fileSynchronizerMap.values()) {
            try {
                fileSynchronizer.save();
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }

        // verify and apply file name changes
        String generatedFileName;
        FileSynchronizer<ENTRY> fileSynchronizer;
        FileSynchronizer<ENTRY> newFileSynchronizer;
        File newFile;

        for (Entry<KEY, FileSynchronizer<ENTRY>> entry : fileSynchronizerMap.entrySet()) {
            fileSynchronizer = entry.getValue();
            try {
                generatedFileName = fileProvider.getFileName(fileSynchronizer.getData());
                if (!fileSynchronizer.getFile().getName().equals(generatedFileName)) {
                    try {
                        // rename file
                        newFile = new File(fileSynchronizer.getFile().getParent(), generatedFileName);
                        if (!fileSynchronizer.getFile().renameTo(newFile)) {
                            throw new CouldNotPerformException("Rename failed without explicit error code, please rename file manually after registry shutdown!");
                        }
                        newFileSynchronizer = new FileSynchronizer<>(fileSynchronizer.getData(), newFile, FileSynchronizer.InitMode.AUTO, fileProcessor);
                        fileSynchronizerMap.replace(entry.getKey(), fileSynchronizer, newFileSynchronizer);
                    } catch (CouldNotPerformException ex) {
                        exceptionStack = MultiException.push(this, new CouldNotPerformException("Could not apply db Entry[" + fileSynchronizer.getFile().getName() + "] renaming to Entry[" + generatedFileName + "]!", ex), exceptionStack);
                    }
                }
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, new CouldNotPerformException("Could not reconstruct filename of db Entry[" + fileSynchronizer.getFile().getName() + "]!", ex), exceptionStack);
            }
        }

        MultiException.checkAndThrow("Could not save all registry entries!", exceptionStack);
    }

    @Override
    public void checkAccess() throws RejectedException {
        super.checkAccess();

        try {
            if (!databaseDirectory.canWrite() && !JPService.getProperty(JPTestMode.class).getValue()) {
                throw new RejectedException("DatabaseDirectory[" + databaseDirectory.getAbsolutePath() + "] not writable!");
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }
    }

    @Override
    public void shutdown() {
        try {
            saveRegistry();
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Final save failed!", ex), logger);
        }

        fileSynchronizerMap.clear();
        super.shutdown();
    }

    public File getDatabaseDirectory() {
        return databaseDirectory;
    }

    @Override
    public Integer getDBVersion() {
        return versionControl.getLatestDBVersion();
    }
}
