package gg.projecteden.titan.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiGraphicsExtractor.class)
public interface DrawContextMixin {

    @Accessor("guiRenderState")
    GuiRenderState getRenderState();

    @Accessor("scissorStack")
    GuiGraphicsExtractor.ScissorStack getScissorStack();
}
