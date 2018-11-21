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

import org.openbase.jul.exception.NotAvailableException;
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class GitTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, NotAvailableException {
        System.out.println("is tag:" + isTag(getHead(new FileRepository("/home/divine/tmp/testgit/.git"))));

        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder().findGitDir(new File("/home/divine/tmp/testgit/.git"));

        if (repositoryBuilder.getGitDir() == null) {
            throw new NotAvailableException("git repository");
        }
        Repository repo = repositoryBuilder.build();
//        Repository repo = new FileRepository("/home/divine/tmp/testgit/.git");
        Git git = new Git(repo);

        while (true) {
            try {
                System.out.println("running");
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                break;
            }
        }
        System.out.println("close");
        git.close();
        repo.close();
        System.out.println("finish");
    }

    private static Ref getHead(final Repository repository) throws IOException {
        return repository.findRef(Constants.HEAD);
    }

    private static boolean isTag(final Ref ref) {
        return !ref.getTarget().getName().contains("refs/heads");
    }

}
