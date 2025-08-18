package gg.projecteden.titan.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.projecteden.titan.config.ConfigItem;
import gg.projecteden.titan.utils.InventoryOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;

import static gg.projecteden.titan.utils.Utils.getStoredItems;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    @Shadow @Nullable protected Slot focusedSlot;

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V"), cancellable = true)
    private void onRenderTooltip(DrawContext drawContext, int x, int y, CallbackInfo ci) {
        if (!ConfigItem.DO_BACKPACK_PREVIEWS.getValue())
            return;

        if (ConfigItem.PREVIEWS_REQUIRE_SHIFT.getValue() && !Screen.hasShiftDown())
            return;

        if (this.focusedSlot != null && this.focusedSlot.hasStack())
            onRenderTooltipLast(drawContext, this.focusedSlot.getStack(), x, y, ci);
    }

    @Unique
    private void onRenderTooltipLast(DrawContext context, ItemStack stack, int x, int y, CallbackInfo ci) {
        if (getStoredItems(MinecraftClient.getInstance().player.getWorld().getRegistryManager(), stack).isEmpty()) {
            return;
        }

        renderItemContentsPreview(stack, x, y, context, ci);
    }

    @Unique
    public void renderItemContentsPreview(ItemStack stack, int baseX, int baseY, DrawContext drawContext, CallbackInfo ci) {
        DefaultedList<ItemStack> items = getStoredItems(MinecraftClient.getInstance().player.getWorld().getRegistryManager(), stack);

        InventoryOverlay.InventoryRenderType type = getType(stack);
        InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(type);

        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int height = props.height + 18;
        int x = MathHelper.clamp(baseX + 8     , 0, screenWidth - props.width);
        int y = MathHelper.clamp(baseY - height, 0, screenHeight - height);

        if (baseY - height != y) { // it has been clamped to not go off the screen - cancel rendering the actual tooltip because it renders on top
            ci.cancel();
        }

        Color color;
        if (ConfigItem.USE_BACKGROUND_COLORS.getValue() && stack.getComponents().contains(DataComponentTypes.DYED_COLOR))
            color = new Color(stack.get(DataComponentTypes.DYED_COLOR).rgb());
        else
            color = Color.WHITE;

        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        matrixStack.translate(0, 0, 500);

        InventoryOverlay.renderInventoryBackground(drawContext, type, x, y, color.getRGB(), MinecraftClient.getInstance());

        Inventory inv = getAsInventory(items);
        InventoryOverlay.renderInventoryStacks(type, inv, x + props.slotOffsetX, y + props.slotOffsetY, props.slotsPerRow, 0, type.getMaxSlots(), MinecraftClient.getInstance(), drawContext);

        matrixStack.popMatrix();
    }

    @Unique
    private InventoryOverlay.InventoryRenderType getType(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return null;

        NbtCompound nbt = component.copyNbt();
        if (nbt == null)
            return InventoryOverlay.InventoryRenderType.FIXED_27;

        if (nbt.contains("BP_TIER_NETHERITE") || nbt.contains("BP_TIER_DIAMOND"))
            return InventoryOverlay.InventoryRenderType.FIXED_54;
        if (nbt.contains("BP_TIER_GOLD"))
            return InventoryOverlay.InventoryRenderType.FIXED_45;
        if (nbt.contains("BP_TIER_IRON"))
            return InventoryOverlay.InventoryRenderType.FIXED_36;
        return InventoryOverlay.InventoryRenderType.FIXED_27;
    }

    @Unique
    private Inventory getAsInventory(List<ItemStack> items) {
        SimpleInventory inv = new SimpleInventory(items.size());

        for (int slot = 0; slot < items.size(); ++slot)
            inv.setStack(slot, items.get(slot));

        return inv;
    }

}
