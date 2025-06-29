package nofrills.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import nofrills.config.Config;
import nofrills.events.InteractBlockEvent;
import nofrills.events.InteractEntityEvent;
import nofrills.events.InteractItemEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.features.AttributeDebug;
import nofrills.misc.Utils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    @Nullable
    public ClientWorld world;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @WrapWithCondition(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V"))
    private boolean onDropSwing(ClientPlayerEntity instance, Hand hand) {
        return !Utils.isFixEnabled(Config.noDropSwing);
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onBeforeOpenScreen(Screen screen, CallbackInfo ci) {
        if (Config.noLoadingScreen && screen instanceof DownloadingTerrainScreen) {
            mc.setScreen(null);
            ci.cancel();
        }
    }

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void onOpenScreen(Screen screen, CallbackInfo ci) {
        if (screen != null && world != null) {
            if (Utils.isFixEnabled(Config.clearCursorStack) && screen instanceof HandledScreen<?> handledScreen) {
                handledScreen.getScreenHandler().setCursorStack(ItemStack.EMPTY);
            }
            eventBus.post(new ScreenOpenEvent(screen));
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

    @Inject(method = "stop", at = @At("HEAD"))
    private void onBeforeStop(CallbackInfo ci) {
        AttributeDebug.saveData();
    }
}
