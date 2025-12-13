package gg.projecteden.titan.mixin;

import gg.projecteden.titan.config.ConfigItem;
import gg.projecteden.titan.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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

    @Shadow public abstract DataComponentMap getComponents();

    @Shadow public abstract ItemStack copy();

    @Unique
    private static final Component HOVER = Component.literal("Hold ").withStyle(ChatFormatting.DARK_AQUA)
            .append(Component.literal("Shift").withStyle(ChatFormatting.YELLOW))
            .append(" to view contents").withStyle(ChatFormatting.DARK_AQUA);

    @Inject(at = @At("RETURN"), method = "getTooltipLines")
    private void addBackpackPreviewLore(Item.TooltipContext context, Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> ci) {
        if (!ConfigItem.DO_BACKPACK_PREVIEWS.getValue())
            return;

        if (!ConfigItem.PREVIEWS_REQUIRE_SHIFT.getValue() || Utils.isShiftPressed())
            return;

        if (!this.getComponents().has(DataComponents.CUSTOM_DATA))
            return;

        if (player == null)
            return;

        if (getStoredItems(player.level().registryAccess(), this.copy()).isEmpty())
            return;

        var tooltip = ci.getReturnValue();
        tooltip.add(Component.empty());
        tooltip.add(HOVER);
    }
}
