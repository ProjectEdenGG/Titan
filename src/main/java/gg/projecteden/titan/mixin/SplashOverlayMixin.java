package gg.projecteden.titan.mixin;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.saturn.Saturn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public class SplashOverlayMixin {

	@Shadow
	private float currentProgress;

	@Inject(method = "drawProgressBar", at = @At("RETURN"))
	private void start(GuiGraphics context, int minX, int minY, int maxX, int maxY, float opacity, CallbackInfo ci) {
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
