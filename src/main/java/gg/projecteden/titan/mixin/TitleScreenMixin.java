package gg.projecteden.titan.mixin;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.update.TitanUpdater;
import gg.projecteden.titan.update.UpdateStatus;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
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
	private static ServerInfo serverInfo;

	// Just have to have this due to screen methods
	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "addNormalWidgets")
	private void addDirectServerButton(int y, int spacingY, CallbackInfoReturnable ci) {
		if (TitleScreenMixin.serverInfo == null) {
			ServerInfo serverInfo = null;
			ServerList serverList = new ServerList(MinecraftClient.getInstance());
			String[] ignoredSubs = { "update", "test", "prespace", "old" };
			servers:
			for (int i = 0; i < serverList.size(); i++) {
				String ip = serverList.get(i).address.toLowerCase();
				if (ip.contains("projecteden.gg")) {
					for (String ignored : ignoredSubs)
						if (ip.contains(ignored))
							continue servers;
					serverInfo = serverList.get(i);
					break;
				}
			}
			if (serverInfo == null)
				serverInfo = new ServerInfo("project-eden", "projecteden.gg", ServerInfo.ServerType.OTHER);

			TitleScreenMixin.serverInfo = serverInfo;
		}

		boolean modMenu = FabricLoader.getInstance().getModContainer("modmenu").isPresent();
		if (modMenu)
			y -= spacingY;

		ButtonWidget.PressAction action = button -> {
			if (TitanUpdater.updateStatus == UpdateStatus.AVAILABLE && Screen.hasShiftDown()) {
				Util.getOperatingSystem().open(Titan.MODRINTH_URL);
			} else
				ConnectScreen.connect(this, MinecraftClient.getInstance(), ServerAddress.parse("projecteden.gg"), TitleScreenMixin.serverInfo, false, null);
		};
		TextIconButtonWidget textIconButtonWidget = this.addDrawableChild(TextIconButtonWidget.builder(Text.of(""), action, true)
						.width(20)
						.texture(PE_LOGO_IDEN, 20, 20)
						.build());
		textIconButtonWidget.setPosition(this.width / 2 + 104, y - spacingY);
		textIconButtonWidget.setTooltip(TitanUpdater.updateStatus.getTitleScreenTooltip());

		y -= spacingY;
		if (!modMenu)
			y -= 10;

		if (TitanUpdater.updateStatus != UpdateStatus.NONE || Titan.debug) {
			int finalY = y;
			this.addDrawable((context, mouseX, mouseY, delta) -> {
				context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, UPDATE_AVAILABLE, this.width / 2 + 120, finalY, 5, 20);
			});
		}
	}



}
