package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import nofrills.config.Config;
import nofrills.events.InputEvent;
import nofrills.mixin.HandledScreenAccessor;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class HotbarSwap {
    @EventHandler
    public static void onKey(InputEvent event) {
        if (Config.hotbarSwap) {
            if (event.key == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.action == GLFW.GLFW_PRESS && event.modifiers == 2) {
                if (mc.currentScreen instanceof InventoryScreen screen) {
                    Slot focusedSlot = ((HandledScreenAccessor) screen).getFocusedSlot();
                    if (focusedSlot != null && !focusedSlot.getStack().isEmpty()) {
                        int focusedSlotId = focusedSlot.getIndex();
                        if (focusedSlotId >= 9 && focusedSlotId <= 35) {
                            int button = focusedSlotId % 9;
                            if (button == 8 && Config.hotbarSwapOverride != 9) {
                                button = Config.hotbarSwapOverride - 1;
                            }
                            mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, focusedSlotId, button, SlotActionType.SWAP, mc.player);
                            event.cancel();
                        }
                    }
                }
            }
        }
    }
}
