package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SnowBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import nofrills.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SnowBlock.class)
public abstract class SnowBlockMixin {
    @Unique
    private static final VoxelShape singleLayerShape = VoxelShapes.cuboid(0.0, -0.00001, 0.0, 1.0, 0.0, 1.0);

    @ModifyReturnValue(method = "getCollisionShape", at = @At("RETURN"))
    private VoxelShape getSnowCollision(VoxelShape original, BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (Config.snowFix) {
            if (state.get(Properties.LAYERS) == 1) {
                return singleLayerShape;
            }
        }
        return original;
    }
}
