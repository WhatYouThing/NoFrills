package nofrills.events;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class AttackBlockEvent {
    public BlockHitResult blockHitResult;
    public BlockPos blockPos;

    public AttackBlockEvent(BlockHitResult blockHitResult, BlockPos blockPos) {
        this.blockHitResult = blockHitResult;
        this.blockPos = blockPos;
    }
}
