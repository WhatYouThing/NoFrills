package nofrills.misc;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LayeringTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class Rendering {

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, RenderColor color) {
        drawBorder(context, x, y, width, height, color.argb);
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int argb) {
        context.fill(x, y, x + width, y + 1, argb);
        context.fill(x, y + height - 1, x + width, y + height, argb);
        context.fill(x, y + 1, x + 1, y + height - 1, argb);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, argb);
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

    public static class Layers {
        public static final RenderLayer BoxFilled = RenderLayer.of("nofrills_box_filled", RenderSetup.builder(Pipelines.filledCull)
                .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .translucent()
                .build());
        public static final RenderLayer BoxFilledNoCull = RenderLayer.of("nofrills_box_filled_no_cull", RenderSetup.builder(Pipelines.filledNoCull)
                .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .translucent()
                .build());
        public static final RenderLayer BoxOutline = RenderLayer.of("nofrills_box_outline", RenderSetup.builder(Pipelines.outlineCull)
                .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .translucent()
                .build());
        public static final RenderLayer BoxOutlineNoCull = RenderLayer.of("nofrills_box_outline_no_cull", RenderSetup.builder(Pipelines.outlineNoCull)
                .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .translucent()
                .build());
        public static final RenderLayer GuiLine = RenderLayer.of("nofrills_gui_line", RenderSetup.builder(Pipelines.lineNoCull)
                .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .translucent()
                .build());

    }
}
