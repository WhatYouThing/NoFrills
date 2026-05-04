package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import nofrills.features.misc.StreamerMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static nofrills.Main.mc;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow
    @Final
    private GameProfile profile;

    @ModifyReturnValue(method = "getSkinTextures", at = @At("RETURN"))
    private SkinTextures getSkinTextures(SkinTextures original) {
        if (StreamerMode.isActive() && mc.player != null && !this.profile.id().equals(mc.player.getUuid())) {
            return mc.player.getSkin();
        }
        return original;
    }
}