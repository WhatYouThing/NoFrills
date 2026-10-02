package nofrills.compat;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4fc;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/** Immediate world geometry backed by Minecraft 26.3's staged GPU buffers. */
public final class WorldBufferSource implements AutoCloseable {
    private StagedVertexBuffer buffer;
    private final List<Draw> draws = new ArrayList<>();
    private Draw current;
    private record Draw(RenderType type, StagedVertexBuffer.Draw vertices) {}

    public VertexConsumer getBuffer(RenderType type) {
        if (this.buffer == null) this.buffer = new StagedVertexBuffer(() -> "NoFrills world geometry", 2048);
        if (this.current == null || this.current.type() != type) {
            var vertices = type.sortOnUpload()
                ? this.buffer.appendDraw(type.format(), type.primitiveTopology(), VertexSorting.DISTANCE_TO_ORIGIN)
                : this.buffer.appendDraw(type.format(), type.primitiveTopology());
            this.current = new Draw(type, vertices);
            this.draws.add(this.current);
        }
        return this.buffer.getVertexBuilder(this.current.vertices());
    }

    public void drawText(Font.PreparedText text, Matrix4fc pose, Font.DisplayMode displayMode, int light) {
        text.visit(new Font.GlyphVisitor() {
            @Override
            public void acceptRenderable(TextRenderable renderable) {
                renderable.render(pose, getBuffer(renderable.renderType(displayMode)), light, false);
            }
        });
    }

    public void endBatch(RenderTarget target) {
        if (this.buffer == null || this.draws.isEmpty()) return;
        try {
            // upload() finishes the active vertex builder. endDraw() clears the staged
            // draws and must only run after execution, through endFrame() below.
            this.buffer.upload();
            for (var draw : this.draws) this.buffer.requestIndexCount(draw.vertices());
            var encoder = RenderSystem.getDevice().createCommandEncoder();
            try (var pass = encoder.createRenderPass(() -> "NoFrills world overlays", target.getColorTextureView(),
                    Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
                for (var draw : this.draws) {
                    var info = this.buffer.getExecuteInfo(draw.vertices());
                    if (info != null) draw.type().prepare().drawFromBuffer(info, pass);
                }
            }
            encoder.submit();
        } finally {
            this.draws.clear();
            this.current = null;
            this.buffer.endFrame();
        }
    }

    @Override
    public void close() {
        if (this.buffer != null) this.buffer.close();
        this.buffer = null;
        this.draws.clear();
        this.current = null;
    }
}
