package nofrills.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import nofrills.features.misc.StreamerMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(TextVisitFactory.class)
public class TextVisitFactoryMixin {

    @WrapOperation(method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"))
    private static boolean onVisitFormatted(String text, int startIndex, Style startingStyle, Style resetStyle, CharacterVisitor visitor, Operation<Boolean> original) {
        if (StreamerMode.isActive()) {
            Optional<String> replacement = StreamerMode.replaceIfNeeded(text);
            if (replacement.isPresent()) {
                return original.call(replacement.get(), startIndex, startingStyle, resetStyle, visitor);
            }
        }
        return original.call(text, startIndex, startingStyle, resetStyle, visitor);
    }
}
