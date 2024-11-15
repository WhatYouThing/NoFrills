package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import nofrills.misc.RaycastOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RaycastContext.class)
public class RaycastContextMixin implements RaycastOptions {
    @Unique
    private boolean onlyFull = false;

    @Override
    public void nofrills_mod$setConsiderAllFull(boolean toggle) {
        onlyFull = toggle;
    }

    @ModifyReturnValue(method = "getBlockShape", at = @At("RETURN"))
    private VoxelShape getBlockShape(VoxelShape original, BlockState state, BlockView world, BlockPos pos) {
        if (onlyFull && !original.isEmpty()) {
            return VoxelShapes.fullCube();
        }
        return original;
    }

    @ModifyReturnValue(method = "getFluidShape", at = @At("RETURN"))
    private VoxelShape getFluidShape(VoxelShape original, FluidState state, BlockView world, BlockPos pos) {
        if (onlyFull && !original.isEmpty()) {
            return VoxelShapes.fullCube();
        }
        return original;
    }
}
