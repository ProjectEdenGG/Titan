package gg.projecteden.titan.network.clientbound;

import gg.projecteden.titan.network.models.Clientbound;
import gg.projecteden.titan.saturn.Saturn;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SaturnUpdate extends Clientbound {

    static final ClickEvent clickEvent = new ClickEvent.RunCommand("/titanclient update");

    static final Text text = Text.literal("")
            .append(Text.literal("[").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
            .formatted(Formatting.RESET)
            .append(Text.literal("Titan").formatted(Formatting.YELLOW))
            .append(Text.literal("]").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
            .formatted(Formatting.RESET)
            .append(Text.literal(" An update for Saturn is available. ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Update in the Options menu or ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Click Here").setStyle(Style.EMPTY.withClickEvent(clickEvent)).formatted(Formatting.YELLOW, Formatting.BOLD));

    @Override
    public void onReceive() {
        Saturn.checkForUpdatesAsync().thenAccept(update -> {
            if (update)
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
        });
    }

}
