package nofrills.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import nofrills.config.Config;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Shadow
    private int blockBreakingCooldown;

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void onUpdateBreakProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (Utils.isFixEnabled(Config.stonkFix)) {
            this.blockBreakingCooldown = Math.min(this.blockBreakingCooldown, 1);
        }
    }
}
