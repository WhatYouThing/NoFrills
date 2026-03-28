package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import nofrills.features.tweaks.EyeHeightFix;
import nofrills.features.tweaks.InstantSneak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow
    private Entity focusedEntity;

    @Unique
    public float nofrills_mod$getEyeHeight(boolean revertSneak) {
        float eyeHeight = this.focusedEntity.getStandingEyeHeight();
        if (revertSneak && eyeHeight == 1.27f) {
            return 1.54f;
        }
        return eyeHeight;
    }

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 1))
    private float onLerpEyeHeight(float original, @Local(argsOnly = true) Entity focusedEntity) {
        if (InstantSneak.instance.isActive()) {
            return this.nofrills_mod$getEyeHeight(EyeHeightFix.active());
        }
        return original;
    }

    @ModifyExpressionValue(method = "updateEyeHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getStandingEyeHeight()F"))
    private float onUpdateEyeHeight(float original) {
        if (EyeHeightFix.active()) {
            return this.nofrills_mod$getEyeHeight(true);
        }
        return original;
    }
}
