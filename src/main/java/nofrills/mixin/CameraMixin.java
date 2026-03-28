package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import nofrills.features.tweaks.EyeHeightFix;
import nofrills.features.tweaks.InstantSneak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow
    private Entity entity;

    @Unique
    public float nofrills_mod$getEyeHeight(boolean revertSneak) {
        float eyeHeight = this.entity.getEyeHeight();
        if (revertSneak && eyeHeight == 1.27f) {
            return 1.54f;
        }
        return eyeHeight;
    }

    @ModifyExpressionValue(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 1))
    private float onLerpEyeHeight(float original) {
        if (InstantSneak.instance.isActive()) {
            return this.nofrills_mod$getEyeHeight(EyeHeightFix.active());
        }
        return original;
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeHeight()F"))
    private float onUpdateEyeHeight(float original) {
        if (EyeHeightFix.active()) {
            return this.nofrills_mod$getEyeHeight(true);
        }
        return original;
    }
}
