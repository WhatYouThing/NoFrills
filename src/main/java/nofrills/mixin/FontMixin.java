package nofrills.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import nofrills.features.tweaks.LegacyTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Font.class)
public class FontMixin {

    @WrapOperation(method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/FormattedCharSequence;accept(Lnet/minecraft/util/FormattedCharSink;)Z"))
    private boolean onAcceptSequence(FormattedCharSequence instance, FormattedCharSink formattedCharSink, Operation<Boolean> original) {
        if (LegacyTextures.instance.isActive() && LegacyTextures.masterStars.value()) {
            return original.call(LegacyTextures.replaceStarsIfNeeded(instance).orElse(instance), formattedCharSink);
        }
        return original.call(instance, formattedCharSink);
    }
}
