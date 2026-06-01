package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import nofrills.misc.Rendering;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderLayer.class)
public class RenderLayerMixin implements Rendering.GlowRendering {

    @Unique
    Rendering.GlowParameters nofrills_mod$glowParameters = null;
    @Shadow
    @Final
    private RenderSetup renderSetup;

    @Override
    public void nofrills_mod$setGlowingParameters(Rendering.GlowParameters parameters) {
        this.nofrills_mod$glowParameters = parameters;
    }

    @Override
    public Rendering.GlowParameters nofrills_mod$getGlowingParameters() {
        return this.nofrills_mod$glowParameters;
    }

    @ModifyExpressionValue(method = "draw", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderSetup;pipeline:Lcom/mojang/blaze3d/pipeline/RenderPipeline;", opcode = Opcodes.GETFIELD))
    private RenderPipeline onGetPipeline(RenderPipeline original) {
        if (this.nofrills_mod$glowParameters != null) {
            return this.nofrills_mod$glowParameters.throughWalls() ? original : Rendering.OUTLINE;
        }
        return original;
    }
}
