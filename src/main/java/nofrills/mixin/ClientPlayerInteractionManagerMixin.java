package nofrills.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import nofrills.features.fixes.StonkFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Shadow
    private BlockPos currentBreakingPos;

    @Inject(method = "breakBlock", at = @At("TAIL"))
    private void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (StonkFix.active()) { // fixes a vanilla bug where a long break cooldown is applied if you insta mine a block you are inside of
            this.currentBreakingPos = new BlockPos(-1, -1, -1);
        }
    }
}
