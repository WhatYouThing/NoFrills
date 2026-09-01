package nofrills.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.entity.player.Inventory;
import nofrills.events.InputEvent;
import nofrills.features.farming.MouseLock;
import nofrills.features.misc.HotbarScrollLock;
import nofrills.features.tweaks.NoCursorReset;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    private double xpos;

    @Shadow
    private double ypos;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (eventBus.post(new InputEvent(rawButtonInfo, action)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void onMouseMove(double mousea, CallbackInfo ci) {
        if (MouseLock.instance.isActive() && MouseLock.locked) {
            ci.cancel();
        }
    }

    @Inject(method = "grabMouse", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getScreenWidth()I"))
    private void onBeforeUpdatePos(CallbackInfo ci) {
        if (NoCursorReset.instance.isActive()) {
            NoCursorReset.updateCursorPos(this.xpos, this.ypos);
        }
    }

    @WrapOperation(method = "releaseMouse", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"))
    private void onReleaseMouse(Window window, int cursorMode, double xpos, double ypos, Operation<Void> original) {
        if (NoCursorReset.isActive() && NoCursorReset.isPosStored()) {
            this.xpos = NoCursorReset.cursorX;
            this.ypos = NoCursorReset.cursorY;
            original.call(window, cursorMode, NoCursorReset.cursorX, NoCursorReset.cursorY);
            GLFW.glfwSetCursorPos(window.handle(), NoCursorReset.cursorX, NoCursorReset.cursorY);
        } else {
            original.call(window, cursorMode, xpos, ypos);
        }
    }

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"), cancellable = true)
    private void onBeforeSetSlot(long handle, double xoffset, double yoffset, CallbackInfo ci, @Local(name = "inventory") Inventory inventory) {
        if (HotbarScrollLock.instance.isActive()) {
            int selected = inventory.getSelectedSlot();
            if (selected == 0 && (xoffset < 0.0 || yoffset > 0.0)) {
                ci.cancel();
            } else if (selected == 8 && (xoffset > 0.0 || yoffset < 0.0)) {
                ci.cancel();
            }
        }
    }
}
