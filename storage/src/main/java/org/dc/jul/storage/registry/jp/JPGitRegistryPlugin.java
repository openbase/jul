/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry.jp;

import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPNotAvailableException;
import org.dc.jps.preset.AbstractJPBoolean;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class JPGitRegistryPlugin extends AbstractJPBoolean {

    public static final String[] COMMAND_IDENTIFIERS = {"--git-support"};

    public JPGitRegistryPlugin() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    public String getDescription() {
        return "Activates the git registry plugin to support database versioning.";
    }

    @Override
    public Boolean getDefaultValue() throws JPNotAvailableException {
        return !JPService.getProperty(JPGitRegistryPluginRemoteURL.class).getValue().isEmpty() || super.getDefaultValue();
    }

}
