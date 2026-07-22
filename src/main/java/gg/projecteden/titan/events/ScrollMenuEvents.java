package gg.projecteden.titan.events;

import gg.projecteden.titan.network.ServerClientMessaging;
import gg.projecteden.titan.network.serverbound.Scroll;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;

public class ScrollMenuEvents {

    public static void register() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenMouseEvents.allowMouseScroll(screen).register((_screen, x, y, horiz, vert) -> {
                if (!(_screen instanceof ContainerScreen containerScreen))
                    return true;
                if (!containerScreen.getTitle().getString().contains("久"))
                    return true;

                ServerClientMessaging.send(new Scroll(vert <= 0));
                return false;
            });
        });
    }

}
