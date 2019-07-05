package org.openbase.jul.storage.registry.version;

/*
 * #%L
 * JUL Storage
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

import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.storage.registry.FileSynchronizedRegistry;
import org.openbase.jul.storage.registry.jp.JPDeveloperMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

/**
 *
 * This tool class can be used to handle automated update and compatibility handling of external registry dbs.
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a> //
 */
public class GitVersionControl {

    public static final String MASTER_BRANCH_NAME = "master";
    public static final String MASTER_BRANCH_REMOTE_IDENTIFIER = "refs/remotes/origin/" + MASTER_BRANCH_NAME;
    public static final String MASTER_BRANCH_LOCAL_IDENTIFIER = "refs/heads/" + MASTER_BRANCH_NAME;
    public static final String RELEASE_BRANCH_NAME_PREFIX = "release-";
    public static final String RELEASE_BRANCH_REMOTE_PREFIX = "refs/remotes/origin/" + RELEASE_BRANCH_NAME_PREFIX;
    public static final String RELEASE_BRANCH_LOCAL_PREFIX = "refs/heads/" + RELEASE_BRANCH_NAME_PREFIX;

    private static final Logger logger = LoggerFactory.getLogger(GitVersionControl.class);

    public static void syncWithRemoteDatabase(final int latestSupportedDBVersion, final FileSynchronizedRegistry registry) throws CouldNotPerformException {

        try(final Git registryDBGit = Git.open(registry.getDatabaseDirectory())) {

            // reset current database state before triggering the remote sync if repo is valid.
            if (registryDBGit.getRepository() != null && registryDBGit.getRepository().getFullBranch() != null) {

                // handle custom db branches.
                final String currentBranchName = registryDBGit.getRepository().getFullBranch();
                if (!currentBranchName.equals(MASTER_BRANCH_LOCAL_IDENTIFIER) && !currentBranchName.startsWith(RELEASE_BRANCH_LOCAL_PREFIX)) {
                    if (registryDBGit.status().call().isClean()) {
                        // sync with remote repo
                        logger.warn("Custom " + registry + " branch "+currentBranchName+" detected, remote sync will be performed but db auto upgrade will be skipped...");
                        try {
                            if(!registryDBGit.pull().call().isSuccessful()) {
                                throw new CouldNotPerformException("Pull was not successful!");
                            }
                        } catch (GitAPIException | CouldNotPerformException ex) {
                            ExceptionPrinter.printHistory("Custom branch sync failed!", ex, logger, LogLevel.WARN);
                        }
                        return;
                    }
                    logger.warn("Modified custom " + registry + " branch detected, sync and auto db update will be skipped...");
                    return;
                }

                // clear db repo
                registryDBGit.reset().setMode(ResetType.HARD).call();
                registryDBGit.clean().setForce(true).call();
            } else {
                logger.info("Perform initial sync with remote database of "+ registry.getName()+ "...");
            }

            // sync branches with remote repo
            boolean offline = false;
            try {
                registryDBGit.fetch().call();
            } catch (GitAPIException ex) {
                final String errorMessage = "Could not sync with remote repository of " + registry.getName() + " and continue in offline mode...";
                if(JPService.verboseMode()) {
                    ExceptionPrinter.printHistory(errorMessage, ex, logger, LogLevel.WARN);
                } else {
                    logger.warn(errorMessage);
                }
                offline = true;
            }

            // checkout latest compatible database
            if(JPService.getValue(JPDeveloperMode.class, false)) {

                // lookup local branch
                boolean localBranchExist = false;
                for (Ref ref : registryDBGit.branchList().setListMode(ListMode.ALL).call()) {
                    if (ref.getName().equals(MASTER_BRANCH_LOCAL_IDENTIFIER)) {
                        localBranchExist = true;
                        break;
                    }
                }

                // checkout developer branch
                registryDBGit.checkout()
                        .setName(MASTER_BRANCH_NAME)
                        .setCreateBranch(!localBranchExist)
                        .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
                        .setStartPoint(MASTER_BRANCH_REMOTE_IDENTIFIER)
                        .call();
            } else {
                // detect latest compatible release version
                int repositoryDBReleaseVersion = 0;
                for (Ref ref : registryDBGit.branchList().setListMode(ListMode.REMOTE).call()) {
                    final String branchName = ref.getName();
                    if(branchName.startsWith(RELEASE_BRANCH_REMOTE_PREFIX)) {
                        try {
                            repositoryDBReleaseVersion = Math.max(repositoryDBReleaseVersion, Integer.parseInt(branchName.substring(RELEASE_BRANCH_REMOTE_PREFIX.length())));
                        } catch (NumberFormatException ex) {
                            logger.warn(registry.getName() + " remote database contains an invalid release branch["+branchName+"]! Those will be skipped...");
                        }
                    }
                }

                // detect the repository version to checkout.
                // those needs to be compatible whit this application software version defined by the max supported converter pipeline version.
                final int latestCompatibleRepositoryDBVersion = Math.min(repositoryDBReleaseVersion, latestSupportedDBVersion);

                // lookup local branch
                boolean localBranchExist = false;
                for (Ref ref : registryDBGit.branchList().setListMode(ListMode.ALL).call()) {
                    if (ref.getName().equals(RELEASE_BRANCH_LOCAL_PREFIX + latestCompatibleRepositoryDBVersion)) {
                        localBranchExist = true;
                        break;
                    }
                }

                // checkout branch
                registryDBGit.checkout()
                        .setName(RELEASE_BRANCH_NAME_PREFIX + latestCompatibleRepositoryDBVersion)
                        .setCreateBranch(!localBranchExist)
                        .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
                        .setStartPoint(RELEASE_BRANCH_REMOTE_PREFIX + latestCompatibleRepositoryDBVersion)
                        .call();
            }

            // sync with remote repo
            try {
                registryDBGit.pull().call();
            } catch (final TransportException ex) {
                // skip offline warnings.
                if(!offline) {
                    throw ex;
                }
            }
        } catch (GitAPIException | CouldNotPerformException | IOException ex) {
            throw new CouldNotPerformException("Auto db update of "+ registry.getName()+" failed!", ex);
        }
    }
}
