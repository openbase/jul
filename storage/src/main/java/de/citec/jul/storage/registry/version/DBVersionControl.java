package de.citec.jul.storage.registry.version;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import de.citec.jps.core.JPService;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.storage.file.FileProvider;
import de.citec.jul.storage.registry.jp.JPInitializeDB;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
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
        int currenVersion = detectCurrendDBVersion(db);
        int latestVersion = getLatestDBVersion();

        if (currenVersion == latestVersion) {
            logger.info("Database["+db.getName()+"] is up-to-date.");
            return;
        } else if (currenVersion > latestVersion) {
            throw new InvalidStateException("DB Version[" + currenVersion + "] is newer than the latest supported version[" + latestVersion + "]!");
        }

        upgradeDB(currenVersion, latestVersion, db);
    }

    public void upgradeDB(final int currentVersion, final int targetVersion, final File db) throws CouldNotPerformException {
        try {
            logger.info("Upgrade Database["+db.getName()+"] from current Version["+currentVersion+"] to target Version["+targetVersion+"]...");
            final List<DBVersionConverter> currentToTargetConverterPipeline = getDBConverterPipeline(currentVersion, latestDBVersion);
            for (File entry : db.listFiles(entryFileProvider.getFileFilter())) {
                upgradeDBEntry(entry, currentVersion, targetVersion, currentToTargetConverterPipeline);
            }
            upgrateCurrendDBVersion(db);
        } catch (CouldNotPerformException ex) {
            if (targetVersion == latestDBVersion) {
                throw new CouldNotPerformException("Could not upgrade Database[" + db.getAbsolutePath() + "] to latest version[" + targetVersion + "]!", ex);
            } else {
                throw new CouldNotPerformException("Could not upgrade Database[" + db.getAbsolutePath() + "] to version[" + targetVersion + "]!", ex);
            }
        }

    }

    public void upgradeDBEntry(final File entry, final int currentVersion, final int targetVersion, List<DBVersionConverter> converterPipeline) throws CouldNotPerformException {
        try {

            JsonObject jSonEntry;
            String entryAsString;

            // load entry
            try {
                entryAsString = FileUtils.readFileToString(entry, "UTF-8");

                JsonReader jsonReader = new JsonReader(new StringReader(entryAsString));

                // needed to handle protobuf generated malformed json code.
                jsonReader.setLenient(true);

                jSonEntry = new JsonParser().parse(jsonReader).getAsJsonObject();
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not load entry!", ex);
            }

            // upgrade
            for (DBVersionConverter converter : converterPipeline) {
                try {
                    jSonEntry = converter.upgrade(jSonEntry);
                } catch (Exception ex) {
                    throw new CouldNotPerformException("Could not upgrade entry with Converter[" + converter.getClass().getSimpleName() + "]!", ex);
                }
                entryAsString = jSonEntry.toString();
            }

            // format
            try {
                JsonElement el = parser.parse(entryAsString);
                entryAsString = gson.toJson(el);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not format entry!", ex);
            }

            // store
            try {
                FileUtils.writeStringToFile(entry, entryAsString, "UTF-8");
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not store entry!", ex);
            }

        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not upgrade db Entry[" + entry.getAbsolutePath() + "]", ex);
        }
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
                upgrateCurrendDBVersion(db);
            }

            // load db version
            try {
                String versionAsString = FileUtils.readFileToString(versionFile, "UTF-8");
                versionAsString = versionAsString.replace(VERSION_FILE_WARNING, "");
                JsonObject versionJsonObject = new JsonParser().parse(versionAsString).getAsJsonObject();
                return versionJsonObject.get(VERSION_FIELD).getAsInt();
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not load Field[" + VERSION_FIELD + "]!", ex);
            }
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not detect db version of Database[" + db.getName() + "]!", ex);
        }
    }

    /**
     * Method upgrades the current db version to the latest db version.
     *
     * @param db
     * @throws CouldNotPerformException
     */
    public void upgrateCurrendDBVersion(final File db) throws CouldNotPerformException {
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

        } catch (Exception ex) {
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
                    converterClassName = converterPackage.getName() + "." +  entryType + "_" + version + "_To_" + (version + 1) + "_DBConverter";
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
        } catch (Exception ex) {
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
