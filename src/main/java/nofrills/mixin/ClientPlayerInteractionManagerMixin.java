package nofrills.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import nofrills.events.AttackEntityEvent;
import nofrills.features.tweaks.StonkFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nofrills.Main.eventBus;

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

    @Inject(method = "attackEntity", at = @At("TAIL"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        eventBus.post(new AttackEntityEvent(target));
    }
}
