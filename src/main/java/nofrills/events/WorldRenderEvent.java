package nofrills.events;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class WorldRenderEvent {
    private static final WorldRenderEvent INSTANCE = new WorldRenderEvent();

    public VertexConsumerProvider.Immediate consumer;
    public RenderTickCounter tickCounter;
    public Camera camera;
    public MatrixStack matrices;

    public static WorldRenderEvent get(VertexConsumerProvider.Immediate consumer, RenderTickCounter tickCounter, Camera camera, MatrixStack matrices) {
        INSTANCE.consumer = consumer;
        INSTANCE.tickCounter = tickCounter;
        INSTANCE.camera = camera;
        INSTANCE.matrices = matrices;
        return INSTANCE;
    }
}
