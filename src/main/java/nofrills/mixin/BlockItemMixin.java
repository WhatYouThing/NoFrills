package nofrills.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import nofrills.features.tweaks.NoGhostPlace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "placeBlock", at = @At("HEAD"), cancellable = true)
    private void onPlaceBlock(BlockPlaceContext context, BlockState placementState, CallbackInfoReturnable<Boolean> cir) {
        if (NoGhostPlace.instance.isActive() && NoGhostPlace.isNonPlaceable(context)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"), cancellable = true)
    private void beforeGetSoundGroup(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir) {
        if (NoGhostPlace.instance.isActive() && NoGhostPlace.isNonPlaceable(placeContext)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}