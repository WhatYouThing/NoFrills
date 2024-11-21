package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import nofrills.config.Config;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DefaultSkinHelper.class)
public class DefaultSkinHelperMixin {
    @Shadow
    @Final
    private static SkinTextures[] SKINS;

    @ModifyReturnValue(method = "getSkinTextures(Ljava/util/UUID;)Lnet/minecraft/client/util/SkinTextures;", at = @At("RETURN"))
    private static SkinTextures getSkinTextures(SkinTextures original) {
        if (Config.oldSkins) {
            if (original.model().equals(SkinTextures.Model.WIDE)) {
                return SKINS[15]; // steve
            }
            if (original.model().equals(SkinTextures.Model.SLIM)) {
                return SKINS[0]; // alex
            }
        }
        return original;
    }
}
