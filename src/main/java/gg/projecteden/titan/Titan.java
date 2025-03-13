package gg.projecteden.titan;

import gg.projecteden.titan.command.TitanCommand;
import gg.projecteden.titan.config.Config;
import gg.projecteden.titan.config.ConfigItem;
import gg.projecteden.titan.creative.CustomCreativeTabs;
import gg.projecteden.titan.discord.RichPresence;
import gg.projecteden.titan.events.Events;
import gg.projecteden.titan.network.ServerClientMessaging;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import gg.projecteden.titan.update.TitanUpdater;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class Titan implements ModInitializer {
	public static boolean debug = false;
	public static final String MOD_ID = "titan";
	public static final String PREFIX = String.format("[%s] ", Titan.class.getSimpleName());
	public static Logger LOGGER = LogManager.getLogger();
	public static final Identifier PE_LOGO_IDEN = Identifier.of(MOD_ID, "main_menu_button");
	public static final Identifier UPDATE_AVAILABLE = Identifier.of(MOD_ID, "icon/unseen_notification");
	public static final Identifier UPDATING_SATURN = Identifier.of(MOD_ID, "textures/gui/updating_saturn.png");
	public static final String MODRINTH_URL = "https://modrinth.com/mod/titan";
	public static final String MODRINTH_SLUG = "f44hEoWP";
	public static final String MODRINTH_TOKEN = "mrp_oyEJu5NksuJpOuRNQmHoisTES9DFdMMzATX8gFhySvNqVbHcHEzM8WD9Za7V";

	public static void log(String message, Object... objects) {
		LOGGER.info(String.format(PREFIX + message, objects));
	}

	public static void debug(String message) {
		if (!debug)
			return;

		LOGGER.info(PREFIX + "Debug: " + message);

		if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().inGameHud != null && MinecraftClient.getInstance().inGameHud.getChatHud() != null)
			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal(PREFIX + message));
	}

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}

	@NotNull
	public static ModContainer container() {
		return FabricLoader.getInstance().getModContainer(Titan.class.getSimpleName().toLowerCase())
				.orElseThrow(() -> new RuntimeException("Titan not loaded"));
	}

	public static String version() {
		return Titan.container().getMetadata().getVersion().getFriendlyString();
	}

	@Override
	public void onInitialize() {
		TitanUpdater.checkForUpdates();
		Config.load();

		if (ConfigItem.SATURN_UPDATE_INSTANCES.getValue() != SaturnUpdater.Mode.TEXTURE_RELOAD)
			Saturn.update();
		Events.register();
		if (ConfigItem.SATURN_MANAGE_STATUS.getValue() && !ConfigItem.SATURN_ENABLED_DEFAULT.getValue())
			Saturn.disable();
		ClientCommandRegistrationCallback.EVENT.register(TitanCommand::init);

		ServerClientMessaging.init();

		RichPresence.init();

		CustomCreativeTabs.init();
	}


}
