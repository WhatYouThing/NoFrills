package nofrills.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderLayer.MultiPhaseParameters.Builder.class)
public interface PhaseParameterBuilderAccessor {
    @Invoker("layering")
    RenderLayer.MultiPhaseParameters.Builder setLayering(RenderPhase.Layering layering);

    @Invoker("lineWidth")
    RenderLayer.MultiPhaseParameters.Builder setLineWidth(RenderPhase.LineWidth lineWidth);

    @Invoker("build")
    RenderLayer.MultiPhaseParameters buildParameters(boolean affectsOutline);
}