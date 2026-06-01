package nofrills.mixin;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import nofrills.misc.Rendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements Rendering.GlowRendering {

    @Unique
    Rendering.GlowParameters nofrills_mod$glowParameters = null;

    @Override
    public void nofrills_mod$setGlowingParameters(Rendering.GlowParameters parameters) {
        this.nofrills_mod$glowParameters = parameters;
    }

    @Override
    public Rendering.GlowParameters nofrills_mod$getGlowingParameters() {
        return this.nofrills_mod$glowParameters;
    }
}
