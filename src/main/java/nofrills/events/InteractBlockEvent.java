package nofrills.events;

import net.minecraft.world.phys.BlockHitResult;

public class InteractBlockEvent extends Cancellable {
    public BlockHitResult blockHitResult;

    public InteractBlockEvent(BlockHitResult blockHitResult) {
        this.blockHitResult = blockHitResult;
    }
}
