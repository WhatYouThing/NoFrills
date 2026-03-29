package nofrills.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

public class AttackBlockEvent {
    public BlockHitResult blockHitResult;
    public BlockPos blockPos;

    public AttackBlockEvent(BlockHitResult blockHitResult, BlockPos blockPos) {
        this.blockHitResult = blockHitResult;
        this.blockPos = blockPos;
    }
}
