package gg.projecteden.titan.network.clientbound;

import gg.projecteden.titan.network.models.Clientbound;
import gg.projecteden.titan.saturn.Saturn;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class SaturnUpdate extends Clientbound {

    static final ClickEvent clickEvent = new ClickEvent.RunCommand("/titanclient update");

    static final Component text = Component.literal("")
            .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
            .withStyle(ChatFormatting.RESET)
            .append(Component.literal("Titan").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
            .withStyle(ChatFormatting.RESET)
            .append(Component.literal(" An update for Saturn is available. ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Update in the Options menu or ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Click Here").setStyle(Style.EMPTY.withClickEvent(clickEvent)).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));

    @Override
    public void onReceive() {
        Saturn.checkForUpdatesAsync().thenAccept(update -> {
            if (update)
                Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(text);
        });
    }

}
