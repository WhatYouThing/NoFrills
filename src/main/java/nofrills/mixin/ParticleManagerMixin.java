package nofrills.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import nofrills.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
    private void onBreakParticle(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (Config.noBreakParticles) {
            ci.cancel();
        }
    }

    @Inject(method = "addBlockBreakingParticles", at = @At("HEAD"), cancellable = true)
    private void onBreakingParticle(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (Config.noBreakParticles) {
            ci.cancel();
        }
    }
}
