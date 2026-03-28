package nofrills.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import nofrills.events.BlockUpdateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = "updatePOIOnBlockStateChange", at = @At(value = "TAIL"))
    private void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        eventBus.post(new BlockUpdateEvent(pos, oldBlock, newBlock));
    }
}