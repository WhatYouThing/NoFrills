package nofrills.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

public class EtherwarpRaycastContext extends ClipContext {
    public EtherwarpRaycastContext(Vec3 start, Vec3 end, Block shapeType, Fluid fluidHandling, CollisionContext shapeContext) {
        super(start, end, shapeType, fluidHandling, shapeContext);
    }

    public EtherwarpRaycastContext(Vec3 start, Vec3 end, ClipContext.Block shapeType, ClipContext.Fluid fluidHandling, Entity entity) {
        this(start, end, shapeType, fluidHandling, CollisionContext.of(entity));
    }

    @Override
    public @NonNull VoxelShape getBlockShape(BlockState state, @NonNull BlockGetter world, @NonNull BlockPos pos) {
        return switch (state.getBlock()) {
            case PlayerHeadBlock ignored -> Shapes.empty();
            case SkullBlock ignored -> Shapes.empty();
            case WallSkullBlock ignored -> Shapes.empty();
            case CocoaBlock ignored -> Shapes.empty();
            case FlowerPotBlock ignored -> Shapes.empty();
            case LadderBlock ignored -> Shapes.empty();
            case WallSignBlock ignored -> Shapes.block();
            case SignBlock ignored -> Shapes.block();
            case SnowLayerBlock ignored -> Shapes.block();
            case PressurePlateBlock ignored -> Shapes.block();
            case WeightedPressurePlateBlock ignored -> Shapes.block();
            default -> state.getCollisionShape(world, pos).isEmpty() ? Shapes.empty() : Shapes.block();
        };
    }

    @Override
    public @NonNull VoxelShape getFluidShape(@NonNull FluidState state, @NonNull BlockGetter world, @NonNull BlockPos pos) {
        return Shapes.empty();
    }
}