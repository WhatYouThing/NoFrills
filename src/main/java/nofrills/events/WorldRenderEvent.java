package nofrills.events;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import nofrills.misc.Rendering;

public class WorldRenderEvent {
    public MultiBufferSource.BufferSource consumer;
    public DeltaTracker tickCounter;
    public CameraRenderState cameraState;
    public PoseStack matrices;

    public WorldRenderEvent(MultiBufferSource.BufferSource consumer, DeltaTracker tickCounter, CameraRenderState cameraState, PoseStack matrices) {
        this.consumer = consumer;
        this.tickCounter = tickCounter;
        this.cameraState = cameraState;
        this.matrices = matrices;
    }

    public void drawFilled(AABB box, boolean throughWalls, RenderColor color) {
        Rendering.drawFilled(box, throughWalls, color);
    }

    public void drawOutline(AABB box, boolean throughWalls, RenderColor color) {
        Rendering.drawOutline(box, throughWalls, color);
    }

    public void drawStyled(AABB box, RenderStyle style, boolean throughWalls, RenderColor outlineColor, RenderColor filledColor) {
        if (!style.equals(RenderStyle.Outline)) {
            this.drawFilled(box, throughWalls, filledColor);
        }
        if (!style.equals(RenderStyle.Filled)) {
            this.drawOutline(box, throughWalls, outlineColor);
        }
    }

    public void drawText(Vec3 pos, Component text, float scale, boolean throughWalls, RenderColor color) {
        Rendering.drawText(consumer, cameraState, pos, text, scale, throughWalls, color);
    }

    public void drawBeam(Vec3 pos, int height, boolean throughWalls, RenderColor color) {
        Rendering.drawBeam(pos, height, throughWalls, color);
    }

    public void drawFilledWithBeam(AABB box, int height, boolean throughWalls, RenderColor color) {
        Rendering.drawFilled(box, throughWalls, color);
        Vec3 center = box.getCenter();
        Rendering.drawBeam(center.add(0, box.maxY - center.y(), 0), height, throughWalls, color);
    }

    public void drawTracer(Vec3 pos, RenderColor color) {
        Rendering.drawTracer(matrices, consumer, cameraState, pos, color);
    }
}
