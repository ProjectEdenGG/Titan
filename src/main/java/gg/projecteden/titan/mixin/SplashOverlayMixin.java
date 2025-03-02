package gg.projecteden.titan.mixin;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.saturn.Saturn;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {

	@Shadow
	private float progress;

	@Inject(method = "renderProgressBar", at = @At("RETURN"))
	private void start(DrawContext context, int minX, int minY, int maxX, int maxY, float opacity, CallbackInfo ci) {
		if (this.progress < 0.5F)
			return;

		if (!Saturn.queuedProcesses.isEmpty() || Titan.debug) {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client == null || client.getWindow() == null) return;

			int baseScreenWidth = 1920;
			int baseImageWidth = 261;
			int baseImageHeight = 19;

			int screenWidth = client.getWindow().getScaledWidth();

			float scale = Math.min(screenWidth / (float) baseScreenWidth, 1.0f);
			scale = Math.max(scale, 0.5f);

			int imageWidth = (int) (baseImageWidth * scale);
			int imageHeight = (int) (baseImageHeight * scale);

			int x = (screenWidth - imageWidth) / 2;
			int y = minY - imageHeight - imageHeight;

			context.drawTexture(id -> RenderLayer.getGuiTextured(Titan.UPDATING_SATURN),
					Titan.UPDATING_SATURN, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
		}

		for (Runnable runnable : Saturn.queuedProcesses)
			runnable.run();
		Saturn.queuedProcesses.clear();
	}

}
