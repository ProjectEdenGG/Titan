package gg.projecteden.titan.events;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.network.ServerClientMessaging;
import gg.projecteden.titan.network.serverbound.Handshake;
import gg.projecteden.titan.network.serverbound.TitanConfig;
import gg.projecteden.titan.network.serverbound.Versions;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import gg.projecteden.titan.update.TitanUpdater;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static gg.projecteden.titan.config.ConfigItem.*;
import static gg.projecteden.titan.utils.Utils.isOnEden;

public class ResourcePackEvents {

	static final Component text = Component.literal("")
			.append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
			.withStyle(ChatFormatting.RESET)
			.append(Component.literal("Titan").withStyle(ChatFormatting.YELLOW))
			.append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
			.withStyle(ChatFormatting.RESET)
			.append(Component.literal(" Saturn was updated during your last textures reload!").withStyle(ChatFormatting.DARK_AQUA));

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
			if (isOnEden()) {
				Saturn.env = handler.getConnection().getRemoteAddress().toString().contains("25565") ? SaturnUpdater.Env.PROD : SaturnUpdater.Env.TEST;
				if (SATURN_MANAGE_STATUS.getValue())
					Saturn.enable();
				ServerClientMessaging.send(new Handshake());
				ServerClientMessaging.send(new Versions());
				ServerClientMessaging.send(new TitanConfig());
			}
		}));
		ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
			TitanUpdater.checkForUpdates();
			Saturn.env = SaturnUpdater.Env.PROD;
			if (SATURN_MANAGE_STATUS.getValue())
				Saturn.disable();
		}));

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			if (SATURN_ENABLED_DEFAULT.getValue())
				Saturn.enable();
		});

		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(Titan.id("reload_listener"), new PreparableReloadListener() {

			long lastForcedReload = 0L;

			@Override
			public CompletableFuture<Void> reload(SharedState store, Executor prepareExecutor, PreparationBarrier reloadSynchronizer, Executor applyExecutor) {
				if (isOnEden() && (Saturn.getUpdater() == SaturnUpdater.GIT || SATURN_UPDATE_INSTANCES.getValue() != SaturnUpdater.Mode.START_UP)) {
					Saturn.queueProcess(() -> {
						if (Saturn.update()) {
							long thisReload = System.currentTimeMillis(); // Cooldown on forced reload. Should hopefully solve infinite loops
							if (thisReload - lastForcedReload < 30000)
								return;
							lastForcedReload = thisReload;
							Minecraft.getInstance().reloadResourcePacks();
							Minecraft.getInstance().gui.getChat().addMessage(text);
						}

						ServerClientMessaging.send(new Versions());
					});
				}
				return CompletableFuture.completedFuture(null);
			}
		});
	}

}
