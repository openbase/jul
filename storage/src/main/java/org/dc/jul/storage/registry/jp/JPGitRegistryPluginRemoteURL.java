/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry.jp;

import org.dc.jps.preset.AbstractJPMapStringString;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class JPGitRegistryPluginRemoteURL extends AbstractJPMapStringString {

    public static final String[] COMMAND_IDENTIFIERS = {"--git-remote-url"};

    public JPGitRegistryPluginRemoteURL() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    public String getDescription() {
        return "Activates the git registry plugin to support database versioning and use the given remote urls as origin repository.";
    }
}
