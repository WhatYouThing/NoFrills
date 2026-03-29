package nofrills.events;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;
import static nofrills.Main.mc;

public class WorldRenderEvent {
    public static final MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(new ByteBufferBuilder(2048));

    private static final RenderPipeline DEBUG_FILLED_BOX_NO_CULL_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/nofrills_debug_filled_box_no_cull")
                    .withDepthStencilState(new DepthStencilState(CompareOp.NOT_EQUAL, false))
                    .build()
    );
    private static final RenderPipeline LINES_TRANSLUCENT_NO_CULL_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withDepthStencilState(new DepthStencilState(CompareOp.NOT_EQUAL, false))
                    .withLocation("pipeline/nofrills_lines_translucent_no_cull")
                    .build()
    );
    private static final RenderType DEBUG_FILLED_BOX_NO_CULL = RenderType.create(
            "nofrills_debug_filled_box_no_cull",
            RenderSetup.builder(DEBUG_FILLED_BOX_NO_CULL_PIPELINE)
                    .sortOnUpload()
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );
    private static final RenderType LINES_TRANSLUCENT_NO_CULL = RenderType.create(
            "nofrills_lines_translucent_no_cull",
            RenderSetup.builder(LINES_TRANSLUCENT_NO_CULL_PIPELINE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()
    );

    public DeltaTracker tickCounter;
    public CameraRenderState camera;
    public PoseStack matrices;
    public net.minecraft.client.renderer.state.level.LevelRenderState state;

    public WorldRenderEvent(DeltaTracker tickCounter, CameraRenderState camera, PoseStack matrices, net.minecraft.client.renderer.state.level.LevelRenderState state) {
        this.tickCounter = tickCounter;
        this.camera = camera;
        this.matrices = matrices;
        this.state = state;
    }

    private void drawQuad(Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, VertexConsumer consumer, RenderColor color) {
        PoseStack.Pose entry = this.matrices.last();
        Vec3 camPos = this.camera.pos;
        consumer.addVertex(entry, (float) (first.x() - camPos.x()), (float) (first.y() - camPos.y()), (float) (first.z() - camPos.z())).setColor(color.argb);
        consumer.addVertex(entry, (float) (second.x() - camPos.x()), (float) (second.y() - camPos.y()), (float) (second.z() - camPos.z())).setColor(color.argb);
        consumer.addVertex(entry, (float) (third.x() - camPos.x()), (float) (third.y() - camPos.y()), (float) (third.z() - camPos.z())).setColor(color.argb);
        consumer.addVertex(entry, (float) (fourth.x() - camPos.x()), (float) (fourth.y() - camPos.y()), (float) (fourth.z() - camPos.z())).setColor(color.argb);
    }

    private void drawLine(Vec3 start, Vec3 end, float width, VertexConsumer consumer, RenderColor color) {
        PoseStack.Pose entry = this.matrices.last();
        Vec3 camPos = this.camera.pos;
        Vector4f vector4f = new Vector4f().set(start.x() - camPos.x(), start.y() - camPos.y(), start.z() - camPos.z(), 1.0);
        Vector4f vector4f2 = new Vector4f().set(end.x() - camPos.x(), end.y() - camPos.y(), end.z() - camPos.z(), 1.0);
        consumer.addVertex(entry, vector4f.x, vector4f.y, vector4f.z)
                .setNormal(entry, vector4f2.x - vector4f.x, vector4f2.y - vector4f.y, vector4f2.z - vector4f.z)
                .setColor(color.argb)
                .setLineWidth(width);
        consumer.addVertex(entry, vector4f2.x, vector4f2.y, vector4f2.z)
                .setNormal(entry, vector4f2.x - vector4f.x, vector4f2.y - vector4f.y, vector4f2.z - vector4f.z)
                .setColor(color.argb)
                .setLineWidth(width);
    }

    public void drawFilled(AABB box, boolean throughWalls, RenderColor color) {
        VertexConsumer consumer = throughWalls ? immediate.getBuffer(DEBUG_FILLED_BOX_NO_CULL) : immediate.getBuffer(RenderTypes.debugFilledBox());
        double d = box.minX;
        double e = box.minY;
        double f = box.minZ;
        double g = box.maxX;
        double h = box.maxY;
        double i = box.maxZ;
        this.drawQuad(new Vec3(g, e, f), new Vec3(g, h, f), new Vec3(g, h, i), new Vec3(g, e, i), consumer, color);
        this.drawQuad(new Vec3(d, e, f), new Vec3(d, e, i), new Vec3(d, h, i), new Vec3(d, h, f), consumer, color);
        this.drawQuad(new Vec3(d, e, f), new Vec3(d, h, f), new Vec3(g, h, f), new Vec3(g, e, f), consumer, color);
        this.drawQuad(new Vec3(d, e, i), new Vec3(g, e, i), new Vec3(g, h, i), new Vec3(d, h, i), consumer, color);
        this.drawQuad(new Vec3(d, h, f), new Vec3(d, h, i), new Vec3(g, h, i), new Vec3(g, h, f), consumer, color);
        this.drawQuad(new Vec3(d, e, f), new Vec3(g, e, f), new Vec3(g, e, i), new Vec3(d, e, i), consumer, color);
    }

    public void drawOutline(AABB box, boolean throughWalls, RenderColor color) {
        VertexConsumer consumer = throughWalls ? immediate.getBuffer(LINES_TRANSLUCENT_NO_CULL) : immediate.getBuffer(RenderTypes.lines());
        double d = box.minX;
        double e = box.minY;
        double f = box.minZ;
        double g = box.maxX;
        double h = box.maxY;
        double i = box.maxZ;
        this.drawLine(new Vec3(d, e, f), new Vec3(g, e, f), 3.0f, consumer, color);
        this.drawLine(new Vec3(d, e, f), new Vec3(d, h, f), 3.0f, consumer, color);
        this.drawLine(new Vec3(d, e, f), new Vec3(d, e, i), 3.0f, consumer, color);
        this.drawLine(new Vec3(g, e, f), new Vec3(g, h, f), 3.0f, consumer, color);
        this.drawLine(new Vec3(g, h, f), new Vec3(d, h, f), 3.0f, consumer, color);
        this.drawLine(new Vec3(d, h, f), new Vec3(d, h, i), 3.0f, consumer, color);
        this.drawLine(new Vec3(d, h, i), new Vec3(d, e, i), 3.0f, consumer, color);
        this.drawLine(new Vec3(d, e, i), new Vec3(g, e, i), 3.0f, consumer, color);
        this.drawLine(new Vec3(g, e, i), new Vec3(g, e, f), 3.0f, consumer, color);
        this.drawLine(new Vec3(d, h, i), new Vec3(g, h, i), 3.0f, consumer, color);
        this.drawLine(new Vec3(g, e, i), new Vec3(g, h, i), 3.0f, consumer, color);
        this.drawLine(new Vec3(g, h, f), new Vec3(g, h, i), 3.0f, consumer, color);
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
        Matrix4f matrices = new Matrix4f();
        Vec3 camPos = this.camera.pos;
        float textX = (float) (pos.x() - camPos.x());
        float textY = (float) (pos.y() - camPos.y());
        float textZ = (float) (pos.z() - camPos.z());
        matrices.translate(textX, textY, textZ);
        matrices.rotate(camera.orientation);
        matrices.scale(scale, -scale, scale);
        mc.font.drawInBatch(text, -mc.font.width(text) / 2f, 1.0f, color.argb, true, matrices, immediate, throughWalls ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
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
        VertexConsumer consumer = immediate.getBuffer(LINES_TRANSLUCENT_NO_CULL);
        Vec3 point = this.camera.pos.add(Vec3.directionFromRotation(this.camera.xRot, this.camera.yRot));
        this.drawLine(point, pos, width, consumer, color);
    }

    public void drawTracer(Vec3 pos, RenderColor color) {
        this.drawTracer(pos, 4.0f, color);
    }
}
