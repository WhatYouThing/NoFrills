package nofrills.events;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static nofrills.Main.mc;

public class WorldRenderEvent {
    public static final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(new BufferAllocator(2048));

    private static final RenderPipeline DEBUG_FILLED_BOX_NO_CULL_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withLocation("pipeline/nofrills_debug_filled_box_no_cull")
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build()
    );
    private static final RenderPipeline LINES_TRANSLUCENT_NO_CULL_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
                    .withDepthWrite(false)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withLocation("pipeline/nofrills_lines_translucent_no_cull")
                    .build()
    );
    private static final RenderLayer DEBUG_FILLED_BOX_NO_CULL = RenderLayer.of(
            "nofrills_debug_filled_box_no_cull",
            RenderSetup.builder(DEBUG_FILLED_BOX_NO_CULL_PIPELINE)
                    .translucent()
                    .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .build()
    );
    private static final RenderLayer LINES_TRANSLUCENT_NO_CULL = RenderLayer.of(
            "nofrills_lines_translucent_no_cull",
            RenderSetup.builder(LINES_TRANSLUCENT_NO_CULL_PIPELINE)
                    .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .outputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .build()
    );

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

    private void drawQuad(Vec3d first, Vec3d second, Vec3d third, Vec3d fourth, VertexConsumer consumer, RenderColor color) {
        MatrixStack.Entry entry = this.matrices.peek();
        Vec3d camPos = this.camera.getCameraPos();
        consumer.vertex(entry, (float) (first.getX() - camPos.getX()), (float) (first.getY() - camPos.getY()), (float) (first.getZ() - camPos.getZ())).color(color.argb);
        consumer.vertex(entry, (float) (second.getX() - camPos.getX()), (float) (second.getY() - camPos.getY()), (float) (second.getZ() - camPos.getZ())).color(color.argb);
        consumer.vertex(entry, (float) (third.getX() - camPos.getX()), (float) (third.getY() - camPos.getY()), (float) (third.getZ() - camPos.getZ())).color(color.argb);
        consumer.vertex(entry, (float) (fourth.getX() - camPos.getX()), (float) (fourth.getY() - camPos.getY()), (float) (fourth.getZ() - camPos.getZ())).color(color.argb);
    }

    private void drawLine(Vec3d start, Vec3d end, float width, VertexConsumer consumer, RenderColor color) {
        MatrixStack.Entry entry = this.matrices.peek();
        Vec3d camPos = this.camera.getCameraPos();
        Vector4f vector4f = new Vector4f();
        Vector4f vector4f2 = new Vector4f();
        vector4f.set(start.getX() - camPos.getX(), start.getY() - camPos.getY(), start.getZ() - camPos.getZ(), 1.0);
        vector4f2.set(end.getX() - camPos.getX(), end.getY() - camPos.getY(), end.getZ() - camPos.getZ(), 1.0);
        consumer.vertex(entry, vector4f.x, vector4f.y, vector4f.z)
                .normal(entry, vector4f2.x - vector4f.x, vector4f2.y - vector4f.y, vector4f2.z - vector4f.z)
                .color(color.argb)
                .lineWidth(width);
        consumer.vertex(entry, vector4f2.x, vector4f2.y, vector4f2.z)
                .normal(entry, vector4f2.x - vector4f.x, vector4f2.y - vector4f.y, vector4f2.z - vector4f.z)
                .color(color.argb)
                .lineWidth(width);
    }

    public void drawFilled(Box box, boolean throughWalls, RenderColor color) {
        VertexConsumer consumer = throughWalls ? immediate.getBuffer(DEBUG_FILLED_BOX_NO_CULL) : immediate.getBuffer(RenderLayers.debugFilledBox());
        double d = box.minX;
        double e = box.minY;
        double f = box.minZ;
        double g = box.maxX;
        double h = box.maxY;
        double i = box.maxZ;
        this.drawQuad(new Vec3d(g, e, f), new Vec3d(g, h, f), new Vec3d(g, h, i), new Vec3d(g, e, i), consumer, color);
        this.drawQuad(new Vec3d(d, e, f), new Vec3d(d, e, i), new Vec3d(d, h, i), new Vec3d(d, h, f), consumer, color);
        this.drawQuad(new Vec3d(d, e, f), new Vec3d(d, h, f), new Vec3d(g, h, f), new Vec3d(g, e, f), consumer, color);
        this.drawQuad(new Vec3d(d, e, i), new Vec3d(g, e, i), new Vec3d(g, h, i), new Vec3d(d, h, i), consumer, color);
        this.drawQuad(new Vec3d(d, h, f), new Vec3d(d, h, i), new Vec3d(g, h, i), new Vec3d(g, h, f), consumer, color);
        this.drawQuad(new Vec3d(d, e, f), new Vec3d(g, e, f), new Vec3d(g, e, i), new Vec3d(d, e, i), consumer, color);
    }

    public void drawOutline(Box box, boolean throughWalls, RenderColor color) {
        VertexConsumer consumer = throughWalls ? immediate.getBuffer(LINES_TRANSLUCENT_NO_CULL) : immediate.getBuffer(RenderLayers.lines());
        double d = box.minX;
        double e = box.minY;
        double f = box.minZ;
        double g = box.maxX;
        double h = box.maxY;
        double i = box.maxZ;
        this.drawLine(new Vec3d(d, e, f), new Vec3d(g, e, f), 3.0f, consumer, color);
        this.drawLine(new Vec3d(d, e, f), new Vec3d(d, h, f), 3.0f, consumer, color);
        this.drawLine(new Vec3d(d, e, f), new Vec3d(d, e, i), 3.0f, consumer, color);
        this.drawLine(new Vec3d(g, e, f), new Vec3d(g, h, f), 3.0f, consumer, color);
        this.drawLine(new Vec3d(g, h, f), new Vec3d(d, h, f), 3.0f, consumer, color);
        this.drawLine(new Vec3d(d, h, f), new Vec3d(d, h, i), 3.0f, consumer, color);
        this.drawLine(new Vec3d(d, h, i), new Vec3d(d, e, i), 3.0f, consumer, color);
        this.drawLine(new Vec3d(d, e, i), new Vec3d(g, e, i), 3.0f, consumer, color);
        this.drawLine(new Vec3d(g, e, i), new Vec3d(g, e, f), 3.0f, consumer, color);
        this.drawLine(new Vec3d(d, h, i), new Vec3d(g, h, i), 3.0f, consumer, color);
        this.drawLine(new Vec3d(g, e, i), new Vec3d(g, h, i), 3.0f, consumer, color);
        this.drawLine(new Vec3d(g, h, f), new Vec3d(g, h, i), 3.0f, consumer, color);
    }

    public void drawStyled(Box box, RenderStyle style, boolean throughWalls, RenderColor outlineColor, RenderColor filledColor) {
        if (!style.equals(RenderStyle.Outline)) {
            this.drawFilled(box, throughWalls, filledColor);
        }
        if (!style.equals(RenderStyle.Filled)) {
            this.drawOutline(box, throughWalls, outlineColor);
        }
    }

    public void drawText(Vec3d pos, Text text, float scale, boolean throughWalls, RenderColor color) {
        Matrix4f matrices = new Matrix4f();
        Vec3d camPos = this.camera.getCameraPos();
        float textX = (float) (pos.getX() - camPos.getX());
        float textY = (float) (pos.getY() - camPos.getY());
        float textZ = (float) (pos.getZ() - camPos.getZ());
        matrices.translate(textX, textY, textZ);
        matrices.rotate(camera.getRotation());
        matrices.scale(scale, -scale, scale);
        mc.textRenderer.draw(text, -mc.textRenderer.getWidth(text) / 2f, 1.0f, color.argb, true, matrices, immediate, throughWalls ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
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
        VertexConsumer consumer = immediate.getBuffer(LINES_TRANSLUCENT_NO_CULL);
        Vec3d point = this.camera.getCameraPos().add(Vec3d.fromPolar(this.camera.getPitch(), this.camera.getYaw()));
        this.drawLine(point, pos, width, consumer, color);
    }

    public void drawTracer(Vec3d pos, RenderColor color) {
        this.drawTracer(pos, 4.0f, color);
    }
}
