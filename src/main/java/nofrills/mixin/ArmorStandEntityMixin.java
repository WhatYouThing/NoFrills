package nofrills.mixin;

import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Config;
import nofrills.features.KuudraFeatures;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin {
    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (Config.kuudraPileFix && KuudraFeatures.isNearPile()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "tickCramming", at = @At("HEAD"), cancellable = true)
    private void onTickCramming(CallbackInfo ci) {
        if (Utils.isFixEnabled(Config.armorStandFix)) {
            ci.cancel();
        }
    }
}
