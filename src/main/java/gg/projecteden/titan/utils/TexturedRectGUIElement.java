package gg.projecteden.titan.utils;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;

public record TexturedRectGUIElement(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int x,
        int y,
        int u,
        int v,
        int width,
        int height,
        int argb,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds
) implements GuiElementRenderState {
    public TexturedRectGUIElement(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x, int y, int u, int v, int width, int height, int argb, ScreenRectangle scissorArea)
    {
        this(pipeline, textureSetup, pose, x, y, u, v, width, height, argb, scissorArea, createBounds(x, y, x + width, y + height, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer vertices) {
        float pixelWidth = 0.00390625F;

        vertices.addVertexWith2DPose(this.pose(), this.x(), this.y() + this.height()).setUv(this.u() * pixelWidth, (this.v() + this.height()) * pixelWidth).setColor(this.argb());
        vertices.addVertexWith2DPose(this.pose(), this.x() + this.width(), this.y() + this.height()).setUv((this.u() + this.width()) * pixelWidth, (this.v() + this.height()) * pixelWidth).setColor(this.argb());
        vertices.addVertexWith2DPose(this.pose(), this.x() + this.width(), this.y()).setUv((this.u() + this.width()) * pixelWidth, this.v() * pixelWidth).setColor(this.argb());
        vertices.addVertexWith2DPose(this.pose(), this.x(), this.y()).setUv(this.u() * pixelWidth, this.v() * pixelWidth).setColor(this.argb());
    }

    private static ScreenRectangle createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, ScreenRectangle scissorArea) {
        ScreenRectangle screenRect = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }
}
