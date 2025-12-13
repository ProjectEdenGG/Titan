package gg.projecteden.titan.mixin;

import gg.projecteden.titan.config.ConfigItem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static gg.projecteden.titan.utils.Utils.getStoredItems;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract ComponentMap getComponents();

    @Shadow public abstract ItemStack copy();

    @Unique
    private static final Text HOVER = Text.literal("Hold ").formatted(Formatting.DARK_AQUA)
            .append(Text.literal("Shift").formatted(Formatting.YELLOW))
            .append(" to view contents").formatted(Formatting.DARK_AQUA);

    @Inject(at = @At("RETURN"), method = "getTooltip")
    private void addBackpackPreviewLore(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> ci) {
        if (!ConfigItem.DO_BACKPACK_PREVIEWS.getValue())
            return;

        if (!ConfigItem.PREVIEWS_REQUIRE_SHIFT.getValue() || Screen.hasShiftDown())
            return;

        if (!this.getComponents().contains(DataComponentTypes.CUSTOM_DATA))
            return;

        if (player == null)
            return;

        if (getStoredItems(player.getEntityWorld().getRegistryManager(), this.copy()).isEmpty())
            return;

        var tooltip = ci.getReturnValue();
        tooltip.add(Text.empty());
        tooltip.add(HOVER);
    }
}
