package org.openbase.jul.storage.registry.version;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public abstract class AbstractGlobalDbVersionConverter extends AbstractDBVersionConverter implements DBVersionConverter {

    public AbstractGlobalDbVersionConverter(DBVersionControl versionControl) {
        super(versionControl);
    }

}
