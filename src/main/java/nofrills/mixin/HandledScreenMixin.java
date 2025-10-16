package nofrills.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.renderstate.LineElementRenderState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import nofrills.events.SlotClickEvent;
import nofrills.events.TooltipRenderEvent;
import nofrills.features.dungeons.LeapOverlay;
import nofrills.features.dungeons.TerminalSolvers;
import nofrills.features.general.NoRender;
import nofrills.features.general.SlotBinding;
import nofrills.features.kuudra.KuudraChestValue;
import nofrills.features.tweaks.MiddleClickFix;
import nofrills.misc.RenderColor;
import nofrills.misc.ScreenOptions;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenOptions {
    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Shadow
    @Final
    protected T handler;
    @Shadow
    protected int y;
    @Shadow
    protected int x;
    @Unique
    List<LeapOverlay.LeapButton> leapButtons = new ArrayList<>();
    @Unique
    boolean sentLeapMsg = false;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private boolean isStackNameEmpty(Slot slot) {
        if (title.getString().startsWith("Ultrasequencer (")) {
            return false;
        }
        if (slot != null) {
            return slot.getStack().getName().getString().trim().isEmpty();
        }
        return false;
    }

    @Unique
    private boolean isSlotBindingActive() {
        return SlotBinding.instance.isActive() && mc.currentScreen instanceof InventoryScreen;
    }

    @Unique
    private boolean shouldIgnoreBackground(Slot slot) {
        return NoRender.instance.isActive() && NoRender.emptyTooltips.value() && isStackNameEmpty(slot);
    }

    @Unique
    private void drawLine(DrawContext context, int firstSlot, int secondSlot, double width, RenderColor color) {
        Slot slot1 = handler.getSlot(firstSlot);
        Slot slot2 = handler.getSlot(secondSlot);
        drawLine(context, RenderPipelines.GUI, slot1.x + 8, slot1.y + 8, slot2.x + 8, slot2.y + 8, width, Color.ofArgb(color.argb));
    }

    @Unique
    public void drawLine(DrawContext context, RenderPipeline pipeline, int x1, int y1, int x2, int y2, double thickness, Color color) {
        context.state.addSimpleElement(new LineElementRenderState(
                pipeline,
                new Matrix3x2f(context.getMatrices()),
                context.scissorStack.peekLast(),
                x1, y1, x2, y2,
                thickness,
                color
        ));
    }

    @Unique
    private void drawBorder(DrawContext context, int slotId, RenderColor color) {
        Slot slot = handler.getSlot(slotId);
        context.drawBorder(slot.x, slot.y, 16, 16, color.argb);
    }

    @Override
    public void nofrills_mod$addLeapButton(int slotId, String name, String dungeonClass, RenderColor classColor) {
        leapButtons.add(new LeapOverlay.LeapButton(slotId, leapButtons.size(), name, dungeonClass, classColor));
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (LeapOverlay.isLeapMenu(this.title.getString()) || shouldIgnoreBackground(slot) || SlotOptions.isSlotDisabled(slot)) {
            ci.cancel();
            return;
        }
        if (eventBus.post(new SlotClickEvent(slot, slot != null ? slot.id : slotId, button, actionType, this.title.getString())).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("TAIL"))
    private void onClickSlotTail(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (SlotOptions.isSlotSpoofed(slot)) {
            this.handler.setCursorStack(ItemStack.EMPTY); // prevents the real item from showing at the cursor
        }
    }

    @Inject(method = "drawSlotHighlightBack", at = @At("HEAD"), cancellable = true)
    private void onDrawHighlight(DrawContext context, CallbackInfo ci) {
        if (shouldIgnoreBackground(focusedSlot) || SlotOptions.isSlotDisabled(focusedSlot)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlotHighlightFront", at = @At("HEAD"), cancellable = true)
    private void onDrawHighlightFront(DrawContext context, CallbackInfo ci) {
        if (shouldIgnoreBackground(focusedSlot) || SlotOptions.isSlotDisabled(focusedSlot)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V"), cancellable = true)
    private void onDrawTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (TerminalSolvers.shouldHideTooltips(this.title.getString()) || shouldIgnoreBackground(focusedSlot) || SlotOptions.isSlotDisabled(focusedSlot) || SlotBinding.isBinding()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;getTooltipFromItem(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"))
    private List<Text> onGetTooltipFromItem(List<Text> original, @Local ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            eventBus.post(new TooltipRenderEvent(original, itemStack, Utils.getCustomData(itemStack), this.getTitle().getString()));
        }
        return original;
    }

    @ModifyExpressionValue(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack onDrawStack(ItemStack original, DrawContext context, Slot slot) {
        if (SlotOptions.isSlotSpoofed(slot)) {
            return SlotOptions.getSpoofedStack(slot);
        }
        return original;
    }

    @ModifyExpressionValue(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack onDrawSpoofedTooltip(ItemStack original) {
        if (SlotOptions.isSlotSpoofed(focusedSlot)) {
            return SlotOptions.getSpoofedStack(focusedSlot);
        }
        return original;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (LeapOverlay.isLeapMenu(this.title.getString())) {
            for (LeapOverlay.LeapButton button : leapButtons) {
                if (button.slotId != -1) {
                    button.hovered = button.isHovered(mouseX, mouseY);
                }
                button.render(context, mouseX, mouseY, delta);
            }
            ci.cancel();
        }
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void onRenderBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (LeapOverlay.isLeapMenu(this.title.getString())) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void onRenderSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (SlotOptions.hasBackground(slot)) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, SlotOptions.getBackgroundColor(slot).argb);
        }
    }

    @Inject(method = "renderMain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlightBack(Lnet/minecraft/client/gui/DrawContext;)V"))
    private void onBeforeHighlightRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (isSlotBindingActive() && focusedSlot != null) {
            if (SlotBinding.isHotbar(focusedSlot.id)) {
                String name = "hotbar" + SlotBinding.toHotbarNumber(focusedSlot.id);
                if (SlotBinding.data.value().has(name)) {
                    for (JsonElement element : SlotBinding.data.value().get(name).getAsJsonObject().get("binds").getAsJsonArray()) {
                        if (SlotBinding.lines.value()) {
                            drawLine(context, focusedSlot.id, element.getAsInt(), SlotBinding.lineWidth.value(), SlotBinding.bound.value());
                        }
                        if (SlotBinding.borders.value()) {
                            drawBorder(context, element.getAsInt(), SlotBinding.bound.value());
                        }
                    }
                }
            } else if (SlotBinding.isValid(focusedSlot.id)) {
                for (int i = 1; i <= 8; i++) {
                    String name = "hotbar" + i;
                    if (SlotBinding.data.value().has(name)) {
                        for (JsonElement element : SlotBinding.data.value().get(name).getAsJsonObject().get("binds").getAsJsonArray()) {
                            if (element.getAsInt() == focusedSlot.id) {
                                if (SlotBinding.lines.value()) {
                                    drawLine(context, focusedSlot.id, i + 35, SlotBinding.lineWidth.value(), SlotBinding.bound.value());
                                }
                                if (SlotBinding.borders.value()) {
                                    drawBorder(context, i + 35, SlotBinding.bound.value());
                                }
                            }
                        }
                    }
                }
            }
            if (SlotBinding.lastSlot != -1) {
                drawBorder(context, SlotBinding.lastSlot, SlotBinding.binding.value());
                drawBorder(context, focusedSlot.id, SlotBinding.binding.value());
                drawLine(context, SlotBinding.lastSlot, focusedSlot.id, SlotBinding.lineWidth.value(), SlotBinding.binding.value());
            }
        }
    }

    @SuppressWarnings("mapping")
    @Inject(method = "renderMain", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;"))
    private void onAfterRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (KuudraChestValue.instance.isActive() && KuudraChestValue.currentValue > 0.0) {
            Slot targetSlot = this.handler.getSlot(4);
            String value = Utils.format("Chest Value: {}", Utils.formatSeparator(KuudraChestValue.currentValue));
            int width = mc.textRenderer.getWidth(value);
            int baseX = targetSlot.x + 8;
            int baseY = targetSlot.y + 8;
            context.fill((int) Math.floor(baseX - 2 - width * 0.5), baseY - 6, (int) Math.ceil(baseX + 2 + width * 0.5), baseY + 6, KuudraChestValue.background.argb);
            context.drawCenteredTextWithShadow(this.textRenderer, value, baseX, baseY - 4, RenderColor.green.argb);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (LeapOverlay.isLeapMenu(this.title.getString()) && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            for (LeapOverlay.LeapButton leapButton : leapButtons) {
                if (leapButton.slotId != -1 && leapButton.isHovered(mouseX, mouseY)) {
                    mc.interactionManager.clickSlot(handler.syncId, leapButton.slotId, 0, SlotActionType.PICKUP, mc.player);
                    this.handler.setCursorStack(ItemStack.EMPTY);
                    if (LeapOverlay.send.value() && !LeapOverlay.message.value().isEmpty() && !sentLeapMsg) {
                        sentLeapMsg = true;
                        Utils.sendMessage(LeapOverlay.message.value().replace("{name}", leapButton.player.getString()));
                    }
                    cir.setReturnValue(true);
                }
            }
            cir.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isInCreativeMode()Z"))
    private boolean onMiddleClick(boolean original) {
        if (MiddleClickFix.active()) {
            return true;
        }
        return original;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (LeapOverlay.isLeapMenu(this.title.getString())) {
            int x = mc.getWindow().getWidth() / 2;
            int y = mc.getWindow().getHeight() / 2;
            this.x = x;
            this.y = y;
            InputUtil.setCursorParameters(mc.getWindow().getHandle(), InputUtil.GLFW_CURSOR_NORMAL, x, y);
        }
    }
}
