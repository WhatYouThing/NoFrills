package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;
import nofrills.features.misc.StreamerMode;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin {

    @Shadow
    @Final
    private GameProfile profile;

    @Shadow
    private @Nullable Component tabListDisplayName;

    @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
    private PlayerSkin getSkinTextures(PlayerSkin original) {
        if (StreamerMode.isActive() && (this.profile.id().version() == 4 || this.tabListDisplayName != null)) {
            return StreamerMode.skinSupplier.get();
        }
        return original;
    }
}