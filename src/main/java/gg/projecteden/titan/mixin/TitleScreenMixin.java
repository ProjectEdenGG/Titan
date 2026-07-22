package gg.projecteden.titan.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.update.TitanUpdater;
import gg.projecteden.titan.update.UpdateStatus;
import gg.projecteden.titan.utils.Utils;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
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
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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
	private void addDirectServerButton(int y, int spacingY, CallbackInfoReturnable<Integer> cir, @Local(name = "singleplayerButton") Button singleplayerButton) {
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

		Button.OnPress action = button -> {
			if (TitanUpdater.updateStatus == UpdateStatus.AVAILABLE && Utils.isShiftPressed()) {
				Util.getPlatform().openUri(Titan.MODRINTH_URL);
			} else
				ConnectScreen.startConnecting(this, Minecraft.getInstance(), ServerAddress.parseString("projecteden.gg"), TitleScreenMixin.serverInfo, false, null);
		};
		SpriteIconButton textIconButtonWidget = SpriteIconButton.builder(Component.nullToEmpty(""), action, true)
						.width(20)
						.sprite(PE_LOGO_IDEN, 20, 20)
						.build();
		textIconButtonWidget.setPosition(this.width / 2 + 104, singleplayerButton.getY() + spacingY);
		textIconButtonWidget.setTooltip(TitanUpdater.updateStatus.getTitleScreenTooltip());

		List<AbstractWidget> widgets = Screens.getWidgets(this);

		int realmsIndex = -1;

		for (int i = 0; i < widgets.size(); i++) {
			AbstractWidget widget = widgets.get(i);

			if (widget instanceof Button button
					&& button.getMessage().getContents() instanceof TranslatableContents contents
					&& contents.getKey().equals("menu.online")) {
				realmsIndex = i;
				break;
			}
		}

		if (realmsIndex >= 0)
			widgets.add(realmsIndex, textIconButtonWidget);
		else
			// Another mod may have removed the Realms button.
			widgets.add(textIconButtonWidget);

		if (TitanUpdater.updateStatus != UpdateStatus.NONE || Titan.debug) {
			this.addRenderableOnly((context, mouseX, mouseY, delta) -> {
				context.blitSprite(RenderPipelines.GUI_TEXTURED, UPDATE_AVAILABLE, textIconButtonWidget.getX() + 16, textIconButtonWidget.getY() - 10, 5, 20);
			});
		}
	}



}
