package nofrills.events;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;

public class WorldRenderEvent {
    public VertexConsumerProvider.Immediate consumer;
    public RenderTickCounter tickCounter;
    public Camera camera;
    public MatrixStack matrices;

    public WorldRenderEvent(VertexConsumerProvider.Immediate consumer, RenderTickCounter tickCounter, Camera camera, MatrixStack matrices) {
        this.consumer = consumer;
        this.tickCounter = tickCounter;
        this.camera = camera;
        this.matrices = matrices;
    }

    public void drawFilled(Box box, boolean throughWalls, RenderColor color) {
        Rendering.drawFilled(matrices, consumer, camera, box, throughWalls, color);
    }

    public void drawOutline(Box box, boolean throughWalls, RenderColor color) {
        Rendering.drawOutline(matrices, consumer, camera, box, throughWalls, color);
    }

    public void drawText(Vec3d pos, Text text, float scale, boolean throughWalls, RenderColor color) {
        Rendering.drawText(consumer, camera, pos, text, scale, throughWalls, color);
    }
}
