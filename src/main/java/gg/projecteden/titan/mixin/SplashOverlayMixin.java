package gg.projecteden.titan.mixin;

import gg.projecteden.titan.saturn.Saturn;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
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
	private void start(DrawContext matrices, int minX, int minY, int maxX, int maxY, float opacity, CallbackInfo ci) {
		if (this.progress > 0.5F) {
			for (Runnable runnable : Saturn.queuedProcesses)
				runnable.run();
			Saturn.queuedProcesses.clear();
		}
	}

}
