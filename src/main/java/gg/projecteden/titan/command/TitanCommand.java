package gg.projecteden.titan.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.saturn.Saturn;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TitanCommand {

	private static final Text error = Text.literal("")
			.append(Text.literal("[").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
			.formatted(Formatting.RESET)
			.append(Text.literal("Titan").formatted(Formatting.YELLOW))
			.append(Text.literal("]").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
			.formatted(Formatting.RESET)
			.append(Text.literal(" You are already on the most updated version of Saturn").formatted(Formatting.RED));

	private static final Text version = Text.literal("")
			.append(Text.literal("[").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
			.formatted(Formatting.RESET)
			.append(Text.literal("Titan").formatted(Formatting.YELLOW))
			.append(Text.literal("]").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
			.append(Text.literal(" Running version ").formatted(Formatting.DARK_AQUA))
			.append(Text.literal(Titan.version()).formatted(Formatting.YELLOW));

	public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
		dispatcher.register(literal("titanclient")
				.then(literal("update").executes(context -> {
						Saturn.checkForUpdatesAsync().thenAccept(update -> {
							if (!update)
								MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(TitanCommand.error);
							else {
									Saturn.queueProcess(() -> {
										if (Saturn.update())
											MinecraftClient.getInstance().reloadResources();
									});
									MinecraftClient.getInstance().reloadResources();
								}
						});

						return Command.SINGLE_SUCCESS;
					}))
					.executes(context -> {
						MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(TitanCommand.version);
						return Command.SINGLE_SUCCESS;
					}));
	}

}
