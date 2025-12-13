package gg.projecteden.titan.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.projecteden.titan.config.ConfigItem;
import gg.projecteden.titan.network.clientbound.BackpackConfig;
import gg.projecteden.titan.utils.InventoryOverlay;
import gg.projecteden.titan.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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

@Mixin(AbstractContainerScreen.class)
public class HandledScreenMixin {
    @Shadow @Nullable protected Slot hoveredSlot;

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"), cancellable = true)
    private void onRenderTooltip(GuiGraphics drawContext, int x, int y, CallbackInfo ci) {
        if (!ConfigItem.DO_BACKPACK_PREVIEWS.getValue())
            return;

        if (ConfigItem.PREVIEWS_REQUIRE_SHIFT.getValue() && !Utils.isShiftPressed())
            return;

        if (this.hoveredSlot != null && this.hoveredSlot.hasItem())
            onRenderTooltipLast(drawContext, this.hoveredSlot.getItem(), x, y, ci);
    }

    @Unique
    private void onRenderTooltipLast(GuiGraphics context, ItemStack stack, int x, int y, CallbackInfo ci) {
        if (getStoredItems(Minecraft.getInstance().player.level().registryAccess(), stack).isEmpty()) {
            return;
        }

        renderItemContentsPreview(stack, x, y, context, ci);
    }

    @Unique
    public void renderItemContentsPreview(ItemStack stack, int baseX, int baseY, GuiGraphics drawContext, CallbackInfo ci) {
        NonNullList<ItemStack> items = getStoredItems(Minecraft.getInstance().player.level().registryAccess(), stack);

        InventoryOverlay.InventoryRenderType type = getType(stack);
        InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(type);

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int height = props.height + 18;
        int x = Mth.clamp(baseX + 8     , 0, screenWidth - props.width);
        int y = Mth.clamp(baseY - height, 0, screenHeight - height);

        if (baseY - height != y) { // it has been clamped to not go off the screen - cancel rendering the actual tooltip because it renders on top
            ci.cancel();
        }

        Color color;
        if (ConfigItem.USE_BACKGROUND_COLORS.getValue() && stack.getComponents().has(DataComponents.DYED_COLOR))
            color = new Color(stack.get(DataComponents.DYED_COLOR).rgb());
        else
            color = Color.WHITE;

        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        matrixStack.translate(0, 0, 500);

        InventoryOverlay.renderInventoryBackground(drawContext, type, x, y, color.getRGB(), Minecraft.getInstance());

        Container inv = getAsInventory(items);
        InventoryOverlay.renderInventoryStacks(type, inv, x + props.slotOffsetX, y + props.slotOffsetY, props.slotsPerRow, 0, type.getMaxSlots(), Minecraft.getInstance(), drawContext);

        matrixStack.popMatrix();
    }

    @Unique
    private InventoryOverlay.InventoryRenderType getType(ItemStack stack) {
        if (BackpackConfig.ENTRIES == null)
            return InventoryOverlay.InventoryRenderType.FIXED_27;

        CustomData component = stack.get(DataComponents.CUSTOM_DATA);
        if (component == null) return null;

        CompoundTag nbt = component.copyTag();

        for (BackpackConfig.Entry entry : BackpackConfig.ENTRIES)
            if (nbt.contains(entry.getType()))
                return entry.getRenderType();

        return InventoryOverlay.InventoryRenderType.FIXED_27;
    }

    @Unique
    private Container getAsInventory(List<ItemStack> items) {
        SimpleContainer inv = new SimpleContainer(items.size());

        for (int slot = 0; slot < items.size(); ++slot)
            inv.setItem(slot, items.get(slot));

        return inv;
    }

}
