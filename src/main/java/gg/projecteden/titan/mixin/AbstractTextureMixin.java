package gg.projecteden.titan.mixin;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractTexture.class)
public interface AbstractTextureMixin {

    @Accessor("textureView")
    GpuTextureView getGlTextureView();

}
