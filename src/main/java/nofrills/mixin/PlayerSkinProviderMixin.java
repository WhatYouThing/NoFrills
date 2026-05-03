package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.SkinTextures;
import nofrills.features.misc.StreamerMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(PlayerSkinProvider.class)
public abstract class PlayerSkinProviderMixin {

    @ModifyReturnValue(method = "supplySkinTextures", at = @At("RETURN"))
    private Supplier<SkinTextures> onSupplyTextures(Supplier<SkinTextures> original) {
        if (StreamerMode.isActive()) {
            return DefaultSkinHelper::getSteve;
        }
        return original;
    }

    @ModifyReturnValue(method = "fetchSkinTextures(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
    private CompletableFuture<Optional<SkinTextures>> onFetchTextures(CompletableFuture<Optional<SkinTextures>> original) {
        if (StreamerMode.isActive()) {
            return CompletableFuture.completedFuture(Optional.of(DefaultSkinHelper.getSteve()));
        }
        return original;
    }
}