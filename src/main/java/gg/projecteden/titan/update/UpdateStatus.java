package gg.projecteden.titan.update;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public enum UpdateStatus {
	NONE(Tooltip.create(Component.nullToEmpty("Connect directly to projecteden.gg"))),
	AVAILABLE(Tooltip.create(Component.nullToEmpty("An update for Titan is available. Shift-Click to open the mod's page")));

	final Tooltip titleScreenTooltip;

	UpdateStatus(Tooltip titleScreenTooltip) {
		this.titleScreenTooltip = titleScreenTooltip;
	}

	public Tooltip getTitleScreenTooltip() {
		return this.titleScreenTooltip;
	}

}
