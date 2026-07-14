package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
import nofrills.features.tweaks.LegacyTextures;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(DataComponentHolder.class)
public interface DataComponentHolderMixin {

    @Shadow
    DataComponentMap getComponents();

    @SuppressWarnings("unchecked")
    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private <T> T onGetComponent(@Nullable T original, @Local(argsOnly = true, name = "type") DataComponentType<?> type) {
        if (LegacyTextures.instance.isActive() && type == DataComponents.PROFILE) {
            Optional<ResolvableProfile> replacement = LegacyTextures.replaceProfileIfNeeded(this.getComponents());
            if (replacement.isPresent()) {
                return (T) replacement.get();
            }
        }
        return original;
    }
}
