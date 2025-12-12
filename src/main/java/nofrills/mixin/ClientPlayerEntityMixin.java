package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import nofrills.features.hunting.InstantFog;
import nofrills.features.tweaks.HitboxFix;
import nofrills.features.tweaks.RidingCameraFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @ModifyReturnValue(method = "getYaw", at = @At("RETURN"))
    private float onGetYaw(float original) {
        if (RidingCameraFix.active()) {
            return getYaw();
        }
        return original;
    }

    @Inject(method = "getUnderwaterVisibility", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"), cancellable = true)
    private void onGetWaterVisibility(CallbackInfoReturnable<Float> cir) {
        if (InstantFog.instance.isActive()) {
            cir.setReturnValue(1.0f);
        }
    }

    @ModifyExpressionValue(method = "wouldCollideAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getBoundingBox()Lnet/minecraft/util/math/Box;"))
    private Box onGetBoundingBox(Box original) {
        if (HitboxFix.active()) {
            return PlayerEntity.STANDING_DIMENSIONS.getBoxAt(this.getEntityPos());
        }
        return original;
    }
}
