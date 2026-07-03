package gg.projecteden.titan.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.saturn.Saturn;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class TitanCommand {

	private static final Component error = Component.literal("")
			.append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
			.withStyle(ChatFormatting.RESET)
			.append(Component.literal("Titan").withStyle(ChatFormatting.YELLOW))
			.append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
			.withStyle(ChatFormatting.RESET)
			.append(Component.literal(" You are already on the most updated version of Saturn").withStyle(ChatFormatting.RED));

	private static final Component version = Component.literal("")
			.append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
			.withStyle(ChatFormatting.RESET)
			.append(Component.literal("Titan").withStyle(ChatFormatting.YELLOW))
			.append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
			.append(Component.literal(" Running version ").withStyle(ChatFormatting.DARK_AQUA))
			.append(Component.literal(Titan.version()).withStyle(ChatFormatting.YELLOW));

	public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext commandRegistryAccess) {
		dispatcher.register(literal("titanclient")
				.then(literal("update").executes(context -> {
						Saturn.checkForUpdatesAsync().thenAccept(update -> {
							if (!update)
								Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(TitanCommand.error);
							else {
									Saturn.queueProcess(() -> {
										if (Saturn.update())
											Minecraft.getInstance().reloadResourcePacks();
									});
									Minecraft.getInstance().reloadResourcePacks();
								}
						});

						return Command.SINGLE_SUCCESS;
					}))
					.executes(context -> {
						Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(TitanCommand.version);
						return Command.SINGLE_SUCCESS;
					}));
	}

}
