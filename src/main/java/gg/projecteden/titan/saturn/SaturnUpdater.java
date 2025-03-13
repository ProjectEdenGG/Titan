package gg.projecteden.titan.saturn;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.config.ConfigItem;
import gg.projecteden.titan.update.GitResponse;
import joptsimple.internal.Strings;
import lombok.SneakyThrows;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static gg.projecteden.titan.saturn.Saturn.PATH;
import static gg.projecteden.titan.utils.Utils.getGitResponse;

public enum SaturnUpdater {
	GIT {
		final String REMOTE_NAME = "https";
		final String REMOTE_URI = "https://github.com/ProjectEdenGG/Saturn.git";
		boolean updateAvailable;

		@Override
		public String version() {
			try (Git git = git()) {
				return git.getRepository().findRef("HEAD").getObjectId().getName().substring(0, 7);
			} catch (Exception ex) {
				ex.printStackTrace();
				if (!ConfigItem.SATURN_HARD_RESET.getValue())
					return "Unknown";
				Titan.log("Attempting to reinstall...");
				Titan.log(install());
				try (Git git = git()) {
					return git.getRepository().findRef("HEAD").getObjectId().getName().substring(0, 7);
				} catch (Exception ex2) {
					ex2.printStackTrace();
					return "Unknown";
				}
			}
		}

		@Override
		public String install() {
			try {
				if (PATH.toFile().exists())
					FileUtils.deleteDirectory(PATH.toFile());
				try (Git git = cloneCommand().call()) {
					return git.toString();
				}
			} catch (Exception ex) {
				Titan.log("An error occurred while installing Saturn:");
				ex.printStackTrace();
				return ex.getMessage();
			}
		}

		@SneakyThrows
		protected CloneCommand cloneCommand() {
			return Git.cloneRepository()
					.setBranchesToClone(List.of("refs/heads/titan"))
					.setBranch("titan")
					.setURI(REMOTE_URI)
					.setDirectory(getResourcePackFolder().resolve("Saturn").toFile());
		}

		@NotNull
		private Path getResourcePackFolder() {
			return FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
		}

		@Override
		@SneakyThrows
		public String update() {
			Titan.log("Updating Saturn via jgit");
			try (Git git = git()) {
				if (ConfigItem.SATURN_HARD_RESET.getValue()) {
					BranchConfig branchConfig = new BranchConfig(git.getRepository().getConfig(), git.getRepository().getBranch());

					String ref = branchConfig.getRemote() + "/" + branchConfig.getMerge().substring("refs/heads/".length());
					Titan.log("Using ref " + ref);
					git.reset().setMode(ResetType.HARD).setRef(ref).call();
				}

				updateAvailable = false;

				final List<RemoteConfig> remotes = git.remoteList().call();

				if (remotes.stream().noneMatch(config -> config.getName().equals(REMOTE_NAME)))
					git.remoteAdd().setName(REMOTE_NAME).setUri(new URIish(REMOTE_URI)).call();
				else
					git.remoteSetUrl().setRemoteName(REMOTE_NAME).setRemoteUri(new URIish(REMOTE_URI)).call();

				return git.pull().setRemote(REMOTE_NAME).setRebase(true).call().toString();
			}
		}

		@NotNull
		@SneakyThrows
		private Git git() {
			return new Git(new FileRepositoryBuilder().setGitDir(getResourcePackFolder().resolve("Saturn").resolve(".git").toFile())
					.readEnvironment()
					.findGitDir()
					.build());
		}

		@Override
		public boolean checkForUpdates() {
			if (updateAvailable)
				return true;
			else {
				try (Git git  = git()) {
					String commitVersion = getGitResponse("Saturn/commits/" + git.getRepository().getBranch(), GitResponse.Saturn.class).getSha();
					String saturnVersion = Saturn.version();
					updateAvailable = (commitVersion != null && saturnVersion != null && !commitVersion.startsWith(saturnVersion)) || Strings.isNullOrEmpty(saturnVersion);
				} catch (Exception ignore) { } // Rate limit on unauthenticated git api requests
			}
			return updateAvailable;
		}

		@Override
		public CompletableFuture<Boolean> checkForUpdatesAsync() {
			CompletableFuture<Boolean> future = new CompletableFuture<>();
			if (updateAvailable)
				return CompletableFuture.completedFuture(true);
			else {
				new Thread(() -> {
					try (Git git  = git()) {
						String commitVersion = getGitResponse("Saturn/commits/" + git.getRepository().getBranch(), GitResponse.Saturn.class).getSha();
						String saturnVersion = Saturn.version();
						future.complete((commitVersion != null && saturnVersion != null && !commitVersion.startsWith(saturnVersion)) || Strings.isNullOrEmpty(saturnVersion));
					} catch (Exception ignore) { } // Rate limit on unauthenticated git api requests
				}).start();
			}
			return future;
		}

		@Override
		public void branch(String branch) {
			if (branch().endsWith(branch))
				return;

			Titan.log("Checking out to " + branch);
			try (Git git = git()) {
				// Fetch from all remotes
				for (String remote : git.getRepository().getRemoteNames()) {
					Titan.log("Fetching from remote: " + remote);
					git.fetch().setRemote(remote).setRefSpecs("+refs/heads/*:refs/remotes/" + remote + "/*").call();
				}

				// Check if branch already exists locally
				boolean branchExistsLocally = git.getRepository().findRef(branch) != null;

				if (branchExistsLocally) {
					Titan.log("Branch '" + branch + "' exists locally. Checking it out...");
					Saturn.queueProcess(() -> {
						try {
							git.checkout()
									.setName(branch)
									.call();
							MinecraftClient.getInstance().reloadResources();
						} catch (Exception e) {
							Titan.log("Failed to checkout " + branch);
							e.printStackTrace();
						}
					});

				} else {
					// Get all remote branches
					Set<String> remotes = git.getRepository().getRemoteNames();
					String foundRemote = null;

					for (String remote : remotes) {
						List<Ref> refs = git.lsRemote()
								.setRemote(remote)
								.setHeads(true)
								.call()
								.stream()
								.toList();

						boolean branchExists = refs.stream()
								.anyMatch(ref -> ref.getName().endsWith(branch));

						if (branchExists) {
							foundRemote = remote;
							break; // Stop once we find the branch
						}
					}

					if (foundRemote == null) {
						Titan.log("Branch '" + branch + "' does not exist on any remote!");
						return;
					}

					Titan.log("Branch found on remote: " + foundRemote);

					// Ensure branch exists locally
					String remoteBranchRef = "refs/remotes/" + foundRemote + "/" + branch;
					if (git.getRepository().exactRef(remoteBranchRef) == null) {
						Titan.log("Remote branch found but not tracked locally, attempting to fetch explicitly...");
						git.fetch().setRemote(foundRemote).setRefSpecs("+refs/heads/" + branch + ":refs/remotes/" + foundRemote + "/" + branch).call();
					}

					// Try resolving again
					if (git.getRepository().exactRef(remoteBranchRef) == null) {
						Titan.log("Failed to fetch branch '" + branch + "' from remote '" + foundRemote + "'");
						return;
					}

					Titan.log("Creating and checking out branch '" + branch + "' from remote '" + foundRemote + "'");
					Saturn.queueProcess(() -> {
						try {
							git.checkout()
									.setCreateBranch(true)
									.setForceRefUpdate(true)
									.setName(branch)
									.setStartPoint(remoteBranchRef)
									.call();
							MinecraftClient.getInstance().reloadResources();
						} catch (Exception e) {
							Titan.log("Could not checkout to new branch " + branch);
							e.printStackTrace();
						}
					});
				}

				MinecraftClient.getInstance().reloadResources();
			} catch (Exception e) {
				Titan.log("Error: " + e.getMessage());
			}
		}


		@Override
		public String branch() {
            try {
				try (Git git = git()) {
					return git.getRepository().getBranch();
				}
            } catch (IOException e) {
                return "Unknown";
            }
        }
	};

	public abstract String version();

	public abstract String install();

	public abstract String update();

	public abstract void branch(String branch);

	public abstract String branch();

	public abstract boolean checkForUpdates();

	public abstract CompletableFuture<Boolean> checkForUpdatesAsync();

	public enum Mode {
		START_UP,
		TEXTURE_RELOAD,
		BOTH
	}

	public enum Env {
		PROD {
			@Override
			public String getSuffix() {
				return "";
			}
		},
		TEST,
		;

		public String getSuffix() {
			return "-" + name();
		}
	}

}
