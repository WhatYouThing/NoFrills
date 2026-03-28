package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.monster.Creeper;
import nofrills.features.mining.GhostVision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Creeper.class)
public class CreeperMixin {

    @ModifyReturnValue(method = "isPowered", at = @At("RETURN"))
    private boolean isCharged(boolean original) {
        if (GhostVision.isGhost((Creeper) (Object) this)) {
            return false;
        }
        return original;
    }
}
