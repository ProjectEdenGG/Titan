package gg.projecteden.titan.mixin;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.network.ServerClientMessaging;
import gg.projecteden.titan.network.serverbound.Versions;
import gg.projecteden.titan.saturn.Saturn;
import gg.projecteden.titan.saturn.SaturnUpdater;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static gg.projecteden.titan.config.ConfigItem.SATURN_UPDATE_INSTANCES;
import static gg.projecteden.titan.utils.Utils.isOnEden;

@Mixin(LoadingOverlay.class)
public class SplashOverlayMixin {

	@Shadow
	private float currentProgress;

	@Unique
	private static long lastForcedReload = 0L;
	@Unique
	private static final Component text = Component.literal("")
			.append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
			.withStyle(ChatFormatting.RESET)
			.append(Component.literal("Titan").withStyle(ChatFormatting.YELLOW))
			.append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
			.withStyle(ChatFormatting.RESET)
			.append(Component.literal(" Saturn was updated during your last textures reload!").withStyle(ChatFormatting.DARK_AQUA));


	@Inject(method = "extractProgressBar", at = @At("RETURN"))
	private void start(GuiGraphicsExtractor context, int minX, int minY, int maxX, int maxY, float opacity, CallbackInfo ci) {
		if (isOnEden() && (Saturn.getUpdater() == SaturnUpdater.GIT || SATURN_UPDATE_INSTANCES.getValue() != SaturnUpdater.Mode.START_UP)) {
			Saturn.queueProcess(() -> {
				if (Saturn.update()) {
					long thisReload = System.currentTimeMillis(); // Cooldown on forced reload. Should hopefully solve infinite loops
					if (thisReload - lastForcedReload < 30000)
						return;
					lastForcedReload = thisReload;
					Minecraft.getInstance().reloadResourcePacks();
					Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(text);
					ServerClientMessaging.send(new Versions());
				}
			});
		}

		if (this.currentProgress < 0.5F)
			return;

		if (!Saturn.queuedProcesses.isEmpty() || Titan.debug) {
			Minecraft client = Minecraft.getInstance();
			if (client == null || client.getWindow() == null) return;

			int baseScreenWidth = 1920;
			int baseImageWidth = 261;
			int baseImageHeight = 19;

			int screenWidth = client.getWindow().getGuiScaledWidth();

			float scale = Math.min(screenWidth / (float) baseScreenWidth, 1.0f);
			scale = Math.max(scale, 0.5f);

			int imageWidth = (int) (baseImageWidth * scale);
			int imageHeight = (int) (baseImageHeight * scale);

			int x = (screenWidth - imageWidth) / 2;
			int y = minY - imageHeight - imageHeight;

			context.blit(RenderPipelines.GUI_TEXTURED, Titan.UPDATING_SATURN, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
		}

		if (this.currentProgress < 0.6F)
			return;

		for (Runnable runnable : Saturn.queuedProcesses)
			runnable.run();
		Saturn.queuedProcesses.clear();
	}

}
