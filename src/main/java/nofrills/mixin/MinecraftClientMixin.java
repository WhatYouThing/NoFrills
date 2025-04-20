package nofrills.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import nofrills.config.Config;
import nofrills.events.ScreenOpenEvent;
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
    @Nullable
    public HitResult crosshairTarget;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @WrapWithCondition(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V"))
    private boolean onDropSwing(ClientPlayerEntity instance, Hand hand) {
        return !Utils.isFixEnabled(Config.noDropSwing);
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

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void onPickobulus(CallbackInfo ci) {
        if (Config.safePickobulus && (Utils.isOnPrivateIsland() || Utils.isInGarden())) {
            if (this.crosshairTarget instanceof BlockHitResult blockHit) {
                Block block = mc.world.getBlockState(blockHit.getBlockPos()).getBlock();
                if (block.equals(Blocks.CHEST) || block.equals(Blocks.TRAPPED_CHEST)) {
                    return;
                }
            } else if (this.crosshairTarget != null) {
                return;
            }
            if (Utils.getRightClickAbility(mc.player.getMainHandStack()).contains("Pickobulus")) {
                Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.MASTER, 1.0f, 0.0f);
                ci.cancel();
            }
        }
    }
}
