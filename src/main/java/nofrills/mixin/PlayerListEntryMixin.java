package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import nofrills.features.misc.StreamerMode;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow
    @Final
    private GameProfile profile;

    @Shadow
    private @Nullable Text displayName;

    @ModifyReturnValue(method = "getSkinTextures", at = @At("RETURN"))
    private SkinTextures getSkinTextures(SkinTextures original) {
        if (StreamerMode.isActive() && (this.profile.id().version() == 4 || this.displayName != null)) {
            return StreamerMode.skinSupplier.get();
        }
        return original;
    }
}