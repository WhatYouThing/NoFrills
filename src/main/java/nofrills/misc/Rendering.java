package nofrills.misc;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.OptionalDouble;

import static nofrills.Main.mc;

public final class Rendering {
    /**
     * Draws a filled box for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawFilled(MatrixStack matrices, VertexConsumerProvider.Immediate consumer, Camera camera, Box box, boolean throughWalls, RenderColor color) {
        matrices.push();
        Vec3d camPos = camera.getPos().negate();
        matrices.translate(camPos.x, camPos.y, camPos.z);
        VertexConsumer buffer = throughWalls ? consumer.getBuffer(Layers.BoxFilledNoCull) : consumer.getBuffer(Layers.BoxFilled);
        VertexRendering.drawFilledBox(matrices, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color.r, color.g, color.b, color.a);
        matrices.pop();
    }

    /**
     * Draws an outline box for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawOutline(MatrixStack matrices, VertexConsumerProvider.Immediate consumer, Camera camera, Box box, boolean throughWalls, RenderColor color) {
        matrices.push();
        Vec3d camPos = camera.getPos().negate();
        matrices.translate(camPos.x, camPos.y, camPos.z);
        VertexConsumer buffer = throughWalls ? consumer.getBuffer(Layers.BoxOutlineNoCull) : consumer.getBuffer(Layers.BoxOutline);
        VertexRendering.drawBox(matrices, buffer, box, color.r, color.g, color.b, color.a);
        matrices.pop();
    }

    /**
     * Draws text within the world for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawText(VertexConsumerProvider.Immediate consumer, Camera camera, Vec3d pos, Text text, float scale, boolean throughWalls, RenderColor color) {
        Matrix4f matrices = new Matrix4f();
        Vec3d camPos = camera.getPos();
        float textX = (float) (pos.getX() - camPos.getX());
        float textY = (float) (pos.getY() - camPos.getY());
        float textZ = (float) (pos.getZ() - camPos.getZ());
        matrices.translate(textX, textY, textZ);
        matrices.rotate(camera.getRotation());
        matrices.scale(scale, -scale, scale);
        mc.textRenderer.draw(text, -mc.textRenderer.getWidth(text) / 2f, 1.0f, color.argb, true, matrices, consumer, throughWalls ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }

    /**
     * Draws a simulated beacon beam for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawBeam(MatrixStack matrices, VertexConsumerProvider.Immediate consumer, Camera camera, Vec3d pos, int height, boolean throughWalls, RenderColor color) {
        drawFilled(matrices, consumer, camera, Box.of(pos, 0.5, 0, 0.5).stretch(0, height, 0), throughWalls, color);
    }

    /**
     * Draws a tracer going from the center of the screen to the provided coordinate. Automatically performs the required matrix stack translation.
     */
    public static void drawTracer(MatrixStack matrices, VertexConsumerProvider.Immediate consumer, Camera camera, Vec3d pos, RenderColor color) {
        Vec3d camPos = camera.getPos();
        matrices.push();
        matrices.translate(-camPos.getX(), -camPos.getY(), -camPos.getZ());
        MatrixStack.Entry entry = matrices.peek();
        VertexConsumer buffer = consumer.getBuffer(Layers.GuiLine);
        Vec3d point = camPos.add(Vec3d.fromPolar(camera.getPitch(), camera.getYaw())); // taken from Skyblocker's RenderHelper, my brain cannot handle OpenGL
        Vector3f normal = pos.toVector3f().sub((float) point.getX(), (float) point.getY(), (float) point.getZ()).normalize(new Vector3f(1.0f, 1.0f, 1.0f));
        buffer.vertex(entry, (float) point.getX(), (float) point.getY(), (float) point.getZ()).color(color.r, color.g, color.b, color.a).normal(entry, normal);
        buffer.vertex(entry, (float) pos.getX(), (float) pos.getY(), (float) pos.getZ()).color(color.r, color.g, color.b, color.a).normal(entry, normal);
        matrices.pop();
    }

    public static class Entities {
        /**
         * Enable/disable filled box rendering for an entity.
         */
        public static void drawFilled(Entity entity, boolean shouldRender, RenderColor color) {
            ((EntityRendering) entity).nofrills_mod$setRenderBoxFilled(shouldRender, color);
        }

        /**
         * Check if a filled box is currently being rendered for the entity.
         */
        public static boolean isDrawingFilled(Entity entity) {
            return ((EntityRendering) entity).nofrills_mod$getRenderingFilled();
        }

        /**
         * Enable/disable outline box rendering for an entity.
         */
        public static void drawOutline(Entity entity, boolean shouldRender, RenderColor color) {
            ((EntityRendering) entity).nofrills_mod$setRenderBoxOutline(shouldRender, color);
        }

        /**
         * Check if an outline box is currently being rendered for the entity.
         */
        public static boolean isDrawingOutline(Entity entity) {
            return ((EntityRendering) entity).nofrills_mod$getRenderingOutline();
        }

        /**
         * Enable/disable glowing for a specific entity.
         */
        public static void drawGlow(Entity ent, boolean shouldGlow, RenderColor color) {
            ((EntityRendering) ent).nofrills_mod$setGlowingColored(shouldGlow, color);
        }

        /**
         * Checks if an entity is drawing the glow effect. Does not account for vanilla/server applied glows.
         */
        public static boolean isDrawingGlow(Entity ent) {
            return ((EntityRendering) ent).nofrills_mod$getGlowing();
        }
    }

    public static class Pipelines {
        public static final RenderPipeline.Snippet filledSnippet = RenderPipelines.POSITION_COLOR_SNIPPET;
        public static final RenderPipeline.Snippet outlineSnippet = RenderPipelines.RENDERTYPE_LINES_SNIPPET;

        public static final RenderPipeline filledNoCull = RenderPipelines.register(RenderPipeline.builder(filledSnippet)
                .withLocation(Identifier.of("nofrills", "pipeline/nofrills_filled_no_cull"))
                .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build());
        public static final RenderPipeline filledCull = RenderPipelines.register(RenderPipeline.builder(filledSnippet)
                .withLocation(Identifier.of("nofrills", "pipeline/nofrills_filled_cull"))
                .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
                .build());
        public static final RenderPipeline outlineNoCull = RenderPipelines.register(RenderPipeline.builder(outlineSnippet)
                .withLocation(Identifier.of("nofrills", "pipeline/nofrills_outline_no_cull"))
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build());
        public static final RenderPipeline outlineCull = RenderPipelines.register(RenderPipeline.builder(outlineSnippet)
                .withLocation(Identifier.of("nofrills", "pipeline/nofrills_outline_cull"))
                .build());
        public static final RenderPipeline lineNoCull = RenderPipelines.register(RenderPipeline.builder(outlineSnippet)
                .withLocation(Identifier.of("nofrills", "pipeline/nofrills_line_no_cull"))
                .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINE_STRIP)
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build());
    }

    public static class Parameters {
        public static final RenderLayer.MultiPhaseParameters.Builder filled = RenderLayer.MultiPhaseParameters.builder()
                .layering(RenderLayer.VIEW_OFFSET_Z_LAYERING);
        public static final RenderLayer.MultiPhaseParameters.Builder lines = RenderLayer.MultiPhaseParameters.builder()
                .layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
                .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(3.0)));
    }

    public static class Layers {
        public static final RenderLayer.MultiPhase BoxFilled = RenderLayer.of(
                "nofrills_box_filled",
                RenderLayer.DEFAULT_BUFFER_SIZE,
                false,
                true,
                Pipelines.filledCull,
                Parameters.filled.build(false)
        );
        public static final RenderLayer.MultiPhase BoxFilledNoCull = RenderLayer.of(
                "nofrills_box_filled_no_cull",
                RenderLayer.DEFAULT_BUFFER_SIZE,
                false,
                true,
                Pipelines.filledNoCull,
                Parameters.filled.build(false)
        );
        public static final RenderLayer.MultiPhase BoxOutline = RenderLayer.of(
                "nofrills_box_outline",
                RenderLayer.DEFAULT_BUFFER_SIZE,
                false,
                false,
                Pipelines.outlineCull,
                Parameters.lines.build(false)
        );
        public static final RenderLayer.MultiPhase BoxOutlineNoCull = RenderLayer.of(
                "nofrills_box_outline_no_cull",
                RenderLayer.DEFAULT_BUFFER_SIZE,
                false,
                false,
                Pipelines.outlineNoCull,
                Parameters.lines.build(false)
        );
        public static final RenderLayer.MultiPhase GuiLine = RenderLayer.of(
                "nofrills_gui_line",
                RenderLayer.DEFAULT_BUFFER_SIZE,
                false,
                false,
                Pipelines.lineNoCull,
                Parameters.lines.build(false)
        );
    }
}
