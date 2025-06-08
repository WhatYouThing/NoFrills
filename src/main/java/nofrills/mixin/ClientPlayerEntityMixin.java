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

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
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
}
