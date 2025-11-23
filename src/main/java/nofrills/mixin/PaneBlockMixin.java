package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import nofrills.features.mining.GemstoneDesyncFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PaneBlock.class)
public abstract class PaneBlockMixin {

    @ModifyReturnValue(method = "getStateForNeighborUpdate", at = @At("RETURN"))
    private BlockState onGetUpdateState(BlockState original) {
        if (GemstoneDesyncFix.active() && GemstoneDesyncFix.isDefaultPane(original)) {
            return GemstoneDesyncFix.asFullPane(original);
        }
        return original;
    }
}
