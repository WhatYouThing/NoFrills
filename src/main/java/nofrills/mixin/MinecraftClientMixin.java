package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import nofrills.config.Config;
import nofrills.events.*;
import nofrills.features.hunting.ShardTracker;
import nofrills.features.misc.UnfocusedTweaks;
import nofrills.features.tweaks.NoCursorReset;
import nofrills.features.tweaks.NoDropSwing;
import nofrills.features.tweaks.NoLoadingScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientWorld world;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Unique
    @Final
    SoundSystem getSoundSystem() {
        if (mc.getSoundManager() != null) {
            return mc.getSoundManager().soundSystem;
        }
        return null;
    }

    @WrapWithCondition(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V"))
    private boolean onDropSwing(ClientPlayerEntity instance, Hand hand) {
        return !NoDropSwing.active();
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onBeforeOpenScreen(Screen screen, CallbackInfo ci) {
        if (NoLoadingScreen.instance.isActive() && screen instanceof DownloadingTerrainScreen) {
            this.setScreen(null);
            ci.cancel();
        }
    }

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void onOpenScreen(Screen screen, CallbackInfo ci) {
        if (this.world == null) return;
        if (screen != null) {
            eventBus.post(new ScreenOpenEvent(screen));
        } else {
            if (ShardTracker.instance.isActive()) ShardTracker.refreshDisplay();
            if (NoCursorReset.instance.isActive()) NoCursorReset.startTicking();
        }
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactEntityAtLocation(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/hit/EntityHitResult;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"), cancellable = true)
    private void onInteractEntity(CallbackInfo ci, @Local Entity entity, @Local EntityHitResult entityHitResult) {
        if (eventBus.post(new InteractEntityEvent(entity, entityHitResult)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"), cancellable = true)
    private void onInteractBlock(CallbackInfo ci, @Local BlockHitResult blockHitResult) {
        if (eventBus.post(new InteractBlockEvent(blockHitResult)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"), cancellable = true)
    private void onInteractItem(CallbackInfo ci) {
        if (eventBus.post(new InteractItemEvent()).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"), cancellable = true)
    private void onAttackBlock(CallbackInfoReturnable<Boolean> cir, @Local BlockHitResult blockHitResult, @Local BlockPos blockPos) {
        eventBus.post(new AttackBlockEvent(blockHitResult, blockPos));
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void beforeStop(CallbackInfo ci) {
        Config.save();
    }

    @Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=yield"))
    private void beforeRender(CallbackInfo ci) {
        if (UnfocusedTweaks.active() && UnfocusedTweaks.fpsLimit.value() > 0) {
            RenderSystem.limitDisplayFPS(UnfocusedTweaks.fpsLimit.value());
        }
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;skipGameRender:Z"))
    private boolean skipRender(boolean original) {
        if (mc.world != null && UnfocusedTweaks.active() && UnfocusedTweaks.noWorldRender.value()) {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/InactivityFpsLimiter;update()I"))
    private int skipLimiter(int original) {
        if (UnfocusedTweaks.instance.isActive() && UnfocusedTweaks.noVanilla.value()) {
            return mc.options.getMaxFps().getValue();
        }
        return original;
    }

    @Inject(method = "onWindowFocusChanged", at = @At("TAIL"))
    private void onFocusChanged(boolean focused, CallbackInfo ci) {
        if (this.getSoundSystem() != null && UnfocusedTweaks.instance.isActive() && UnfocusedTweaks.muteSounds.value()) {
            this.getSoundSystem().updateSoundVolume(SoundCategory.MASTER, !focused ? 0.0f : mc.options.getSoundVolumeOption(SoundCategory.MASTER).getValue().floatValue());
        }
    }
}
