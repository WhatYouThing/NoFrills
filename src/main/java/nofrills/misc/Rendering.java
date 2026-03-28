package nofrills.misc;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static nofrills.Main.mc;

public final class Rendering {
    /**
     * Draws a filled box for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawFilled(AABB box, boolean throughWalls, RenderColor color) {
        Gizmos.cuboid(box, GizmoStyle.fill(color.argb));
    }

    /**
     * Draws an outline box for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawOutline(AABB box, boolean throughWalls, RenderColor color) {
        Gizmos.cuboid(box, GizmoStyle.stroke(color.argb));
    }

    /**
     * Draws text within the world for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawText(MultiBufferSource.BufferSource consumer, CameraRenderState cameraState, Vec3 pos, Component text, float scale, boolean throughWalls, RenderColor color) {
        Matrix4f matrices = new Matrix4f();
        Vec3 camPos = cameraState.pos;
        float textX = (float) (pos.x() - camPos.x());
        float textY = (float) (pos.y() - camPos.y());
        float textZ = (float) (pos.z() - camPos.z());
        matrices.translate(textX, textY, textZ);
        matrices.mul(cameraState.viewRotationMatrix);
        matrices.scale(scale, -scale, scale);
        mc.font.drawInBatch(text, -mc.font.width(text) / 2f, 1.0f, color.argb, true, matrices, consumer, throughWalls ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, LightCoordsUtil.FULL_BRIGHT);
    }

    /**
     * Draws a simulated beacon beam for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawBeam(Vec3 pos, int height, boolean throughWalls, RenderColor color) {
        drawFilled(AABB.ofSize(pos, 0.5, 0, 0.5).expandTowards(0, height, 0), throughWalls, color);
    }

    /**
     * Draws a tracer going from the center of the screen to the provided coordinate. Automatically performs the required matrix stack translation.
     */
    public static void drawTracer(PoseStack matrices, MultiBufferSource.BufferSource consumer, CameraRenderState cameraState, Vec3 pos, RenderColor color) {
        Vec3 camPos = cameraState.pos;
        matrices.pushPose();
        matrices.translate(-camPos.x(), -camPos.y(), -camPos.z());
        PoseStack.Pose entry = matrices.last();
        VertexConsumer buffer = consumer.getBuffer(Layers.GuiLine);
        Vec3 point = camPos.add(Vec3.directionFromRotation(cameraState.xRot, cameraState.yRot)); // taken from Skyblocker's RenderHelper, my brain cannot handle OpenGL
        Vector3f normal = pos.toVector3f().sub((float) point.x(), (float) point.y(), (float) point.z()).normalize(new Vector3f(1.0f, 1.0f, 1.0f));
        buffer.addVertex(entry, (float) point.x(), (float) point.y(), (float) point.z()).setColor(color.r, color.g, color.b, color.a).setNormal(entry, normal);
        buffer.addVertex(entry, (float) pos.x(), (float) pos.y(), (float) pos.z()).setColor(color.r, color.g, color.b, color.a).setNormal(entry, normal);
        matrices.popPose();
    }

    public static void drawBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, RenderColor color) {
        drawBorder(context, x, y, width, height, color.argb);
    }

    public static void drawBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int argb) {
        context.fill(x, y, x + width, y + 1, argb);
        context.fill(x, y + height - 1, x + width, y + height, argb);
        context.fill(x, y + 1, x + 1, y + height - 1, argb);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, argb);
    }

    public static class Pipelines {
        public static final RenderPipeline.Snippet filledSnippet = RenderPipelines.DEBUG_FILLED_SNIPPET;
        public static final RenderPipeline.Snippet outlineSnippet = RenderPipelines.LINES_SNIPPET;

        public static final RenderPipeline filledNoCull = RenderPipelines.register(RenderPipeline.builder(filledSnippet)
                .withLocation(Identifier.fromNamespaceAndPath("nofrills", "pipeline/nofrills_filled_no_cull"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
                .build());
        public static final RenderPipeline filledCull = RenderPipelines.register(RenderPipeline.builder(filledSnippet)
                .withLocation(Identifier.fromNamespaceAndPath("nofrills", "pipeline/nofrills_filled_cull"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                .build());
        public static final RenderPipeline outlineNoCull = RenderPipelines.register(RenderPipeline.builder(outlineSnippet)
                .withLocation(Identifier.fromNamespaceAndPath("nofrills", "pipeline/nofrills_outline_no_cull"))
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
                .build());
        public static final RenderPipeline outlineCull = RenderPipelines.register(RenderPipeline.builder(outlineSnippet)
                .withLocation(Identifier.fromNamespaceAndPath("nofrills", "pipeline/nofrills_outline_cull"))
                .build());
        public static final RenderPipeline lineNoCull = RenderPipelines.register(RenderPipeline.builder(outlineSnippet)
                .withLocation(Identifier.fromNamespaceAndPath("nofrills", "pipeline/nofrills_line_no_cull"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP)
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
                .build());
    }

    public static class Layers {
        public static final RenderType BoxFilled = RenderType.create(
                "nofrills_box_filled",
                RenderSetup.builder(Pipelines.filledCull)
                        .sortOnUpload()
                        .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .createRenderSetup()
        );
        public static final RenderType BoxFilledNoCull = RenderType.create(
                "nofrills_box_filled_no_cull",
                RenderSetup.builder(Pipelines.filledNoCull)
                        .sortOnUpload()
                        .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .createRenderSetup()
        );
        public static final RenderType BoxOutline = RenderType.create(
                "nofrills_box_outline",
                RenderSetup.builder(Pipelines.outlineCull)
                        .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .createRenderSetup()
        );
        public static final RenderType BoxOutlineNoCull = RenderType.create(
                "nofrills_box_outline_no_cull",
                RenderSetup.builder(Pipelines.outlineNoCull)
                        .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .createRenderSetup()
        );
        public static final RenderType GuiLine = RenderType.create(
                "nofrills_gui_line",
                RenderSetup.builder(Pipelines.lineNoCull)
                        .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .createRenderSetup()
        );
    }
}
