package nofrills.mixin;

import net.minecraft.entity.decoration.ArmorStandEntity;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.Config;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin {
    @Inject(method = "tickCramming", at = @At("HEAD"), cancellable = true)
    private void onTickCramming(CallbackInfo ci) {
        if (Utils.isFixEnabled(Config.armorStandFix())) {
            ci.cancel();
        }
    }
}
