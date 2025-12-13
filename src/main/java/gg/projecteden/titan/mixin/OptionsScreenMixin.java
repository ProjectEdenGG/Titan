package gg.projecteden.titan.mixin;

import gg.projecteden.titan.Titan;
import gg.projecteden.titan.saturn.Saturn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static gg.projecteden.titan.Titan.PE_LOGO_IDEN;
import static gg.projecteden.titan.Titan.UPDATE_AVAILABLE;

@Mixin(PackSelectionScreen.class)
public class OptionsScreenMixin extends Screen {

	@Shadow @Final private HeaderAndFooterLayout layout;
	@Shadow private TransferableSelectionList availablePackList;
	@Shadow private TransferableSelectionList selectedPackList;
	@Unique
	private boolean updateAvailable;
	@Unique
	private SpriteIconButton button;
	@Unique
	private Renderable updateIcon;

    @Unique
	Button.OnPress action = button -> {
		if (updateAvailable) {
			Saturn.queueProcess(() -> {
				if (Saturn.update())
					Minecraft.getInstance().reloadResourcePacks();
			});
			Minecraft.getInstance().reloadResourcePacks();
		}
	};

	protected OptionsScreenMixin(Component title) {
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

		button = this.addRenderableWidget(SpriteIconButton.builder(Component.nullToEmpty(""), action, true)
				.width(20)
				.sprite(PE_LOGO_IDEN, 20, 20)
				.build());
		button.setPosition(this.width - 26, 6);
		button.setTooltip(Tooltip.create(Component.literal(tooltipText)));

		if (updateAvailable || Titan.debug)
			renderUpdateIcon();
		else
			updateIcon = null;

	}

	@Inject(method = "repositionElements", at = @At("RETURN"))
	void refreshWidgetPositions(CallbackInfo ci) {
		if (button != null)
			button.setPosition(this.width - 26, 6);
	}

	@Unique
	private void renderUpdateIcon() {
		updateIcon = this.addRenderableOnly((context, mouseX, mouseY, delta) -> {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, UPDATE_AVAILABLE, this.width - 8, 0, 5, 20);
		});
	}

}
