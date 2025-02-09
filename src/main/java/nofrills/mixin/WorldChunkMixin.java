package nofrills.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import nofrills.events.BlockUpdateEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nofrills.Main.eventBus;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {
    @Shadow
    @Final
    World world;

    @Inject(method = "setBlockState", at = @At("TAIL"))
    private void onBlockUpdate(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        if (world.isClient) {
            eventBus.post(new BlockUpdateEvent(pos, cir.getReturnValue(), state));
        }
    }
}