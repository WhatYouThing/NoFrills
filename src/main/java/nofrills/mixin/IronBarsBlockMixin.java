package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import nofrills.features.mining.GemstoneDesyncFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IronBarsBlock.class)
public abstract class IronBarsBlockMixin {

    @ModifyReturnValue(method = "updateShape", at = @At("RETURN"))
    private BlockState onGetUpdateState(BlockState original) {
        if (GemstoneDesyncFix.active() && GemstoneDesyncFix.isDefaultPane(original)) {
            return GemstoneDesyncFix.asFullPane(original);
        }
        return original;
    }
}
