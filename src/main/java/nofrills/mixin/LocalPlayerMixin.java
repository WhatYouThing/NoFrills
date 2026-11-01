package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import nofrills.features.general.AutoSprint;
import nofrills.features.hunting.InstantFog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nofrills.Main.mc;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "getWaterVision", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"), cancellable = true)
    private void onGetWaterVisibility(CallbackInfoReturnable<Float> cir) {
        if (InstantFog.instance.isActive()) {
            cir.setReturnValue(1.0f);
        }
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Input;sprint()Z"))
    private boolean onIsSprinting(boolean original) {
        if (AutoSprint.instance.isActive() && mc.getConnection() != null && mc.getConnection().hasClientLoaded()) {
            if (AutoSprint.waterCheck.value() && this.isInWater()) {
                return original;
            }
            AutoSprint.setSprinting(true);
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;shouldStopSwimSprinting()Z"))
    private boolean onShouldStopSwimSprinting(boolean original) {
        if (AutoSprint.instance.isActive() && AutoSprint.waterCheck.value() && AutoSprint.wasSprinting()) {
            AutoSprint.setSprinting(false);
            return true;
        }
        return original;
    }
}
