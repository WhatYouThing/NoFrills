package nofrills.events;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public class BlockUpdateEvent {

    public BlockPos pos;
    public BlockState oldState;
    public BlockState newState;

    public BlockUpdateEvent(BlockPos pos, BlockState oldState, BlockState newState) {
        this.pos = pos;
        this.oldState = oldState;
        this.newState = newState;
    }
}
