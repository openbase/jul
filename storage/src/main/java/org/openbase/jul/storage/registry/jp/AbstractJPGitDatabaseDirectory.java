package org.openbase.jul.storage.registry.jp;

/*-
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPValidationException;
import org.openbase.jps.preset.JPForce;
import org.openbase.jps.tools.FileHandler.ExistenceHandling;
import org.openbase.jul.exception.printer.ExceptionPrinter;

import java.io.IOException;

public abstract class AbstractJPGitDatabaseDirectory extends AbstractJPDatabaseDirectory {

    public AbstractJPGitDatabaseDirectory(final String[] commandIdentifier) {
        super(commandIdentifier);
        registerDependingProperty(JPForce.class);
    }

    @Override
    public void validate() throws JPValidationException {
        // this makes sure the db folder is created.
        super.validate();

        // skip git validation in test mode.
        if(JPService.testMode()) {
            return;
        }

        try(final Git git = Git.open(getValue())) {
            if (git.getRepository().getConfig().getString( "remote", "origin", "url" ).equals(getRepositoryURL())) {
                // git seems to be ok, so finish validation.
                return;
            }
        } catch (IOException ex) {
            // git seems not to be valid, so try to create / recover.

            if (getValue().list().length > 0) {
                if(JPService.getValue(JPRecoverDB.class, false)) {
                    logger.error("Invalid database detected at "+ getValue()+" try to recover...");
                    setExistenceHandling(ExistenceHandling.MustBeNew);
                    super.validate();
                } else {
                    ExceptionPrinter.printHistory("Database Folder[" + getValue() + "] does not contain a valid repository but already includes some files.",ex, logger);
                    logger.info("Please start bco in db recovery mode ("+JPRecoverDB.COMMAND_IDENTIFIERS[0]+") to reset the database to the latest compatible version.");
                    System.exit(24);
                }
            }
        }

        try (final Git git = Git.cloneRepository()
                    .setDirectory(getValue())
                    .setBare(false)
                    .setNoCheckout(true)
                    .setURI(getRepositoryURL()).call()) {
        } catch (TransportException ex) {
            // todo: some parts of the following code could be used to initially create the db from templates when the system is offline during initial startup.
//            // during tests the registry generation is skipped because the mock registry is handling the db initialization.
//            if (!JPService.testMode()) {
//                try {
//                    BCORegistryLoader.prepareRegistry(getValue());
//                } catch (CouldNotPerformException ex) {
//                    throw new JPValidationException(ex);
//                }
//            }
            throw new JPValidationException("Could not connect to remote repository! Maybe you are offline, but offline initial setup is not supported yet!", ex);
        } catch (GitAPIException ex) {
            throw new JPValidationException("Initial database setup failed!", ex);
        }
    }

    protected abstract String getRepositoryURL();
}

