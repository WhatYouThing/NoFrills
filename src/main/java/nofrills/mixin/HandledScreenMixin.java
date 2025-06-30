package nofrills.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.events.DrawItemTooltip;
import nofrills.features.AttributeDebug;
import nofrills.features.DungeonSolvers;
import nofrills.features.SlotBinding;
import nofrills.hud.LeapMenuButton;
import nofrills.misc.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
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
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_CAPTURED;

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
    List<LeapMenuButton> leapButtons = new ArrayList<>();
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
    private boolean isLeapMenu() {
        return Utils.isLeapMenu(title.getString());
    }

    @Unique
    private boolean isSlotBindingActive() {
        return Config.slotBinding && mc.currentScreen instanceof InventoryScreen;
    }

    @Unique
    private void drawLine(DrawContext context, int firstSlot, int secondSlot, RenderColor color) {
        context.draw(drawer -> {
            Slot slot1 = handler.getSlot(firstSlot);
            Slot slot2 = handler.getSlot(secondSlot);
            Matrix4f mat = context.getMatrices().peek().getPositionMatrix();
            VertexConsumer consumer = drawer.getBuffer(Rendering.Layers.GuiLine);
            consumer.vertex(mat, slot1.x + 8, slot1.y + 8, 1.0f).color(color.argb);
            consumer.vertex(mat, slot2.x + 8, slot2.y + 8, 1.0f).color(color.argb);
        });
    }

    @Unique
    private void drawBorder(DrawContext context, int slotId, RenderColor color) {
        Slot slot = handler.getSlot(slotId);
        context.drawBorder(slot.x, slot.y, 16, 16, color.argb);
    }

    @Override
    public void nofrills_mod$addLeapButton(int slotId, String name, String dungeonClass, RenderColor classColor) {
        leapButtons.add(new LeapMenuButton(slotId, leapButtons.size(), name, dungeonClass, classColor));
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (isLeapMenu() || (Config.ignoreBackground && isStackNameEmpty(slot)) || SlotOptions.isSlotDisabled(slot)) {
            ci.cancel();
            return;
        }
        if (Config.fastTerminals && DungeonSolvers.isInTerminal && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            mc.interactionManager.clickSlot(handler.syncId, slot != null ? slot.id : slotId, GLFW.GLFW_MOUSE_BUTTON_3, SlotActionType.CLONE, mc.player);
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
        if ((Config.ignoreBackground && isStackNameEmpty(focusedSlot)) || SlotOptions.isSlotDisabled(focusedSlot)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlotHighlightFront", at = @At("HEAD"), cancellable = true)
    private void onDrawHighlightFront(DrawContext context, CallbackInfo ci) {
        if ((Config.ignoreBackground && isStackNameEmpty(focusedSlot)) || SlotOptions.isSlotDisabled(focusedSlot)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void onDrawTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if ((Config.solveTerminals && DungeonSolvers.isInTerminal) || (Config.ignoreBackground && isStackNameEmpty(focusedSlot)) || SlotOptions.isSlotDisabled(focusedSlot)) {
            ci.cancel();
        }
        if (Config.slotBinding && SlotBinding.lastSlot != -1) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;getTooltipFromItem(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"))
    private List<Text> onGetTooltipFromItem(List<Text> original) {
        if (focusedSlot != null) {
            ItemStack stack = focusedSlot.getStack();
            NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (!stack.isEmpty()) {
                eventBus.post(new DrawItemTooltip(original, stack, component));
            }
        }
        return original;
    }

    @ModifyExpressionValue(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack onDrawStack(ItemStack original, DrawContext context, Slot slot) {
        ItemStack stack = SlotOptions.getSpoofedStack(slot);
        if (stack != null) {
            return stack;
        }
        return original;
    }

    @ModifyExpressionValue(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack onDrawSpoofedTooltip(ItemStack original) {
        ItemStack stack = SlotOptions.getSpoofedStack(focusedSlot);
        if (stack != null) {
            return stack;
        }
        return original;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (isLeapMenu()) {
            for (LeapMenuButton button : leapButtons) {
                if (button.slotId != -1) {
                    button.hovered = button.isHovered(mouseX, mouseY);
                }
                button.render(context, mouseX, mouseY, delta);
            }
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onAfterRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().push();
        context.getMatrices().translate(this.x, this.y, 0.0f);
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 100.0f);
        if (isSlotBindingActive() && focusedSlot != null) {
            if (SlotBinding.isHotbar(focusedSlot.id)) {
                String name = "hotbar" + SlotBinding.toHotbarNumber(focusedSlot.id);
                if (Config.slotBindData.has(name)) {
                    for (JsonElement element : Config.slotBindData.get(name).getAsJsonObject().get("binds").getAsJsonArray()) {
                        if (Config.slotBindingLines) {
                            drawLine(context, focusedSlot.id, element.getAsInt(), SlotBinding.boundColor);
                        }
                        if (Config.slotBindingBorders) {
                            drawBorder(context, element.getAsInt(), SlotBinding.boundColor);
                        }
                    }
                }
            } else if (SlotBinding.isValid(focusedSlot.id)) {
                for (int i = 1; i <= 8; i++) {
                    String name = "hotbar" + i;
                    if (Config.slotBindData.has(name)) {
                        for (JsonElement element : Config.slotBindData.get(name).getAsJsonObject().get("binds").getAsJsonArray()) {
                            if (element.getAsInt() == focusedSlot.id) {
                                if (Config.slotBindingLines) {
                                    drawLine(context, focusedSlot.id, i + 35, SlotBinding.boundColor);
                                }
                                if (Config.slotBindingBorders) {
                                    drawBorder(context, i + 35, SlotBinding.boundColor);
                                }
                            }
                        }
                    }
                }
            }
            if (SlotBinding.lastSlot != -1) {
                drawBorder(context, SlotBinding.lastSlot, SlotBinding.bindingColor);
                drawBorder(context, focusedSlot.id, SlotBinding.bindingColor);
                drawLine(context, SlotBinding.lastSlot, focusedSlot.id, SlotBinding.bindingColor);
            }
        }
        if (AttributeDebug.isEnabled && this.title.getString().equals("Shard Fusion")) {
            for (Slot slot : new ArrayList<>(AttributeDebug.highlightedSlots)) {
                context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, RenderColor.fromHex(0xff0000).argb);
            }
        }
        context.getMatrices().pop();
        context.getMatrices().pop();
        if (AttributeDebug.isEnabled && this.title.getString().equals("Shard Fusion")) {
            if (AttributeDebug.data.has("recipes")) {
                context.drawText(this.textRenderer, Utils.format("Input Combinations Found: {}", AttributeDebug.data.getAsJsonObject("recipes").size()), 50, 50, 0xffffff, true);
            }
            if (AttributeDebug.data.has("shards")) {
                context.drawText(this.textRenderer, Utils.format("Shard Details Found: {}", AttributeDebug.data.getAsJsonObject("shards").size()), 50, 60, 0xffffff, true);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (isLeapMenu() && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            for (LeapMenuButton leapButton : leapButtons) {
                if (leapButton.slotId != -1 && leapButton.isHovered(mouseX, mouseY)) {
                    mc.interactionManager.clickSlot(handler.syncId, leapButton.slotId, 0, SlotActionType.PICKUP, mc.player);
                    this.handler.setCursorStack(ItemStack.EMPTY);
                    if (Config.leapOverlayMsg && !sentLeapMsg) {
                        sentLeapMsg = true;
                        Utils.sendMessage("/pc Leaped to " + leapButton.player.getString() + "!");
                    }
                    cir.setReturnValue(true);
                }
            }
            cir.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isInCreativeMode()Z"))
    private boolean onMiddleClick(boolean original) {
        if (Utils.isFixEnabled(Config.middleClickFix)) {
            return true;
        }
        return original;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (isLeapMenu()) {
            int x = mc.getWindow().getWidth() / 2;
            int y = mc.getWindow().getHeight() / 2;
            this.x = x;
            this.y = y;
            InputUtil.setCursorParameters(mc.getWindow().getHandle(), GLFW_CURSOR_CAPTURED, x, y);
        }
    }
}
