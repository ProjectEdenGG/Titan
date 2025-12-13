package gg.projecteden.titan.saturn;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.network.ServerClientMessaging;
import gg.projecteden.titan.network.serverbound.Versions;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Saturn {

	@Getter
	private static final SaturnUpdater updater = SaturnUpdater.GIT;
	public static SaturnUpdater.Env env = SaturnUpdater.Env.PROD;
	public static final Path PATH = FabricLoader.getInstance().getGameDir().resolve("resourcepacks/Saturn");
	public static final Path DOT_GIT_PATH = PATH.resolve(".git");

	public static List<Runnable> queuedProcesses = new ArrayList<>();

	public static boolean update() {
		try {
			if (!isInstalled()) {
				Titan.log("Installing Saturn");
				Titan.log(updater.install());
			} else if (updater.checkForUpdates()) {
				Titan.log("Updating Saturn");
				Titan.log(updater.update());
			} else {
				Titan.log("Not updating as Saturn is already up-to-date");
				return false;
			}
			ServerClientMessaging.send(new Versions());
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean isInstalled() {
		return PATH.toFile().exists() && DOT_GIT_PATH.toFile().exists();
	}

	public static String version() {
		if (!isInstalled())
			return null;

		return updater.version();
	}

	public static String shortVersion() {
		final String version = Saturn.version();
		if (version == null)
			return null;

		return version.substring(0, Math.min(7, version.length()));
	}

	public static boolean checkForUpdates() {
		return updater.checkForUpdates();
	}

	public static CompletableFuture<Boolean> checkForUpdatesAsync() {
		return updater.checkForUpdatesAsync();
	}

	public static void queueProcess(Runnable runnable) {
		queuedProcesses.add(runnable);
	}

	public static void enable() {
		PackRepository manager = Minecraft.getInstance().getResourcePackRepository();
		if (!manager.getSelectedIds().contains("file/Saturn")) {
			List<String> packs = new ArrayList<>(manager.getSelectedIds());
			packs.add("file/Saturn");
			manager.setSelected(packs);
			Minecraft.getInstance().reloadResourcePacks();
		}
	}

	public static void disable() {
		PackRepository manager = Minecraft.getInstance().getResourcePackRepository();
		if (manager.getSelectedIds().contains("file/Saturn")) {
			List<String> packs = new ArrayList<>(manager.getSelectedIds());
			packs.remove("file/Saturn");
			manager.setSelected(packs);
			Minecraft.getInstance().reloadResourcePacks();
		}
	}

}
