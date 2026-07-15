package nofrills.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockUpdateEvent {

    public BlockPos pos;
    public BlockState oldState;
    public BlockState newState;
    public Block oldBlock;
    public Block newBlock;

    public BlockUpdateEvent(BlockPos pos, BlockState oldState, BlockState newState) {
        this.pos = pos;
        this.oldState = oldState;
        this.newState = newState;
        this.oldBlock = oldState.getBlock();
        this.newBlock = newState.getBlock();
    }
}
