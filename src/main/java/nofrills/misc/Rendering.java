package nofrills.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

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
        if (throughWalls) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
        }
        VertexConsumer buffer = throughWalls ? consumer.getBuffer(Layers.BoxFilledNoCull) : consumer.getBuffer(Layers.BoxFilled);
        WorldRenderer.renderFilledBox(matrices, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color.r, color.g, color.b, color.a);
        if (throughWalls) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        }
        matrices.pop();
    }

    /**
     * Draws an outline box for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawOutline(MatrixStack matrices, VertexConsumerProvider.Immediate consumer, Camera camera, Box box, boolean throughWalls, RenderColor color) {
        matrices.push();
        Vec3d camPos = camera.getPos().negate();
        matrices.translate(camPos.x, camPos.y, camPos.z);
        if (throughWalls) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
        }
        VertexConsumer buffer = throughWalls ? consumer.getBuffer(Layers.BoxOutlineNoCull) : consumer.getBuffer(Layers.BoxOutline);
        WorldRenderer.drawBox(matrices, buffer, box, color.r, color.g, color.b, color.a);
        if (throughWalls) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        }
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
        if (throughWalls) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
        }
        mc.textRenderer.draw(text, -mc.textRenderer.getWidth(text) / 2f, 1.0f, color.hex, false, matrices, consumer, throughWalls ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        if (throughWalls) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        }
    }

    /**
     * Draws a simulated beacon beam for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawBeam(MatrixStack matrices, VertexConsumerProvider.Immediate consumer, Camera camera, Vec3d pos, int height, boolean throughWalls, RenderColor color) {
        drawFilled(matrices, consumer, camera, Box.of(pos, 0.35, 0, 0.35).stretch(0, height, 0), throughWalls, color);
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

    public static class Layers {
        public static final RenderLayer.MultiPhase BoxFilled = RenderLayer.of(
                "nofrills_box_filled",
                VertexFormats.POSITION_COLOR,
                VertexFormat.DrawMode.TRIANGLE_STRIP,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.COLOR_PROGRAM)
                        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .build(false));
        public static final RenderLayer.MultiPhase BoxFilledNoCull = RenderLayer.of(
                "nofrills_box_filled_no_cull",
                VertexFormats.POSITION_COLOR,
                VertexFormat.DrawMode.TRIANGLE_STRIP,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.COLOR_PROGRAM)
                        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                        .build(false));
        public static final RenderLayer.MultiPhase BoxOutline = RenderLayer.of(
                "nofrills_box_outline",
                VertexFormats.LINES,
                VertexFormat.DrawMode.LINES,
                1536,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.LINES_PROGRAM)
                        .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(3.0)))
                        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .target(RenderPhase.ITEM_ENTITY_TARGET)
                        .writeMaskState(RenderPhase.ALL_MASK)
                        .cull(RenderPhase.DISABLE_CULLING)
                        .build(false)
        );
        public static final RenderLayer.MultiPhase BoxOutlineNoCull = RenderLayer.of(
                "nofrills_box_outline_no_cull",
                VertexFormats.LINES,
                VertexFormat.DrawMode.LINES,
                1536,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.LINES_PROGRAM)
                        .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(3.0)))
                        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .target(RenderPhase.ITEM_ENTITY_TARGET)
                        .writeMaskState(RenderPhase.ALL_MASK)
                        .cull(RenderPhase.DISABLE_CULLING)
                        .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                        .build(false)
        );
    }
}
