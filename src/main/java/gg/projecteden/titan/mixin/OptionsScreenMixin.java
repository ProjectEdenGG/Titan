package gg.projecteden.titan.mixin;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.saturn.Saturn;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static gg.projecteden.titan.Titan.PE_LOGO_IDEN;
import static gg.projecteden.titan.Titan.UPDATE_AVAILABLE;

@Mixin(PackScreen.class)
public class OptionsScreenMixin extends Screen {

	@Shadow @Final private ThreePartsLayoutWidget layout;
	@Shadow private PackListWidget availablePackList;
	@Shadow private PackListWidget selectedPackList;
	@Unique
	private boolean updateAvailable;
	@Unique
	private TextIconButtonWidget button;
	@Unique
	private Drawable updateIcon;

    @Unique
	ButtonWidget.PressAction action = button -> {
		if (updateAvailable) {
			Saturn.queueProcess(() -> {
				if (Saturn.update())
					MinecraftClient.getInstance().reloadResources();
			});
			MinecraftClient.getInstance().reloadResources();
		}
	};

	protected OptionsScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	public void drawSaturnUpdateChecker(CallbackInfo ci) {
		updateAvailable = Saturn.checkForUpdates();
        String saturnVersion = Saturn.shortVersion();

		String tooltipText = "Saturn installed with Titan\n" +
				"Version: " + saturnVersion;
		if (updateAvailable) {
			tooltipText +=
					"""
                        \nThere is an update available
                        Click to download
                        """;
		}

		button = this.addDrawableChild(TextIconButtonWidget.builder(Text.of(""), action, true)
				.width(20)
				.texture(PE_LOGO_IDEN, 20, 20)
				.build());
		button.setPosition(this.width - 26, 6);
		button.setTooltip(Tooltip.of(Text.literal(tooltipText)));

		if (updateAvailable || Titan.debug)
			renderUpdateIcon();
		else
			updateIcon = null;

	}

	@Inject(method = "refreshWidgetPositions", at = @At("RETURN"))
	void refreshWidgetPositions(CallbackInfo ci) {
		if (button != null)
			button.setPosition(this.width - 26, 6);
	}

	@Unique
	private void renderUpdateIcon() {
		updateIcon = this.addDrawable((context, mouseX, mouseY, delta) -> {
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, UPDATE_AVAILABLE, this.width - 8, 0, 5, 20);
		});
	}

}
