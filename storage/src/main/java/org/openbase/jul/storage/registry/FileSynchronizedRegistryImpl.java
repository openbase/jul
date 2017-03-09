package org.openbase.jul.storage.registry;

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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.preset.JPForce;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.RejectedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.processing.FileProcessor;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.jul.storage.file.FileProvider;
import org.openbase.jul.storage.file.FileSynchronizer;
import org.openbase.jul.storage.registry.jp.JPResetDB;
import org.openbase.jul.storage.registry.plugin.FileRegistryPlugin;
import org.openbase.jul.storage.registry.plugin.FileRegistryPluginPool;
import org.openbase.jul.storage.registry.version.DBVersionControl;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <R>
 */
public class FileSynchronizedRegistryImpl<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends FileSynchronizedRegistry<KEY, ENTRY>> extends AbstractRegistry<KEY, ENTRY, MAP, R, FileRegistryPlugin<KEY, ENTRY>> implements FileSynchronizedRegistry<KEY, ENTRY> {

    public enum DatabaseState {

        UNKNOWN,
        OUTDATED,
        LATEST;
    }

    private final File databaseDirectory;
    private final Map<KEY, FileSynchronizer<ENTRY>> fileSynchronizerMap;
    private final FileProcessor<ENTRY> fileProcessor;
    private final FileProvider<Identifiable<KEY>> fileProvider;
    private final FileRegistryPluginPool<KEY, ENTRY, FileRegistryPlugin<KEY, ENTRY>> filePluginPool;
    private DBVersionControl versionControl;
    private DatabaseState databaseState;
    private final String databaseName;

    public FileSynchronizedRegistryImpl(final MAP entryMap, final File databaseDirectory, final FileProcessor<ENTRY> fileProcessor, final FileProvider<Identifiable<KEY>> fileProvider) throws InstantiationException, InterruptedException {
        this(entryMap, databaseDirectory, fileProcessor, fileProvider, new FileRegistryPluginPool<>());
    }

    public FileSynchronizedRegistryImpl(final MAP entryMap, final File databaseDirectory, final FileProcessor<ENTRY> fileProcessor, final FileProvider<Identifiable<KEY>> fileProvider, final FileRegistryPluginPool<KEY, ENTRY, FileRegistryPlugin<KEY, ENTRY>> filePluginPool) throws InstantiationException, InterruptedException {
        super(entryMap, filePluginPool);
        try {
            this.databaseDirectory = databaseDirectory;
            this.fileSynchronizerMap = new HashMap<>();
            this.fileProcessor = fileProcessor;
            this.fileProvider = fileProvider;
            this.filePluginPool = filePluginPool;
            this.databaseState = DatabaseState.UNKNOWN;
            this.databaseName = generateDatabaseName(databaseDirectory);
        } catch (NullPointerException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    private String generateDatabaseName(final File databaseDirectory) {
        return StringProcessor.transformToCamelCase(databaseDirectory.getName().replaceAll("db", "").replaceAll("DB", ""));
    }

    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * This method activates the version control unit of the underlying registry
     * db. The version check and db upgrade is automatically performed during
     * the registry db loading phrase. The db will be upgraded to the latest db
     * format provided by the given converter package. The converter package
     * should contain only classes implementing the DBVersionConverter
     * interface. To fully support outdated db upgrade make sure that the
     * converter pipeline covers the whole version range!
     *
     * Activate version control before loading the registry. Please provide
     * within the converter package only converter with the naming structure
     * [$(EntryType)_$(VersionN)_To_$(VersionN+1)_DBConverter].
     *
     * Example:
     *
     * converter package myproject.db.converter containing the converter
     * pipeline
     *
     * myproject.db.converter.DeviceConfig_0_To_1_DBConverter.class
     * myproject.db.converter.DeviceConfig_1_To_2_DBConverter.class
     * myproject.db.converter.DeviceConfig_2_To_3_DBConverter.class
     *
     * Would support the db upgrade from version 0 till the latest db version 3.
     *
     * @param entryType
     * @param converterPackage the package containing all converter which
     * provides db entry updates from the first to the latest db version.
     * @throws CouldNotPerformException in case of an invalid converter pipeline
     * or initialization issues.
     */
    public void activateVersionControl(final String entryType, final Package converterPackage) throws CouldNotPerformException {
        if (!isEmpty()) {
            throw new CouldNotPerformException("Could not activate version control because registry already loaded! Please activate version control before loading the registry.");
        }
        versionControl = new DBVersionControl(entryType, fileProvider, converterPackage, databaseDirectory, this);
    }

    @Override
    public ENTRY register(final ENTRY entry) throws CouldNotPerformException {
        ENTRY result = super.register(entry);
        FileSynchronizer<ENTRY> fileSynchronizer = new FileSynchronizer<>(result, new File(databaseDirectory, fileProvider.getFileName(entry)), FileSynchronizer.InitMode.CREATE, fileProcessor);
        fileSynchronizerMap.put(result.getId(), fileSynchronizer);
        filePluginPool.afterRegister(result, fileSynchronizer);

        return result;
    }

    @Override
    public ENTRY update(final ENTRY entry) throws CouldNotPerformException {
        ENTRY result = super.update(entry);

        // ignore update during registration process.
        if (!fileSynchronizerMap.containsKey(result.getId())) {
            logger.debug("Ignore update during registration process of entry " + result);
            return entry;
        }

        FileSynchronizer<ENTRY> fileSynchronizer = fileSynchronizerMap.get(result.getId());

        filePluginPool.beforeUpdate(result, fileSynchronizer);
        fileSynchronizer.save(result);
        filePluginPool.afterUpdate(result, fileSynchronizer);

        return result;
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
    protected void afterConsistencyCheck() throws CouldNotPerformException {
        super.afterConsistencyCheck();
        saveRegistry();
    }

    @Override
    public void loadRegistry() throws CouldNotPerformException {
        assert databaseDirectory != null;

        // check db version
        if (versionControl != null) {
            try {
                versionControl.validateAndUpgradeDBVersion();
                databaseState = DatabaseState.LATEST;
            } catch (CouldNotPerformException ex) {
                databaseState = DatabaseState.OUTDATED;
                try {
                    if (!JPService.getProperty(JPForce.class).getValue()) {
                        throw new CouldNotPerformException("Registry is not up-to-date! To fix registry manually start the registry in force mode", ex);
                    }
                } catch (JPServiceException exx) {
                    ExceptionPrinter.printHistory("Could not check force flag!", exx, logger);
                }
                ExceptionPrinter.printHistory(new CouldNotPerformException("Registry is not up-to-date but force mode is enabled so you are able to apply manual fixes via the registry editor.", ex), logger);
            }
        }

        try {
            if (JPService.getProperty(JPResetDB.class).getValue()) {
                return;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        logger.debug("Load " + this + " out of " + databaseDirectory + "...");
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

        if (!isEmpty() || MultiException.size(exceptionStack) > 0) {
            logger.info("====== " + size() + (size() == 1 ? " entry" : " entries") + " of " + this + " successfully loaded." + (MultiException.size(exceptionStack) > 0 ? MultiException.size(exceptionStack) + " skipped." : "") + " ======");
        }

        MultiException.checkAndThrow("Could not load all registry entries!", exceptionStack);

        // register and apply db version specific consistency handler
        if (versionControl != null) {

            List<ConsistencyHandler> versionConsistencyHandlers = versionControl.loadDBVersionConsistencyHandlers(this);

            for (ConsistencyHandler handler : versionConsistencyHandlers) {
                try {
                    registerConsistencyHandler(handler);
                } catch (CouldNotPerformException ex) {
                    throw new FatalImplementationErrorException("During VersionConsistencyHandler[" + handler.getClass().getSimpleName() + "] execution!", this, ex);
                }
            }
        }

        notifyObservers();
    }

    @Override
    public synchronized void saveRegistry() throws MultiException {

        if (JPService.testMode()) {
            return;
        }

        try {
            if (!JPService.getProperty(JPForce.class).getValue() && isReadOnly()) {
                logger.warn("Skipping save of Registry[" + getName() + "] because its read only.");
                return;
            }
        } catch (JPNotAvailableException ex) {
            logger.error("Could not check JPFoceProperty!", ex);
            if (isReadOnly()) {
                logger.warn("Skipping save of Registry[" + getName() + "] because its read only.");
                return;
            }
        }

        logger.debug("Save " + this + " into " + databaseDirectory + "...");
        ExceptionStack exceptionStack = null;

        // save all changes.
        for (FileSynchronizer<ENTRY> fileSynchronizer : new ArrayList<>(fileSynchronizerMap.values())) {
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

        for (Entry<KEY, FileSynchronizer<ENTRY>> entry : new ArrayList<>(fileSynchronizerMap.entrySet())) {
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
    public void checkWriteAccess() throws RejectedException {
        try {
            if (JPService.getProperty(JPForce.class).getValue()) { // || JPService.getProperty(JPTestMode.class).getValue()
                return;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        super.checkWriteAccess();

        if (isOutdated()) {
            throw new RejectedException("Database[" + databaseDirectory.getAbsolutePath() + "] is outdated!");
        }

        if (!databaseDirectory.canWrite()) {
            throw new RejectedException("DatabaseDirectory[" + databaseDirectory.getAbsolutePath() + "] not writable!");
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

    @Override
    public boolean isConsistent() {
        return super.isConsistent() && !isOutdated();
    }

    public boolean isOutdated() {
        return databaseState == DatabaseState.OUTDATED;
    }

    public DatabaseState getDatabaseState() {
        return databaseState;
    }
}
