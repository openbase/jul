package org.openbase.jul.storage.registry.plugin;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import com.google.protobuf.GeneratedMessage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.preset.JPTestMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.RejectedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.storage.file.FileSynchronizer;
import org.openbase.jul.storage.registry.jp.JPGitRegistryPluginRemoteURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @param <KEY>
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a> //
 */
public class GitAutoVersionControlRegistryPlugin<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> extends ProtobufRegistryPluginAdapter<KEY, M, MB> {

    protected final Logger logger = LoggerFactory.getLogger(GitAutoVersionControlRegistryPlugin.class);

    private boolean detached;
    private final String repositoryURL;

    public GitAutoVersionControlRegistryPlugin(final String repositoryURL) throws InstantiationException {
        try {
            this.detached = false;
            this.repositoryURL = repositoryURL;
        } catch (Exception ex) {
            throw new InstantiationException(this, ex);
        }
    }

    @Override
    public void prepareRegistry(final File registyDirectory)  throws CouldNotPerformException {
        final Git git = detectGitRepository(registyDirectory);

        this.initialSync();
    }

    private String getVersion() {
            String version = null;

            // try to load from maven properties first
            try {
                Properties p = new Properties();
                InputStream is = getClass().getResourceAsStream("/META-INF/maven/com.my.group/my-artefact/pom.properties");
                if (is != null) {
                    p.load(is);
                    version = p.getProperty("version", "");
                }
            } catch (Exception e) {
                // ignore
            }

            // fallback to using Java API
            if (version == null) {
                Package aPackage = getClass().getPackage();
                if (aPackage != null) {
                    version = aPackage.getImplementationVersion();
                    if (version == null) {
                        version = aPackage.getSpecificationVersion();
                    }
                }
            }

            if (version == null) {
                // we could not compute the version so use a blank
                version = "";
            }

            return version;
        }
    }

    private static Ref getHead(final Repository repository) throws IOException {
        return repository.findRef(Constants.HEAD);
    }

    private static boolean isTag(final Ref ref) {
        return !ref.getTarget().getName().contains("refs/heads");
    }

    private void initialSync() throws CouldNotPerformException {
        try {

            // sync main git
            try {
                this.git.pull().call().isSuccessful();
            } catch (DetachedHeadException ex) {
                detached = true;
            }

            // sync submodules
            try {
                this.git.submoduleInit();
                this.git.submoduleUpdate();
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not init db submodules!", ex), logger, LogLevel.WARN);
            }
        } catch (GitAPIException ex) {
            throw new CouldNotPerformException("Initial sync failed!", ex);
        }
    }

    private Git detectGitRepository(final File databaseDirectory) throws CouldNotPerformException {
        try {

            Repository repo;
            try {
                // === load git out of db folder === //
                FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder().findGitDir(databaseDirectory);

                if (repositoryBuilder.getGitDir() == null) {
                    throw new NotAvailableException("git repository");
                }
                repo = repositoryBuilder.build();

            } catch (IOException | CouldNotPerformException | NullPointerException ex) {
                logger.info("Could not find git repository in db Directory[" + databaseDirectory + "] for Registry[" + registry + "]!");

                // === load git out of remote url === //
                Map<String, String> remoteRepositoryMap = JPService.getProperty(JPGitRegistryPluginRemoteURL.class).getValue();

                if (remoteRepositoryMap.containsKey(registry.getName()) && !remoteRepositoryMap.get(registry.getName()).isEmpty()) {
                    logger.info("Cloning git repository from " + remoteRepositoryMap.get(registry.getName()) + " into db Directory[" + databaseDirectory + "] ...");
                    return Git.cloneRepository().setURI(remoteRepositoryMap.get(registry.getName())).setDirectory(databaseDirectory).call();
                }

                repo = FileRepositoryBuilder.create(databaseDirectory);
            }
            return new Git(repo);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not detect git repo of Directory[" + databaseDirectory.getAbsolutePath() + "]!", ex);
        }
    }

    @Override
    public void afterRegister(IdentifiableMessage<KEY, M, MB> entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        commitAllChanges();
    }

    @Override
    public void afterUpdate(IdentifiableMessage<KEY, M, MB> entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        commitAllChanges();
    }

    @Override
    public void afterRemove(IdentifiableMessage<KEY, M, MB> entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        commitAllChanges();
    }

    private void commitAllChanges() throws CouldNotPerformException {

        // Avoid commit in test mode.
        try {
            if (JPService.getProperty(JPTestMode.class).getValue()) {
                logger.warn("Skip commit because test mode is enabled!");
                return;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        // Avoid commit if branch is detached.
        if (detached) {
            logger.info("Skip commit because branch detached!");
            return;
        }

        try {
            // add all changes
            git.add().addFilepattern(".").call();

            // commit
            git.commit().setMessage(JPService.getApplicationName() + " commited all changes.").call();
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not commit all database changes!", ex);
        }
    }

    @Override
    public void checkAccess() throws RejectedException {
        try {
            if (isTag(getHead(git.getRepository()))) {
                throw new RejectedException("Database based on tag revision and can not be modifiered!");
            }

            if (detached) {
                throw new RejectedException("Database based on detached branch and can not be modifiered!");
            }
        } catch (IOException ex) {
            throw new RejectedException("Could not access database!", ex);
        }
    }

    @Override
    public final void shutdown() {
        if (git == null) {
            return;
        }
        try {
            git.getRepository().close();
            git.close();
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not shutdown", ex), logger, LogLevel.ERROR);
        }
    }
}
