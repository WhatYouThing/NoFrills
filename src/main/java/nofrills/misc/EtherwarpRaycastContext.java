package nofrills.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EtherwarpRaycastContext extends ClipContext {
    public EtherwarpRaycastContext(Vec3 start, Vec3 end, Block shapeType, Fluid fluidHandling, CollisionContext shapeContext) {
        super(start, end, shapeType, fluidHandling, shapeContext);
    }

    public EtherwarpRaycastContext(Vec3 start, Vec3 end, ClipContext.Block shapeType, ClipContext.Fluid fluidHandling, Entity entity) {
        this(start, end, shapeType, fluidHandling, CollisionContext.of(entity));
    }

    @Override
    public VoxelShape getBlockShape(BlockState state, BlockGetter world, BlockPos pos) {
        return switch (state.getBlock()) {
            case WoolCarpetBlock ignored -> Shapes.empty();
            case PlayerHeadBlock ignored -> Shapes.empty();
            case SkullBlock ignored -> Shapes.empty();
            case WallSkullBlock ignored -> Shapes.empty();
            case CarpetBlock ignored -> Shapes.empty();
            case CocoaBlock ignored -> Shapes.empty();
            case FlowerPotBlock ignored -> Shapes.empty();
            case LadderBlock ignored -> Shapes.empty();
            case StandingSignBlock ignored -> Shapes.block();
            case WallSignBlock ignored -> Shapes.block();
            case SnowLayerBlock ignored ->
                    state.getValue(BlockStateProperties.LAYERS) < 8 ? Shapes.empty() : Shapes.block();
            default -> state.getCollisionShape(world, pos).isEmpty() ? Shapes.empty() : Shapes.block();
        };
    }

    @Override
    public VoxelShape getFluidShape(FluidState state, BlockGetter world, BlockPos pos) {
        return Shapes.empty();
    }
}