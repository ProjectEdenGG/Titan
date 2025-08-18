package gg.projecteden.titan.utils;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTextureView;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.mixin.DrawContextMixin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

// https://github.com/sakura-ryoko/malilib/blob/1.21.8/src/main/java/fi/dy/masa/malilib/render/InventoryOverlay.java
public class InventoryOverlay {
    public static final Identifier TEXTURE_54 = Identifier.ofVanilla("textures/gui/container/generic_54.png");

    public static final InventoryProperties INV_PROPS_TEMP = new InventoryProperties();


    public static void renderInventoryBackground(DrawContext context, InventoryRenderType type, int x, int y, int color, MinecraftClient mc) {
        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(770, 771, 1, 0);

        int rows = switch (type) {
            case FIXED_27 -> 0;
            case FIXED_36 -> 1;
            case FIXED_45 -> 2;
            case FIXED_54 -> 3;
        };

        int h1 = 61 + (rows * 18);
        int h2 = 54 + (rows * 18);

        renderInventoryBackground(context, x, y, h1, h2, color, mc);
    }

    public static void renderInventoryBackground(DrawContext context, int x, int y, int h1, int h2, int color, MinecraftClient mc) {
        ResourceTexture tex = (ResourceTexture) mc.getTextureManager().getTexture(TEXTURE_54);
        if (tex == null)
            return;
        GpuTextureView gpuTextureView = tex.getGlTextureView();
        if (gpuTextureView == null) return;

        drawTexturedRectBatched(context, gpuTextureView, x      , y     ,   0,   0,   7,  h1, color); // left (top)
        drawTexturedRectBatched(context, gpuTextureView, x +   7, y     ,   7,   0, 169,   7, color); // top (right)
        drawTexturedRectBatched(context, gpuTextureView, x + 169, y +  7, 169, 107,   7,  h1 - 1, color); // right (bottom)
        drawTexturedRectBatched(context, gpuTextureView, x      , y + h1,   0, 215, 180,   7, color); // bottom (left)
        drawTexturedRectBatched(context, gpuTextureView, x +   7, y +  7,   7,  17, 162,  h2, color); // middle
    }

    public static void drawTexturedRectBatched(DrawContext drawContext, GpuTextureView gpuTextureView, int x, int y, int u, int v, int width, int height, int argb)
    {
        addSimpleElement(drawContext,
                new TexturedRectGUIElement(
                        RenderPipelines.GUI_TEXTURED,
                        TextureSetup.withoutGlTexture(gpuTextureView),
                        new Matrix3x2f(drawContext.getMatrices()),
                        x, y, u, v,
                        width, height, argb,
                        peekLastScissor(drawContext))
        );
    }

    public static void addSimpleElement(DrawContext drawContext, SimpleGuiElementRenderState simpleElement) {
        ((DrawContextMixin) drawContext).getRenderState().addSimpleElement(simpleElement);
    }

    public static ScreenRect peekLastScissor(DrawContext drawContext) {
        return ((DrawContextMixin) drawContext).getScissorStack().peekLast();
    }

    /**
     * Returns the instance of the shared/temporary properties instance,
     * with the values set for the type of inventory provided.
     * Don't hold on to the instance, as the values will mutate when this
     * method is called again!
     * @param type
     * @return
     */
    public static InventoryProperties getInventoryPropsTemp(InventoryRenderType type) {
        int totalSlots = Integer.parseInt(type.name().replace("FIXED_", ""));

        INV_PROPS_TEMP.slotsPerRow = 9;
        INV_PROPS_TEMP.slotOffsetX = 8;
        INV_PROPS_TEMP.slotOffsetY = 8;
        int rows = (int) (Math.ceil((double) totalSlots / (double) INV_PROPS_TEMP.slotsPerRow));
        INV_PROPS_TEMP.width = Math.min(INV_PROPS_TEMP.slotsPerRow, totalSlots) * 18 + 14;
        INV_PROPS_TEMP.height = rows * 18 + 14;

        return INV_PROPS_TEMP;
    }

    public static void renderInventoryStacks(InventoryRenderType type, Inventory inv, int startX, int startY, int slotsPerRow, int startSlot, int maxSlots, MinecraftClient mc, DrawContext drawContext) {
        final int slots = inv.size();
        int x = startX;
        int y = startY;

        if (maxSlots < 0) {
            maxSlots = slots;
        }

        Titan.debug("RenderInventoryStacks: " + maxSlots);

        for (int slot = startSlot; slot < maxSlots;) {
            ItemStack stack = inv.getStack(slot).copy();

            if (!stack.isEmpty())
                renderStackAt(drawContext, stack, x, y, 1, mc);

            x += 18;
            slot++;

            if (slot % slotsPerRow == 0) {
                Titan.debug("Rendering on next row");
                x = startX;
                y += 18;
            }
            else
                Titan.debug("Rendering on next column");
        }
    }

    public static void renderStackAt(DrawContext drawContext, ItemStack stack, float x, float y, float scale, MinecraftClient mc) {
        Matrix3x2fStack matrixStack = drawContext.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(x, y);
        matrixStack.scale(scale, scale);

        drawContext.drawItem(stack.copy(), 0, 0);
        drawContext.drawStackOverlay(mc.textRenderer, stack.copy(), 0, 0);

        matrixStack.popMatrix();
    }

    public static class InventoryProperties {
        public int width = 176;
        public int height = 83;
        public int slotsPerRow = 9;
        public int slotOffsetX = 8;
        public int slotOffsetY = 8;
    }

    @Getter
    @AllArgsConstructor
    public enum InventoryRenderType {
        FIXED_27(27),
        FIXED_36(36),
        FIXED_45(45),
        FIXED_54(54);

        final int maxSlots;
    }
}