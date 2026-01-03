package nofrills.events;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.TextGizmo;
import nofrills.misc.RenderColor;

public class WorldRenderEvent {
    public RenderTickCounter tickCounter;
    public Camera camera;
    public MatrixStack matrices;
    public WorldRenderer.Gizmos gizmos;
    public WorldRenderState state;

    public WorldRenderEvent(RenderTickCounter tickCounter, Camera camera, MatrixStack matrices, WorldRenderer.Gizmos gizmos, WorldRenderState state) {
        this.tickCounter = tickCounter;
        this.camera = camera;
        this.matrices = matrices;
        this.gizmos = gizmos;
        this.state = state;
    }

    public void drawFilled(Box box, boolean throughWalls, RenderColor color) {
        DrawStyle style = DrawStyle.filled(color.argb);
        if (throughWalls) {
            GizmoDrawing.box(box, style).ignoreOcclusion();
        } else {
            GizmoDrawing.box(box, style);
        }
    }

    public void drawOutline(Box box, boolean throughWalls, RenderColor color) {
        DrawStyle style = DrawStyle.stroked(color.argb);
        if (throughWalls) {
            GizmoDrawing.box(box, style).ignoreOcclusion();
        } else {
            GizmoDrawing.box(box, style);
        }
    }

    public void drawText(Vec3d pos, Text text, float scale, boolean throughWalls, RenderColor color) {
        TextGizmo.Style style = TextGizmo.Style.centered(color.argb).scaled(scale);
        if (throughWalls) {
            GizmoDrawing.text(text.getString(), pos, style).ignoreOcclusion();
        } else {
            GizmoDrawing.text(text.getString(), pos, style);
        }
    }

    public void drawBeam(Vec3d pos, int height, boolean throughWalls, RenderColor color) {
        this.drawFilled(Box.of(pos, 0.5, 0, 0.5).stretch(0, height, 0), throughWalls, color);
    }

    public void drawFilledWithBeam(Box box, int height, boolean throughWalls, RenderColor color) {
        Vec3d center = box.getCenter();
        this.drawFilled(box, throughWalls, color);
        this.drawBeam(center.add(0, box.maxY - center.getY(), 0), height, throughWalls, color);
    }

    public void drawTracer(Vec3d pos, float width, RenderColor color) {
        Vec3d point = this.camera.getCameraPos().add(Vec3d.fromPolar(this.camera.getPitch(), this.camera.getYaw()));
        GizmoDrawing.line(point, pos, color.argb, width);
    }

    public void drawTracer(Vec3d pos, RenderColor color) {
        this.drawTracer(pos, 4.0f, color);
    }
}
