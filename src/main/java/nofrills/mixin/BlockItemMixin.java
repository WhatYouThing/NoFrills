package nofrills.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import nofrills.features.tweaks.NoAbilityPlace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void onPlaceBlock(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (NoAbilityPlace.instance.isActive() && NoAbilityPlace.hasAbility(context)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"), cancellable = true)
    private void beforeGetSoundGroup(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (NoAbilityPlace.instance.isActive() && NoAbilityPlace.hasAbility(context)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}