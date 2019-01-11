package org.openbase.jul.processing;

/*-
 * #%L
 * JUL Processing
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import org.openbase.jul.exception.NotAvailableException;

import java.io.InputStream;
import java.util.Properties;

public class VersionProcessor {

    /**
     * Method tries to detect the application Version.
     * @param launcherClass the launcher to extract the class path.
     * @param groupId the group id of the application.
     * @param artifactId the artifact id of the application.
     * @return the semantic version of the application is returned.
     * @throws NotAvailableException is thrown if the version could not be detected.
     */
    private String getVersion(final Class launcherClass, final String groupId, final String artifactId) throws NotAvailableException {
        String version = null;

        // try to load from maven properties first
        try {
            Properties p = new Properties();
            InputStream is = getClass().getResourceAsStream("/META-INF/maven/"+groupId+"/"+artifactId+"/pom.properties");
            if (is != null) {
                p.load(is);
                version = p.getProperty("version", "");
            }
        } catch (Exception e) {
            // ignore
        }

        // fallback to using Java API
        if (version == null) {
            Package launcherPackage = launcherClass.getClass().getPackage();
            if (launcherPackage != null) {
                version = launcherPackage.getImplementationVersion();
                if (version == null) {
                    version = launcherPackage.getSpecificationVersion();
                }
            }
        }

        if (version == null) {
            // we could not compute the version so  a blank
            throw new NotAvailableException("Application Version");
        }

        return version;
    }
}
