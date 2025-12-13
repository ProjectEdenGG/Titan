package gg.projecteden.titan.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiGraphics.class)
public interface DrawContextMixin {

    @Accessor("guiRenderState")
    GuiRenderState getRenderState();

    @Accessor("scissorStack")
    GuiGraphics.ScissorStack getScissorStack();
}
