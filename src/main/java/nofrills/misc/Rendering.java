package nofrills.misc;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import static net.minecraft.client.gl.RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET;

public final class Rendering {

    public static final RenderPipeline.Snippet OUTLINE_SNIPPET = RenderPipeline.builder(TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withVertexShader("core/rendertype_outline")
            .withFragmentShader("core/rendertype_outline")
            .withSampler("Sampler0")
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .buildSnippet();

    public static final RenderPipeline OUTLINE = RenderPipelines.register(RenderPipeline.builder(OUTLINE_SNIPPET)
            .withLocation("pipeline/nofrills_outline_cull")
            .withCull(true)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .build());

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, RenderColor color) {
        drawBorder(context, x, y, width, height, color.argb);
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int argb) {
        context.fill(x, y, x + width, y + 1, argb);
        context.fill(x, y + height - 1, x + width, y + height, argb);
        context.fill(x, y + 1, x + 1, y + height - 1, argb);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, argb);
    }

    public static void setGlowing(LivingEntity entity, boolean throughWalls, RenderColor color) {
        GlowParameters parameters = new GlowParameters(throughWalls, color);
        ((GlowRendering) entity).nofrills_mod$setGlowingParameters(parameters);
    }

    public static void setGlowing(Entity entity, boolean throughWalls, RenderColor color) {
        if (entity instanceof LivingEntity living) {
            setGlowing(living, throughWalls, color);
        }
    }

    public interface GlowRendering {
        void nofrills_mod$setGlowingParameters(GlowParameters parameters);

        GlowParameters nofrills_mod$getGlowingParameters();
    }

    public record GlowParameters(boolean throughWalls, RenderColor color) {
    }
}
