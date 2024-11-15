package nofrills.events;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

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
}
