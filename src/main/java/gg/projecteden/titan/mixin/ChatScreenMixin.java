package gg.projecteden.titan.mixin;

import gg.projecteden.titan.config.ConfigItem;
import gg.projecteden.titan.discord.PlayerStates;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

import static gg.projecteden.titan.utils.Utils.isOnEden;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {

    @Shadow protected EditBox input;

    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!isOnEden())
            return;
        if (!ConfigItem.CHAT_CHANNEL_RENDER.getValue())
            return;

        PlayerStates.ChatChannel channel = detectQuickMessage();
        if (channel == null)
            channel = PlayerStates.getChannel();

        if (channel == PlayerStates.ChatChannel.UNKNOWN)
            return;

        context.fill(0, this.height - 14, 2, this.height - 2, channel.getColor());
    }

    @Unique
    private static final List<String> CHANNEL_ALIASES = Arrays.asList("/ch", "/chat", "/channel");
    @Unique
    private static final List<String> MESSAGE_ALIASES = Arrays.asList("/message", "/m", "/msg", "/w", "/whisper", "/t", "/tell", "/pm", "/dm", "/r", "/reply");

    @Unique
    private PlayerStates.ChatChannel detectQuickMessage() {
        String chat = this.input.getValue().trim();

        if (chat.isBlank())
            return null;

        for (String prefix : CHANNEL_ALIASES) {
            if (chat.startsWith(prefix + " qm ")) {
                String[] parts = chat.split(" ", 4);

                if (parts.length >= 3) {
                    String target = parts[2];

                    return Arrays.stream(PlayerStates.ChatChannel.values())
                            .filter(channel -> target.equals(channel.getShortcut()) ||
                                    target.equalsIgnoreCase(channel.name()))
                            .findFirst()
                            .orElse(null);
                }
            }

            if (chat.startsWith(prefix + " ")) {
                String[] parts = chat.split(" ", 3);

                if (parts.length == 3) {
                    String target = parts[1];

                    return Arrays.stream(PlayerStates.ChatChannel.values())
                            .filter(channel -> target.equals(channel.getShortcut()) ||
                                    target.equalsIgnoreCase(channel.name()))
                            .findFirst()
                            .orElse(null);
                }
            }
        }

        for (String prefix : MESSAGE_ALIASES)
            if (chat.startsWith(prefix + " "))
                return PlayerStates.ChatChannel.PRIVATE;

        return null;
    }

}
