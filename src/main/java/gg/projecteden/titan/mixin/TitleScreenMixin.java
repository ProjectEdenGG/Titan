package gg.projecteden.titan.mixin;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.update.TitanUpdater;
import gg.projecteden.titan.update.UpdateStatus;
import gg.projecteden.titan.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static gg.projecteden.titan.Titan.PE_LOGO_IDEN;
import static gg.projecteden.titan.Titan.UPDATE_AVAILABLE;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

	@Unique
	private static ServerData serverInfo;

	// Just have to have this due to screen methods
	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "createNormalMenuOptions")
	private void addDirectServerButton(int y, int spacingY, CallbackInfoReturnable ci) {
		if (TitleScreenMixin.serverInfo == null) {
			ServerData serverInfo = null;
			ServerList serverList = new ServerList(Minecraft.getInstance());
			String[] ignoredSubs = { "update", "test", "prespace", "old" };
			servers:
			for (int i = 0; i < serverList.size(); i++) {
				String ip = serverList.get(i).ip.toLowerCase();
				if (ip.contains("projecteden.gg")) {
					for (String ignored : ignoredSubs)
						if (ip.contains(ignored))
							continue servers;
					serverInfo = serverList.get(i);
					break;
				}
			}
			if (serverInfo == null)
				serverInfo = new ServerData("project-eden", "projecteden.gg", ServerData.Type.OTHER);

			TitleScreenMixin.serverInfo = serverInfo;
		}

		boolean modMenu = FabricLoader.getInstance().getModContainer("modmenu").isPresent();
		if (modMenu)
			y -= spacingY;

		Button.OnPress action = button -> {
			if (TitanUpdater.updateStatus == UpdateStatus.AVAILABLE && Utils.isShiftPressed()) {
				Util.getPlatform().openUri(Titan.MODRINTH_URL);
			} else
				ConnectScreen.startConnecting(this, Minecraft.getInstance(), ServerAddress.parseString("projecteden.gg"), TitleScreenMixin.serverInfo, false, null);
		};
		SpriteIconButton textIconButtonWidget = this.addRenderableWidget(SpriteIconButton.builder(Component.nullToEmpty(""), action, true)
						.width(20)
						.sprite(PE_LOGO_IDEN, 20, 20)
						.build());
		textIconButtonWidget.setPosition(this.width / 2 + 104, y - spacingY);
		textIconButtonWidget.setTooltip(TitanUpdater.updateStatus.getTitleScreenTooltip());

		y -= spacingY;
		if (!modMenu)
			y -= 10;

		if (TitanUpdater.updateStatus != UpdateStatus.NONE || Titan.debug) {
			int finalY = y;
			this.addRenderableOnly((context, mouseX, mouseY, delta) -> {
				context.blitSprite(RenderPipelines.GUI_TEXTURED, UPDATE_AVAILABLE, this.width / 2 + 120, finalY, 5, 20);
			});
		}
	}



}
