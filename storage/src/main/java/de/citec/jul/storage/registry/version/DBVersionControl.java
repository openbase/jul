package de.citec.jul.storage.registry.version;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import de.citec.jps.core.JPService;
import de.citec.jps.exception.JPServiceRuntimeException;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.storage.file.FileProvider;
import de.citec.jul.storage.registry.jp.JPInitializeDB;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class DBVersionControl {

    public static final String VERSION_FILE_NAME = ".db-version";
    public static final String VERSION_FIELD = "version";
    public static final String VERSION_FILE_WARNING = "### PLEASE DO NOT MODIFY ###\n";

    protected final Logger logger = LoggerFactory.getLogger(DBVersionControl.class);
    private final JsonParser parser;
    private final Gson gson;
    private int latestDBVersion;
    private final Package converterPackage;
    private final List<DBVersionConverter> converterPipeline;
    private final FileProvider entryFileProvider;
    final String entryType;

    public DBVersionControl(final String entryType, final FileProvider entryFileProvider, final Package converterPackage) throws InstantiationException {
        try {
            this.entryType = entryType;
            this.gson = new GsonBuilder().setPrettyPrinting().create();
            this.parser = new JsonParser();
            this.converterPackage = converterPackage;
            this.entryFileProvider = entryFileProvider;
            this.converterPipeline = loadDBConverterPipelineAndDetectLatestVersion(converterPackage);
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    public void validateAndUpgradeDBVersion(final File db) throws CouldNotPerformException {
        int currentVersion = detectCurrendDBVersion(db);
        int latestVersion = getLatestDBVersion();

        if (currentVersion == latestVersion) {
            logger.info("Database[" + db.getName() + "] is up-to-date.");
            return;
        } else if (currentVersion > latestVersion) {
            throw new InvalidStateException("DB Version[" + currentVersion + "] is newer than the latest supported version[" + latestVersion + "]!");
        }

        upgradeDB(currentVersion, latestVersion, db);
    }

    public void upgradeDB(final int currentVersion, final int targetVersion, final File db) throws CouldNotPerformException {
        try {
            logger.info("Upgrade Database[" + db.getName() + "] from current Version[" + currentVersion + "] to target Version[" + targetVersion + "]...");
            
            // init
            Map<File, JsonObject> dbSnapshot;
            final List<DBVersionConverter> currentToTargetConverterPipeline = getDBConverterPipeline(currentVersion, latestDBVersion);

            // load db entries
            final HashMap<File, JsonObject> dbFileEntryMap = new HashMap<>();
            for (File entry : db.listFiles(entryFileProvider.getFileFilter())) {
                dbFileEntryMap.put(entry, loadDBEntry(entry));
            }
            
            // upgrade db entries
            for (DBVersionConverter converter : currentToTargetConverterPipeline) {
                for (Entry<File, JsonObject> dbEntry : dbFileEntryMap.entrySet()) {
                    // update converted entry
                    dbFileEntryMap.replace(dbEntry.getKey(), dbEntry.getValue(), upgradeDBEntry(dbEntry.getValue(), converter, dbFileEntryMap));
                }
            }

            // format and store
            for (Entry<File, JsonObject> dbEntry : dbFileEntryMap.entrySet()) {
                storeEntry(formatEntryToHumanReadableString(dbEntry.getValue()), dbEntry.getKey());
            }
            
            // upgrade db version
            upgradeCurrentDBVersion(db);
            
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not upgrade Database[" + db.getAbsolutePath() + "] to" + (targetVersion == latestDBVersion ? " latest" : "") + " version[" + targetVersion + "]!", ex);
        }
    }

    public JsonObject upgradeDBEntry(final JsonObject entry, final DBVersionConverter converter, final Map<File, JsonObject> dbSnapshot) throws CouldNotPerformException {
        try {
            // upgrade
            return converter.upgrade(entry, dbSnapshot);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not upgrade entry with Converter[" + converter.getClass().getSimpleName() + "]!", ex);
        }
    }

    /**
     *
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
        } catch (IOException | JsonIOException | JsonSyntaxException ex) {
            throw new CouldNotPerformException("Could not load entry!", ex);
        }
        return jSonEntry;
    }

    public int getLatestDBVersion() {
        return latestDBVersion;
    }

    /**
     * Method detects the current db version and returns the version number as integer.
     *
     * @param db
     * @return
     */
    public int detectCurrendDBVersion(final File db) throws CouldNotPerformException {
        try {
            // detect file
            File versionFile = new File(db, VERSION_FILE_NAME);

            if (!versionFile.exists()) {
                if (!JPService.getProperty(JPInitializeDB.class).getValue()) {
                    throw new CouldNotPerformException("No version information available!");
                }
                upgradeCurrentDBVersion(db);
            }

            // load db version
            try {
                String versionAsString = FileUtils.readFileToString(versionFile, "UTF-8");
                versionAsString = versionAsString.replace(VERSION_FILE_WARNING, "");
                JsonObject versionJsonObject = new JsonParser().parse(versionAsString).getAsJsonObject();
                return versionJsonObject.get(VERSION_FIELD).getAsInt();
            } catch (IOException | JsonSyntaxException ex) {
                throw new CouldNotPerformException("Could not load Field[" + VERSION_FIELD + "]!", ex);
            }
        } catch (JPServiceRuntimeException | CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not detect db version of Database[" + db.getName() + "]!", ex);
        }
    }

    /**
     * Method upgrades the current db version to the latest db version.
     *
     * @param db
     * @throws CouldNotPerformException
     */
    public void upgradeCurrentDBVersion(final File db) throws CouldNotPerformException {
        try {

            // detect or create version file
            File versionFile = new File(db, VERSION_FILE_NAME);
            if (!versionFile.exists()) {
                if (!versionFile.createNewFile()) {
                    throw new CouldNotPerformException("Could not create db version file!");
                }
            }

            // upgrade version
            try {
                JsonObject versionJsonObject = new JsonObject();
                versionJsonObject.addProperty(VERSION_FIELD, latestDBVersion);
                FileUtils.writeStringToFile(versionFile, VERSION_FILE_WARNING + versionJsonObject.toString(), "UTF-8");
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not write Field[" + VERSION_FIELD + "]!", ex);
            }

        } catch (IOException | CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not upgrade current db version of Database[" + db.getName() + "]!", ex);
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

        int version = 0;
        String converterClassName = "";
        Class<DBVersionConverter> converterClass;
        try {
            while (true) {
                try {
                    converterClassName = converterPackage.getName() + "." + entryType + "_" + version + "_To_" + (version + 1) + "_DBConverter";
                    converterClass = (Class<DBVersionConverter>) Class.forName(converterClassName);
                    version++;
                } catch (ClassNotFoundException ex) {
                    logger.debug("Could not load Converter[" + converterClassName + "] so latest db version should be " + version + ".", ex);
                    break;
                }
                converterList.add(converterClass.newInstance());
            }
            latestDBVersion = version;
            return converterList;
        } catch (java.lang.InstantiationException | IllegalAccessException ex) {
            throw new CouldNotPerformException("Could not load converter db pipeline of Package[" + converterPackage.getName() + "]!", ex);
        }
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
}
