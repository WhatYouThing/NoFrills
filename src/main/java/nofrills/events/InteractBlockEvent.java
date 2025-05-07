package nofrills.events;

import net.minecraft.util.hit.BlockHitResult;

public class InteractBlockEvent extends Cancellable {
    public BlockHitResult blockHitResult;

    public InteractBlockEvent(BlockHitResult blockHitResult) {
        this.blockHitResult = blockHitResult;
    }
}
