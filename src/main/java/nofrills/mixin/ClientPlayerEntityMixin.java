package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import nofrills.config.Config;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.mc;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow
    private boolean inSneakingPose;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public abstract boolean isInSneakingPose();

    @ModifyReturnValue(method = "getYaw", at = @At("RETURN"))
    private float onGetYaw(float original) {
        if (Utils.isFixEnabled(Config.ridingCamFix)) {
            return getYaw();
        }
        return original;
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick()V"))
    private void onTickMovement(CallbackInfo ci) {
        if (Utils.isFixEnabled(Config.sneakFix)) {
            this.inSneakingPose = !getAbilities().flying && !isSwimming() && !hasVehicle() && !isSleeping() && mc.options.sneakKey.isPressed();
        }
    }
}
