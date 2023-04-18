/*
 * Copyright (C) 2021 Dr. Jean-Jacques Ponciano.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package info.ponciano.lab.spalodwfs.mvc.models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

//https://github.com/centic9/jgit-cookbook
public class JGit {
	private Git git;
	private CredentialsProvider cp;

	/**
	 * Initialization of a new repository
	 * 
	 * @param localPath new repository path
	 * @throws IOException           if the repository cannot be created
	 * @throws IllegalStateException if something wrong
	 * @throws GitAPIException       if there is a error in the git configuration.
	 */
	public JGit(String localPath) throws IOException, IllegalStateException, GitAPIException {
		// create the directory
		this.git = Git.init().setDirectory(new File(localPath)).call();
		System.out.println("Having repository: " + git.getRepository().getDirectory());

	}

	/**
	 * Creates instance of PiGit with a local repository
	 * 
	 * @param username     user name
	 * @param password     password
	 * @param localRepoDir the local directory path
	 * @throws IOException if something wrong.
	 */
	public JGit(String localRepoDir, String username, String password) throws IOException {
		// create the credential provider
		this.cp = new UsernamePasswordCredentialsProvider(username, password);
//
//		// now open the resulting repository with a FileRepositoryBuilder
//		FileRepositoryBuilder builder = new FileRepositoryBuilder();
//		Repository repository = builder.setGitDir(new File(localRepoDir)).readEnvironment() // scan environment GIT_*
//																							// variables
//				.findGitDir() // scan up the file system tree
//				.build();
//		System.out.println("Having repository: " + repository.getDirectory());

		this.git = Git.open(new File(localRepoDir));
//		// the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
//		Ref head = repository.getRef("refs/heads/master");
//		System.out.println("Ref of refs/heads/master: " + head);
//
//		// build the repo
//		this.git = new Git(repository);

	}

	/**
	 * Creates a new instance of PiGit according to a remote repository
	 * 
	 * @param localPath        local path to clone the repository
	 * @param user             user name
	 * @param pwd              password
	 * @param host             host where the repository has to be clone
	 * @param pathToRemoteRepo remote path of the repo on the server side
	 * @throws IOException     if the repo cannot be write
	 * @throws GitAPIException if the repo cannot be clonned.
	 */

	public JGit(String localPath, String user, String pwd, String host, String pathToRemoteRepo)
			throws IOException, GitAPIException {

		String url = "ssh://" + user + ":" + pwd + "@" + host + ":22/" + pathToRemoteRepo + "/";
		clone(localPath, url);

	}

	public JGit(String localRepoName, String username, String password, String url)
			throws InvalidRemoteException, TransportException, GitAPIException {
		// prepare a new folder for the cloned repository
		this.cp = new UsernamePasswordCredentialsProvider(username, password);
		// then clone
		System.out.println("Cloning from " + url + " to " + localRepoName);
		this.git = Git.cloneRepository().setURI(url).setDirectory(new File(localRepoName))
				.setCredentialsProvider(this.cp).call();
		// Note: the call() returns an opened repository already which needs to be
		// closed to avoid file handle leaks!
		System.out.println("Having repository: " + git.getRepository().getDirectory());
	}
	/**
	 * Clone a git repository without credential
	 * @param localRepoName local name of the repository
	 * @param url url of the remote repository to clone 
	 * @throws InvalidRemoteException if the remote url is wrong
	 * @throws TransportException if something wrong during the download
	 * @throws GitAPIException if simething wrong in the git comment ( e.g. git not installed)
	 */
	public JGit(String localRepoName, String url)
			throws InvalidRemoteException, TransportException, GitAPIException {
		// prepare a new folder for the cloned repository
		this.cp = null;
		// then clone
		System.out.println("Cloning from " + url + " to " + localRepoName);
		this.git = Git.cloneRepository().setURI(url).setDirectory(new File(localRepoName)).call();
		// Note: the call() returns an opened repository already which needs to be
		// closed to avoid file handle leaks!
		System.out.println("Having repository: " + git.getRepository().getDirectory());
	}
	/**
	 * @param localPath
	 * @param url
	 * @throws GitAPIException
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 */
	public void clone(String localPath, String url) throws GitAPIException, InvalidRemoteException, TransportException {
		// this is necessary when the remote host does not have a valid certificate,
		// ideally we would install the certificate in the JVM
		// instead of this unsecure workaround!
		CredentialsProvider allowHosts = new CredentialsProvider() {

			@Override
			public boolean supports(CredentialItem... items) {
				for (CredentialItem item : items) {
					if ((item instanceof CredentialItem.YesNoType)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
				for (CredentialItem item : items) {
					if (item instanceof CredentialItem.YesNoType) {
						((CredentialItem.YesNoType) item).setValue(true);
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean isInteractive() {
				return false;
			}
		};

		// prepare a new folder for the cloned repository

		// then clone
		System.out.println("Cloning from " + url + " to " + localPath);
		this.git = Git.cloneRepository().setURI(url).setDirectory(new File(localPath))
				.setCredentialsProvider(allowHosts).call();
		// Note: the call() returns an opened repository already which needs to be
		// closed to avoid file handle leaks!
		System.out.println("Having repository: " + git.getRepository().getDirectory());
	}

	/**
	 * Commit change of a file to the git repository.
	 * 
	 * @param inputPathFile path of the file to commit change or add the file (
	 *                      including sub hierarchy as src/package/filname.txt")
	 * @param message       commit message
	 * 
	 * @throws IOException            if the file cannot be copy in the repository
	 * @throws NoFilepatternException if the file is corrupted
	 * @throws GitAPIException        if something is wrong on the git configuration
	 */
	public void commitAfile(String inputPathFile, String message)
			throws IOException, NoFilepatternException, GitAPIException {
//		// create the file
//		File out = new File(repository.getDirectory().getParent(), filename);
//		final File f = new File(inputPathFile);
//		Files.copy(f.toPath(), out.toPath(), REPLACE_EXISTING);

		// run the add-call
		git.add().addFilepattern(inputPathFile).call();
		// and then commit the changes
		if (message == null || message.isEmpty())
			message = "Added testfile";
		git.commit().setMessage(message).call();
		System.out.println(
				"Added file " + inputPathFile + " to repository at " + this.git.getRepository().getDirectory());
	}

	/**
	 * Commit all change of the repository
	 * 
	 * @param message commit message
	 * @throws NoHeadException
	 * @throws NoMessageException
	 * @throws UnmergedPathsException
	 * @throws ConcurrentRefUpdateException
	 * @throws WrongRepositoryStateException
	 * @throws GitAPIException
	 */
	public void commitAllchange(String message) throws NoHeadException, NoMessageException, UnmergedPathsException,
			ConcurrentRefUpdateException, WrongRepositoryStateException, GitAPIException {
		if (message == null || message.isEmpty())
			message = "Commit changes to all files";
		// Stage all changed files, omitting new files, and commit with one command
		git.commit().setAll(true).setMessage(message).call();
	}

	/**
	 * @throws GitAPIException
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 */
	public void lsRemote() throws GitAPIException, InvalidRemoteException, TransportException {
		Collection<Ref> remoteRefs = git.lsRemote().setCredentialsProvider(cp).setRemote("origin").setTags(true)
				.setHeads(false).call();
		for (Ref ref : remoteRefs) {
			System.out.println(ref.getName() + " -> " + ref.getObjectId().name());
		}
	}

	public void push() throws InvalidRemoteException, TransportException, GitAPIException {
		this.git.push().setCredentialsProvider(this.cp).call();

		System.out.println("Pushed!");
	}

	public void pull() throws WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException,
			InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException, TransportException,
			GitAPIException {
		this.git.pull().setCredentialsProvider(this.cp).call();

		System.out.println("Pulled!");
	}

	/**
	 * Add, commit and push a new file to a git repository
	 * 
	 * @param localRepoName local name of the git repository (Example:
	 *                      schema-storage)
	 * @param dirInRepo     directory inside the repo where the file has to be
	 *                      added. (Example to have
	 *                      "repo/directory/subdirectory/file.xml":dirInRepo=
	 *                      "directory/subdirectory/)"
	 * @param file2add      file to add ( will be copied)
	 * @param usr           user name for the authentication in the git
	 * @param pwd           password of the user for the authentication
	 * @param uri           URI of the git repository (Example:
	 *                      https://git.gdi-de.org/claire.prudhomme/schema-storage.git)
	 */
	public static void AddFile2Git(String localRepoName, String dirInRepo, File file2add, String usr, String pwd,
			String uri) {
		new File(localRepoName + "/" + dirInRepo).mkdir();

		// create a file (from upload maybe)
		File newFile = file2add;
		try {

			// cloning
			JGit git;
			try {
				git = new JGit(localRepoName, usr, pwd, uri);
			} catch (Exception e) {
				git = new JGit(localRepoName, usr, pwd);
			}
			// move the file to the right place
			File out = new File(localRepoName + "/" + dirInRepo + newFile.getName());
			Files.copy(newFile.toPath(), out.toPath(), REPLACE_EXISTING);

			// git add file
			git.commitAfile(dirInRepo + out.getName(), null);
			// git push
			git.push();

		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		String localRepoName = "schema-storage";
		String dirInRepo = "test/";
		new File(localRepoName + "/" + dirInRepo).mkdir();

		// create a file (from upload maybe)
		File newFile = new File("test.xml");
		try {

			// git cloning
			JGit git;
			try {
				git = new JGit(localRepoName, "claire.prudhomme", "",
						"https://git.gdi-de.org/claire.prudhomme/schema-storage.git");
			} catch (Exception e) {
				git = new JGit(localRepoName, "claire.prudhomme", "");
			}
			// move the file to the right place
			File out = new File(localRepoName + "/" + dirInRepo + newFile.getName());
			Files.copy(newFile.toPath(), out.toPath(), REPLACE_EXISTING);

			// git add file
			git.commitAfile(dirInRepo + out.getName(), null);
			// git push
			git.push();

		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}

	}

}
