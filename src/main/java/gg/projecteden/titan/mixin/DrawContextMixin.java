package gg.projecteden.titan.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DrawContext.class)
public interface DrawContextMixin {

    @Accessor("state")
    GuiRenderState getRenderState();

    @Accessor("scissorStack")
    DrawContext.ScissorStack getScissorStack();
}
