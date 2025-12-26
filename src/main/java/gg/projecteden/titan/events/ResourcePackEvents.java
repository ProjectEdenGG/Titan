package gg.projecteden.titan.events;

import gg.projecteden.titan.network.ServerClientMessaging;
import gg.projecteden.titan.network.serverbound.Handshake;
import gg.projecteden.titan.network.serverbound.TitanConfig;
import gg.projecteden.titan.network.serverbound.Versions;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import gg.projecteden.titan.update.TitanUpdater;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import static gg.projecteden.titan.config.ConfigItem.*;
import static gg.projecteden.titan.utils.Utils.isOnEden;

public class ResourcePackEvents {

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
	}

}
