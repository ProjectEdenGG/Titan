package gg.projecteden.titan.saturn;

import gg.projecteden.titan.Config;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.update.GitResponse;
import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.nio.file.Paths;

import static gg.projecteden.titan.Utils.bash;
import static gg.projecteden.titan.Utils.getGitResponse;
import static gg.projecteden.titan.saturn.Saturn.PATH;

public enum SaturnUpdater {
	ZIP_DOWNLOAD {
		boolean updateAvailable = false;

		@Override
		public String version() {
			return Saturn.version;
		}

		@Override
		public String install() {
			return update();
		}

		@Override
		public String update() {
			try {
				Titan.log("Updating Saturn via Zip Download");
				FileUtils.copyURLToFile(new URL(String.format("https://cdn.projecteden.gg/ResourcePack%S.zip", Saturn.env.getSuffix())), Paths.get(PATH + ".zip").toFile());
				Titan.log("Unpacking...");

				try (ZipFile zipFile = new ZipFile(PATH + ".zip")) {
					PATH.toFile().delete();
					zipFile.extractAll(PATH.toString());
					zipFile.getFile().delete();
				}

				Saturn.version = bash("curl " + String.format("https://cdn.projecteden.gg/SaturnVersion%s", Saturn.env.getSuffix()), PATH.toFile());
				Config.save();
				updateAvailable = false;
				return "Successfully updated Saturn";
			} catch (Exception ex) {
				Titan.log("An error occurred while updating Saturn:");
				ex.printStackTrace();
				return null;
			}
		}

		@Override
		public boolean checkForUpdates() {
			if (updateAvailable)
				return true;
			try {
				String serverVersion = bash("curl " + String.format("https://cdn.projecteden.gg/SaturnVersion%s", Saturn.env.getSuffix()), PATH.toFile());
				if (!serverVersion.equals(version())) {
					updateAvailable = true;
					return true;
				}

			} catch (Exception ex) {
				Titan.log("An error occurred while checking for Saturn updates");
				ex.printStackTrace();
			}
			return false;
		}
	},
	GIT {
		boolean updateAvailable;

		@Override
		public String version() {
			return git("rev-parse HEAD");
		}

		@Override
		public String install() {
			if (PATH.toFile().exists()) {
				try {
					FileUtils.deleteDirectory(PATH.toFile());
				} catch (Exception ex) {
					Titan.log("An error occurred while updating Saturn:");
					ex.printStackTrace();
				}
			}
			return bash("git clone https://github.com/ProjectEdenGG/Saturn.git", FabricLoader.getInstance().getGameDir().resolve("resourcepacks").toFile());
		}

		@Override
		public String update() {
			Titan.log("Updating Saturn via git");
			if (Saturn.hardReset)
				Titan.log(git("reset --hard HEAD"));
			updateAvailable = false;
			return git("pull");
		}

		@Override
		public boolean checkForUpdates() {
			if (updateAvailable)
				return true;
			else {
				try {
					String commitVersion = getGitResponse("Saturn/commits/main", GitResponse.Saturn.class).getSha();
					String saturnVersion = Saturn.version();
					updateAvailable = commitVersion != null && saturnVersion != null && !commitVersion.startsWith(saturnVersion);
				} catch (Exception ignore) { } // Rate limit on unauthenticated git api requests
			}
			return updateAvailable;
		}

		@NotNull
		private static String git(String command) {
			return bash("git " + command, PATH.toFile());
		}
	};

	public abstract String version();

	public abstract String install();

	public abstract String update();

	public abstract boolean checkForUpdates();

	public enum Mode {
		START_UP,
		TEXTURE_RELOAD,
		BOTH
	}

	public enum Env {
		PROD,
		TEST {
			@Override
			public String getSuffix() {
				return "-TEST";
			}
		};

		public String getSuffix() {
			return "";
		}
	}

}