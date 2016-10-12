package org.openbase.jul.storage.registry.version;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public abstract class AbstractDBVersionConverter implements DBVersionConverter {

    private final DBVersionControl versionControl;

    public AbstractDBVersionConverter(DBVersionControl versionControl) {
        this.versionControl = versionControl;
    }

    @Override
    public DBVersionControl getVersionControl() {
        return versionControl;
    }
}
