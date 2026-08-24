package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import nofrills.events.AttackBlockEvent;
import nofrills.events.InteractBlockEvent;
import nofrills.events.InteractEntityEvent;
import nofrills.events.InteractItemEvent;
import nofrills.features.misc.UnfocusedTweaks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public ClientLevel level;
    @Shadow
    @Final
    private SoundManager soundManager;

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void onInteractEntity(CallbackInfo ci, @Local Entity entity, @Local EntityHitResult entityHitResult) {
        if (eventBus.post(new InteractEntityEvent(entity, entityHitResult)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void onInteractBlock(CallbackInfo ci, @Local BlockHitResult blockHitResult) {
        if (eventBus.post(new InteractBlockEvent(blockHitResult)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void onInteractItem(CallbackInfo ci) {
        if (eventBus.post(new InteractItemEvent()).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private void onAttackBlock(CallbackInfoReturnable<Boolean> cir, @Local(name = "blockHit") BlockHitResult blockHit, @Local(name = "pos") BlockPos pos) {
        eventBus.post(new AttackBlockEvent(blockHit, pos));
    }

    @ModifyReturnValue(method = "isGameLoadFinished", at = @At("RETURN"))
    private boolean isGameLoadFinished(boolean original) {
        if (UnfocusedTweaks.isActive() && UnfocusedTweaks.noWorldRender.value()) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/FramerateLimitTracker;getFramerateLimit()I"))
    private int skipLimiter(int original) {
        if (UnfocusedTweaks.isActive() && UnfocusedTweaks.fpsLimit.value() > 0) {
            return UnfocusedTweaks.fpsLimit.value();
        }
        if (UnfocusedTweaks.instance.isActive() && UnfocusedTweaks.noVanilla.value()) {
            return mc.options.framerateLimit().get();
        }
        return original;
    }

    @Inject(method = "pauseIfInactive", at = @At("TAIL"))
    private void onAfterFocusChanged(CallbackInfo ci) {
        if (UnfocusedTweaks.instance.isActive() && UnfocusedTweaks.muteSounds.value() && this.soundManager != null) {
            for (SoundSource category : SoundSource.values()) {
                try {
                    this.soundManager.refreshCategoryVolume(category);
                } catch (Exception _) {
                }
            }
        }
    }
}
