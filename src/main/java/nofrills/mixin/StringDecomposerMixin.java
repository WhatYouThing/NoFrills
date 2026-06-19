package nofrills.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import nofrills.features.misc.StreamerMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(StringDecomposer.class)
public class StringDecomposerMixin {

    @WrapOperation(method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringDecomposer;iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z"))
    private static boolean onVisitFormatted(String string, int offset, Style currentStyle, Style resetStyle, FormattedCharSink output, Operation<Boolean> original) {
        if (StreamerMode.isActive()) {
            Optional<String> replacement = StreamerMode.replaceIfNeeded(string);
            if (replacement.isPresent()) {
                return original.call(replacement.get(), offset, currentStyle, resetStyle, output);
            }
        }
        return original.call(string, offset, currentStyle, resetStyle, output);
    }
}
