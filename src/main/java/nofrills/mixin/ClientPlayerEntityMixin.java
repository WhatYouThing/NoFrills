package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import nofrills.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    public abstract boolean isSneaking();

    @ModifyReturnValue(method = "getYaw", at = @At("RETURN"))
    private float onGetYaw(float original) {
        if (Config.ridingCamFix) {
            return getYaw();
        }
        return original;
    }

    @ModifyReturnValue(method = "shouldSlowDown", at = @At("RETURN"))
    private boolean shouldSlowDown(boolean original) {
        if (Config.sneakFix) {
            return (isSneaking() && !getAbilities().flying) || isCrawling();
        }
        return original;
    }

    @ModifyReturnValue(method = "isInSneakingPose", at = @At("RETURN"))
    private boolean isInSneakPose(boolean original) {
        if (Config.sneakFix) {
            return !getAbilities().flying && !isSwimming() && !hasVehicle() && !isSleeping() && isSneaking();
        }
        return original;
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        if (Config.sneakFix) {
            if (getPose().equals(EntityPose.CROUCHING) && !isSneaking()) {
                setPose(EntityPose.STANDING);
            }
            if (getPose().equals(EntityPose.STANDING) && isSneaking()) {
                setPose(EntityPose.CROUCHING);
            }
        }
    }
}
