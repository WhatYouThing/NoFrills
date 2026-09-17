package nofrills.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.gizmos.CuboidGizmo;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.LineGizmo;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;

import static net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;
import static nofrills.Main.mc;

public class WorldRenderEvent {
    public final DeltaTracker tickCounter = mc.getDeltaTracker();
    public final SimpleGizmoCollector gizmos;
    public final CameraRenderState camera;
    public final PoseStack matrices;
    public final LevelRenderState state;
    public final SubmitNodeStorage storage;

    public WorldRenderEvent(LevelRenderState state, PoseStack matrices, SubmitNodeStorage storage, SimpleGizmoCollector gizmos) {
        this.camera = state.cameraRenderState;
        this.matrices = matrices;
        this.state = state;
        this.storage = storage;
        this.gizmos = gizmos;
    }

    public void drawFilled(AABB box, boolean throughWalls, RenderColor color) {
        CuboidGizmo gizmo = new CuboidGizmo(box, GizmoStyle.fill(color.getArgb()), false);
        if (throughWalls) {
            gizmos.add(gizmo).setAlwaysOnTop();
        } else {
            gizmos.add(gizmo);
        }
    }

    public void drawOutline(AABB box, boolean throughWalls, RenderColor color) {
        CuboidGizmo gizmo = new CuboidGizmo(box, GizmoStyle.stroke(color.getArgb()), false);
        if (throughWalls) {
            gizmos.add(gizmo).setAlwaysOnTop();
        } else {
            gizmos.add(gizmo);
        }
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
        PoseStack matrices = new PoseStack();
        Vec3 camPos = this.camera.pos;
        float textX = (float) (pos.x() - camPos.x());
        float textY = (float) (pos.y() - camPos.y());
        float textZ = (float) (pos.z() - camPos.z());
        matrices.translate(textX, textY, textZ);
        matrices.rotateAround(camera.orientation, 0.0f, 0.0f, 0.0f);
        matrices.scale(scale, -scale, scale);
        this.storage.submitText(matrices, -mc.font.width(text) / 2f, 1.0f, text.getVisualOrderText(), true, throughWalls ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, FULL_BRIGHT, color.argb, 0, 0);
    }

    public void drawDistanceScaledText(Vec3 pos, Component text, float baseScale, float scaling, boolean throughWalls, RenderColor color) {
        double dist = this.camera.pos.distanceTo(pos);
        float distScale = (float) (1 + dist * scaling);
        float scale = Math.max(baseScale * distScale, baseScale);
        this.drawText(pos.add(0.0, dist * baseScale, 0.0), text, scale, throughWalls, color);
    }

    public void drawDistanceScaledText(Vec3 pos, Component text, float baseScale, boolean throughWalls, RenderColor color) {
        this.drawDistanceScaledText(pos, text, baseScale, 0.1f, throughWalls, color);
    }

    public void drawBeam(Vec3 pos, int height, boolean throughWalls, RenderColor color) {
        this.drawFilled(AABB.ofSize(pos, 0.5, 0, 0.5).expandTowards(0, height, 0), throughWalls, color);
    }

    public void drawFilledWithBeam(AABB box, int height, boolean throughWalls, RenderColor color) {
        Vec3 center = box.getCenter();
        this.drawFilled(box, throughWalls, color);
        this.drawBeam(center.add(0, box.maxY - center.y(), 0), height, throughWalls, color);
    }

    public void drawTracer(Vec3 pos, float width, RenderColor color) {
        Vec3 point = this.camera.pos.add(Vec3.directionFromRotation(this.camera.xRot, this.camera.yRot));
        gizmos.add(new LineGizmo(point, pos, color.argb, width)).setAlwaysOnTop();
    }

    public void drawTracer(Vec3 pos, RenderColor color) {
        this.drawTracer(pos, 4.0f, color);
    }

    public float delta() {
        return this.tickCounter.getGameTimeDeltaPartialTick(true);
    }
}
