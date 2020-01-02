package org.openbase.jul.storage.registry.version;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.storage.file.FileProvider;
import org.openbase.jul.storage.registry.FileSynchronizedRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class DBVersionControl {

    public static final String VERSION_FILE_NAME = ".db-version";
    public static final String VERSION_FIELD = "version";
    public static final String DB_CONVERTER_PACKAGE_NAME = "dbconvert";
    public static final String VERSION_FILE_WARNING = "### PLEASE DO NOT MODIFY ###\n";

    private final Logger logger = LoggerFactory.getLogger(DBVersionControl.class);
    private final JsonParser parser;
    private final Gson gson;
    private final Package converterPackage;
    private final List<DBVersionConverter> converterPipeline;
    private final FileProvider entryFileProvider;
    private final String entryType;
    private final File databaseDirectory;
    private final FileSynchronizedRegistry registry;
    private final List<File> globalDatabaseDirectories;
    private final int latestSupportedDBVersion;
    private int currentDBVersion;

    public DBVersionControl(final String entryType, final FileProvider entryFileProvider, final Package converterPackage, final File databaseDirectory, final FileSynchronizedRegistry registry) throws InstantiationException {
        try {
            this.registry = registry;
            this.entryType = entryType;
            this.gson = new GsonBuilder().setPrettyPrinting().create();
            this.parser = new JsonParser();
            this.currentDBVersion = -1;
            this.converterPackage = converterPackage;
            this.entryFileProvider = entryFileProvider;
            this.converterPipeline = loadDBConverterPipelineAndDetectLatestVersion(converterPackage);
            this.latestSupportedDBVersion = converterPipeline.size();
            this.databaseDirectory = databaseDirectory;
            this.globalDatabaseDirectories = detectNeighbourDatabaseDirectories(databaseDirectory);
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    public void validateAndUpgradeDBVersion() throws CouldNotPerformException {

        // sync with remote db if registry is located externally.
        if(!registry.isLocalRegistry() && !JPService.testMode()) {
            try {
                GitVersionControl.syncWithRemoteDatabase(latestSupportedDBVersion, registry);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
            }
        }

        // detect current db version
        currentDBVersion = detectCurrentDBVersion();

        // check if upgrade is necessary
        if (currentDBVersion == latestSupportedDBVersion) {
            logger.debug("Database[" + databaseDirectory.getName() + "] is up-to-date.");
            return;
        } else if (currentDBVersion > latestSupportedDBVersion) {
            throw new InvalidStateException("DB Version[" + currentDBVersion + "] is newer than the latest supported Version[" + latestSupportedDBVersion + "]!");
        }

        // if current version is still unknown, we are not able to upgrade.
        if(currentDBVersion == -1) {
            throw new InvalidStateException("Current db version is unknown!");
        }

        // upgrade
        upgradeDB(currentDBVersion, latestSupportedDBVersion);
    }

    public void upgradeDB(final int currentVersion, final int targetVersion) throws CouldNotPerformException {
        try {
            logger.info("Upgrade Database[" + databaseDirectory.getName() + "] from current Version[" + currentVersion + "] to target Version[" + targetVersion + "]...");

            // init
            Map<String, Map<File, DatabaseEntryDescriptor>> globalDbSnapshots = null;
            final Map<String, Set<File>> globalKeySet = new HashMap<>();
            final List<DBVersionConverter> currentToTargetConverterPipeline = getDBConverterPipeline(currentVersion, latestSupportedDBVersion);

            int versionOfCurrentTransaction = currentVersion;

            // check if upgrade is needed and write access is permitted.
            if (!currentToTargetConverterPipeline.isEmpty()) {
                registry.checkWriteAccess();
            }

            // load db entries
            final Map<File, JsonObject> dbFileEntryMap = loadDbSnapshot();

            // upgrade db entries
            for (DBVersionConverter converter : currentToTargetConverterPipeline) {

                // calculate current transaction version
                versionOfCurrentTransaction++;

                // load global dbs if needed
                if (globalDbSnapshots == null && converter instanceof GlobalDBVersionConverter) {
                    globalDbSnapshots = loadGlobalDBSnapshots();
                    for (Entry<String, Map<File, DatabaseEntryDescriptor>> entry : globalDbSnapshots.entrySet()) {
                        globalKeySet.put(entry.getKey(), new HashSet<>(entry.getValue().keySet()));
                    }
                }

                for (Entry<File, JsonObject> dbEntry : new HashSet<>(dbFileEntryMap.entrySet())) {
                    // update converted entry
                    final JsonObject jsonObject = upgradeDBEntry(dbEntry.getValue(), converter, dbFileEntryMap, globalDbSnapshots);

                    // entry was not converted but removed
                    if (!dbFileEntryMap.values().contains(jsonObject)) {
                        if (!dbEntry.getKey().delete()) {
                            throw new CouldNotPerformException("Could not remove database entry[" + dbEntry + "]");
                        }
                        dbFileEntryMap.remove(dbEntry.getKey());
                        continue;
                    }

                    dbFileEntryMap.replace(dbEntry.getKey(), dbEntry.getValue(), jsonObject);
                }

                // update current db version related to performed transactions.
                currentDBVersion = versionOfCurrentTransaction;

                // format and store
                storeDBSnapshot(dbFileEntryMap);

                // format and store global db if changed
                storeGlobalDBSnapshots(globalDbSnapshots, globalKeySet);

                // upgrade db version
                syncCurrentDBVersionWithFilesystem();
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not upgrade Database[" + databaseDirectory.getAbsolutePath() + "] to" + (targetVersion == latestSupportedDBVersion ? " latest" : "") + " Version[" + targetVersion + "]!", ex);
        }
    }

    public JsonObject upgradeDBEntry(final JsonObject entry, final DBVersionConverter converter, final Map<File, JsonObject> dbSnapshot, Map<String, Map<File, DatabaseEntryDescriptor>> globalDbSnapshots) throws CouldNotPerformException {
        try {
            // upgrade
            if (converter instanceof GlobalDBVersionConverter) {
                return ((GlobalDBVersionConverter) converter).upgrade(entry, dbSnapshot, globalDbSnapshots);
            } else {
                return converter.upgrade(entry, dbSnapshot);
            }
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not upgrade entry with Converter[" + converter.getClass().getSimpleName() + "]!", ex);
        }
    }

    private Map<File, JsonObject> loadDbSnapshot() throws CouldNotPerformException {
        final HashMap<File, JsonObject> dbFileEntryMap = new HashMap<>();
        for (File entry : databaseDirectory.listFiles(entryFileProvider.getFileFilter())) {
            dbFileEntryMap.put(entry, loadDBEntry(entry));
        }
        return dbFileEntryMap;
    }

    private Map<File, DatabaseEntryDescriptor> loadDbSnapshotAsDBEntryDescriptors(final File globalDatabaseDirectory) throws CouldNotPerformException {
        final HashMap<File, DatabaseEntryDescriptor> dbFileEntryMap = new HashMap<>();
        for (File entry : globalDatabaseDirectory.listFiles(entryFileProvider.getFileFilter())) {
            dbFileEntryMap.put(entry, new DatabaseEntryDescriptor(loadDBEntry(entry), entry, detectCurrentDBVersion(globalDatabaseDirectory), globalDatabaseDirectory));
        }
        return dbFileEntryMap;
    }

    private Map<String, Map<File, DatabaseEntryDescriptor>> loadGlobalDBSnapshots() {
        Map<String, Map<File, DatabaseEntryDescriptor>> globalDbSnapshotMap = new HashMap<>();
        for (File globalDatabaseDirectory : globalDatabaseDirectories) {
            try {
                logger.warn("Test directory [" + globalDatabaseDirectory + "]");
                if (!FileUtils.isSymlink(globalDatabaseDirectory)) {
                    if (globalDatabaseDirectory.canWrite()) {
                        globalDbSnapshotMap.put(globalDatabaseDirectory.getName(), loadDbSnapshotAsDBEntryDescriptors(globalDatabaseDirectory));
                    } else {
                        logger.warn("Skip loading of global Database[" + globalDatabaseDirectory.getAbsolutePath() + "] because directory is write protected!");
                    }
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not load db entries out of " + globalDatabaseDirectory.getAbsolutePath(), ex, logger);
            } catch (IOException ex) {
                ExceptionPrinter.printHistory("Could not check wether [" + globalDatabaseDirectory.getName() + "] is a symlink!", ex, logger);
            }
        }
        return globalDbSnapshotMap;
    }

    private void storeDBSnapshot(final Map<File, JsonObject> dbFileEntryMap) throws CouldNotPerformException {
        try {
            for (Entry<File, JsonObject> dbEntry : dbFileEntryMap.entrySet()) {
                storeEntry(formatEntryToHumanReadableString(dbEntry.getValue()), dbEntry.getKey());
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not store db snapshot!", ex);
        }
    }

    private void storeDBSnapshotASDBEntryDescriptors(final Map<File, DatabaseEntryDescriptor> dbFileEntryMap) throws CouldNotPerformException {
        try {
            for (Entry<File, DatabaseEntryDescriptor> dbEntry : dbFileEntryMap.entrySet()) {
                storeEntry(formatEntryToHumanReadableString(dbEntry.getValue().getEntry()), dbEntry.getKey());
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not store db snapshot!", ex);
        }
    }

    private void storeGlobalDBSnapshots(final Map<String, Map<File, DatabaseEntryDescriptor>> globalDbSnapshots,
                                        final Map<String, Set<File>> globalKeySet) throws CouldNotPerformException {
        if (globalDbSnapshots == null) {
            // skip because globally databases were never loaded.
            return;
        }

        // store every entry in globalDBSnapshot
        for (Entry<String, Map<File, DatabaseEntryDescriptor>> entry : globalDbSnapshots.entrySet()) {
            storeDBSnapshotASDBEntryDescriptors(entry.getValue());
        }

        // remove all entries which have been removed
        for (final Entry<String, Set<File>> entry : globalKeySet.entrySet()) {
            for (File file : entry.getValue()) {
                if (!globalDbSnapshots.get(entry.getKey()).keySet().contains(file)) {
                    if (!file.delete()) {
                        throw new CouldNotPerformException("Could not delete file[" + file + "]");
                    }
                }
            }
        }
    }

    /**
     * @param jsonEntry
     * @return
     * @throws CouldNotPerformException
     */
    public String formatEntryToHumanReadableString(final JsonObject jsonEntry) throws CouldNotPerformException {

        String entryAsString;
        try {
            // format human readable
            entryAsString = jsonEntry.toString();
            JsonElement el = parser.parse(entryAsString);
            entryAsString = gson.toJson(el);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not format entry!", ex);
        }
        return entryAsString;
    }

    public void storeEntry(final String entryAsString, final File entryFile) throws CouldNotPerformException {
        try {
            FileUtils.writeStringToFile(entryFile, entryAsString, "UTF-8");
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not store entry!", ex);
        }
    }

    private JsonObject loadDBEntry(final File entry) throws CouldNotPerformException {
        JsonObject jSonEntry;

        // load entry
        try {
            JsonReader jsonReader = new JsonReader(new StringReader(FileUtils.readFileToString(entry, "UTF-8")));

            // needed to handle protobuf generated malformed json code.
            jsonReader.setLenient(true);

            jSonEntry = new JsonParser().parse(jsonReader).getAsJsonObject();
        } catch (IOException | JsonIOException | JsonSyntaxException | IllegalStateException ex) {
            throw new CouldNotPerformException("Could not load File[" + entry.getAbsolutePath() + "]!", ex);
        }
        return jSonEntry;
    }

    public int getLatestSupportedDBVersion() {
        return latestSupportedDBVersion;
    }

    /**
     * Method detects the current db version of the given database.
     *
     * @param databaseDirectory
     * @return the current db version as integer.
     * @throws CouldNotPerformException
     */
    public int detectCurrentDBVersion(final File databaseDirectory) throws CouldNotPerformException {
        try {

            // detect file
            File versionFile = new File(databaseDirectory, VERSION_FILE_NAME);

            if (!versionFile.exists()) {
                return getLatestSupportedDBVersion();
            }

            // load db version
            try {
                String versionAsString = FileUtils.readFileToString(versionFile, "UTF-8");
                versionAsString = versionAsString.replace(VERSION_FILE_WARNING, "");
                JsonObject versionJsonObject = new JsonParser().parse(versionAsString).getAsJsonObject();
                try {
                    return versionJsonObject.get(VERSION_FIELD).getAsInt();
                } catch (JsonSyntaxException ex) {
                    throw new CouldNotPerformException("Field[" + VERSION_FIELD + "] is missing!", ex);
                }
            } catch (IOException | JsonSyntaxException ex) {
                throw new CouldNotPerformException("Could not parse db version information!", ex);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not detect current db version of Database[" + databaseDirectory.getName() + "]!", ex);
        }
    }

    /**
     * Method detects the current db version and returns the version number as integer.
     *
     * @return
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public int detectCurrentDBVersion() throws CouldNotPerformException {
        try {
            // detect file
            File versionFile = new File(databaseDirectory, VERSION_FILE_NAME);

            // handle if version file is missing.
            if (!versionFile.exists()) {
                if(!registry.isLocalRegistry() && !JPService.testMode()) {
                    throw new InvalidStateException("Not synced with remote registry!");
                }

                // a vanilla db will always be on the latest supported version.
                if (databaseDirectory.list().length == 0) {
                    currentDBVersion = latestSupportedDBVersion;
                }
                syncCurrentDBVersionWithFilesystem();
            }

            // load db version
            try {
                String versionAsString = FileUtils.readFileToString(versionFile, "UTF-8");
                versionAsString = versionAsString.replace(VERSION_FILE_WARNING, "");
                JsonObject versionJsonObject = new JsonParser().parse(versionAsString).getAsJsonObject();

                // handle unknown version and map those on -1
                if(versionJsonObject.get(VERSION_FIELD).getAsString().equals("?")) {
                    return -1;
                }

                // read current version
                return versionJsonObject.get(VERSION_FIELD).getAsInt();
            } catch (IOException | JsonSyntaxException ex) {
                throw new CouldNotPerformException("Could not load Field[" + VERSION_FIELD + "]!", ex);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not detect db version of Database[" + databaseDirectory.getName() + "]!", ex);
        }
    }

    /**
     * Method upgrades the current db version to the latest db version.
     *
     * @throws CouldNotPerformException
     */
    public void syncCurrentDBVersionWithFilesystem() throws CouldNotPerformException {
        try {
            // detect or create version file
            File versionFile = new File(databaseDirectory, VERSION_FILE_NAME);
            String version = (currentDBVersion != -1 ? Integer.toString(currentDBVersion) : "?");
            if (!versionFile.exists()) {
                if (!versionFile.createNewFile()) {
                    throw new CouldNotPerformException("Could not create db version file!");
                }
                JsonObject versionJsonObject = new JsonObject();
                versionJsonObject.addProperty(VERSION_FIELD, version);
                FileUtils.writeStringToFile(versionFile, VERSION_FILE_WARNING + formatEntryToHumanReadableString(versionJsonObject), "UTF-8");
                return;
            }

            // upgrade version
            try {
                String versionAsString = FileUtils.readFileToString(versionFile, "UTF-8");
                versionAsString = versionAsString.replace(VERSION_FILE_WARNING, "");
                JsonObject versionJsonObject = new JsonParser().parse(versionAsString).getAsJsonObject();
                versionJsonObject.remove(VERSION_FIELD);
                versionJsonObject.addProperty(VERSION_FIELD, version);
                FileUtils.writeStringToFile(versionFile, VERSION_FILE_WARNING + formatEntryToHumanReadableString(versionJsonObject), "UTF-8");
            } catch (IOException | JsonSyntaxException | CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not write Field[" + VERSION_FIELD + "]!", ex);
            }

        } catch (IOException | CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not upgrade current db version of Database[" + databaseDirectory.getName() + "]!", ex);
        }
    }

    /**
     * Method returns a db entry converter pipeline which supports the db entry version conversion from the current to the target version.
     *
     * @param currentVersion
     * @param targetVersion
     * @return
     * @throws CouldNotPerformException
     */
    public List<DBVersionConverter> getDBConverterPipeline(final int currentVersion, final int targetVersion) throws CouldNotPerformException {
        return converterPipeline.subList(currentVersion, targetVersion);
    }

    private List<DBVersionConverter> loadDBConverterPipelineAndDetectLatestVersion(final Package converterPackage) throws CouldNotPerformException {
        List<DBVersionConverter> converterList = new ArrayList<>();

        String converterClassName = "";
        Class<DBVersionConverter> converterClass;
        Constructor<DBVersionConverter> converterConstructor;
        try {
            while (true) {
                final int version = converterList.size();
                try {
                    converterClassName = converterPackage.getName() + "." + entryType + "_" + version + "_To_" + (version + 1) + "_DBConverter";
                    converterClass = (Class<DBVersionConverter>) Class.forName(converterClassName);
                    converterConstructor = converterClass.getConstructor(DBVersionControl.class);
                    converterList.add(converterConstructor.newInstance(this));
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                    logger.debug("Could not load Converter[" + converterClassName + "] so latest db version should be " + version + ".", ex);
                    break;
                }
            }
            return converterList;
        } catch (java.lang.InstantiationException | IllegalAccessException ex) {
            throw new CouldNotPerformException("Could not load converter db pipeline of Package[" + converterPackage.getName() + "]!", ex);
        }
    }

    /**
     * Method detects all database directories which are on the same file level than the given database.
     *
     * @param databaseDirectory the database directory used for extracting the base directory
     * @return a list which all top level database directories excluding the currently handled database itself.
     * @throws CouldNotPerformException is thrown if the detection fails.
     */
    private List<File> detectNeighbourDatabaseDirectories(final File databaseDirectory) throws CouldNotPerformException {
        ArrayList<File> globalDatabaseDirectoryList = new ArrayList<>();
        FileFilter dbFilter = new DirectoryFileFilter() {

            @Override
            public boolean accept(File file) {
                return super.accept(file) && !file.equals(databaseDirectory);
            }
        };

        globalDatabaseDirectoryList.addAll(Arrays.asList(databaseDirectory.getParentFile().listFiles(dbFilter)));
        return globalDatabaseDirectoryList;
    }

    public Package getConverterPackage() {
        return converterPackage;
    }

    public List<DBVersionConverter> getConverterPipeline() {
        return converterPipeline;
    }

    public FileProvider getEntryFileProvider() {
        return entryFileProvider;
    }

    public File getDatabaseDirectory() {
        return databaseDirectory;
    }

}
