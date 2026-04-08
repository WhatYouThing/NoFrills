package nofrills.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import nofrills.features.tweaks.NoGhostPlace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void onPlaceBlock(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (NoGhostPlace.instance.isActive() && NoGhostPlace.isNonPlaceable(context)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSoundGroup()Lnet/minecraft/sound/BlockSoundGroup;"), cancellable = true)
    private void beforeGetSoundGroup(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (NoGhostPlace.instance.isActive() && NoGhostPlace.isNonPlaceable(context)) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}