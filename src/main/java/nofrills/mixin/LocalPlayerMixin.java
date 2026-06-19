package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import nofrills.features.general.ItemProtection;
import nofrills.features.hunting.InstantFog;
import nofrills.features.tweaks.RidingCameraFix;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @ModifyReturnValue(method = "getViewYRot", at = @At("RETURN"))
    private float onGetYaw(float original) {
        if (RidingCameraFix.active()) {
            return getYRot();
        }
        return original;
    }

    @Inject(method = "getWaterVision", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"), cancellable = true)
    private void onGetWaterVisibility(CallbackInfoReturnable<Float> cir) {
        if (InstantFog.instance.isActive()) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void onBeforeDropItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (ItemProtection.instance.isActive()) {
            if (Utils.isInDungeons() && DungeonUtil.isDungeonStarted()) {
                return; // items cannot be directly dropped while in an active dungeon due to the class ability
            }
            ItemStack stack = this.getInventory().getSelectedItem();
            if (!ItemProtection.getProtectType(stack).equals(ItemProtection.ProtectType.None)) {
                cir.setReturnValue(false);
            }
        }
    }
}
