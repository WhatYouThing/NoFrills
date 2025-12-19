package nofrills.misc;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;

public class EtherwarpRaycastContext extends RaycastContext {
    public EtherwarpRaycastContext(Vec3d start, Vec3d end, ShapeType shapeType, FluidHandling fluidHandling, ShapeContext shapeContext) {
        super(start, end, shapeType, fluidHandling, shapeContext);
    }

    public EtherwarpRaycastContext(Vec3d start, Vec3d end, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, Entity entity) {
        this(start, end, shapeType, fluidHandling, ShapeContext.of(entity));
    }

    @Override
    public VoxelShape getBlockShape(BlockState state, BlockView world, BlockPos pos) {
        return switch (state.getBlock()) {
            case DyedCarpetBlock ignored -> VoxelShapes.empty();
            case PlayerSkullBlock ignored -> VoxelShapes.empty();
            case SkullBlock ignored -> VoxelShapes.empty();
            case WallSkullBlock ignored -> VoxelShapes.empty();
            case CarpetBlock ignored -> VoxelShapes.empty();
            case CocoaBlock ignored -> VoxelShapes.empty();
            case FlowerPotBlock ignored -> VoxelShapes.empty();
            case LadderBlock ignored -> VoxelShapes.empty();
            case SignBlock ignored -> VoxelShapes.fullCube();
            case WallSignBlock ignored -> VoxelShapes.fullCube();
            case SnowBlock ignored -> state.get(Properties.LAYERS) < 8 ? VoxelShapes.empty() : VoxelShapes.fullCube();
            default -> state.getCollisionShape(world, pos).isEmpty() ? VoxelShapes.empty() : VoxelShapes.fullCube();
        };
    }

    @Override
    public VoxelShape getFluidShape(FluidState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }
}