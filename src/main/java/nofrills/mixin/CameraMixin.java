package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import nofrills.features.tweaks.InstantSneak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 1))
    private float onLerpEyeHeight(float original, @Local(argsOnly = true) Entity focusedEntity) {
        if (InstantSneak.instance.isActive()) {
            return focusedEntity.getEyeHeight(focusedEntity.getPose());
        }
        return original;
    }
}
