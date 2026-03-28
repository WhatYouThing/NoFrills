package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import nofrills.config.Feature;
import nofrills.config.SettingInt;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class HotbarSwap {
    public static final Feature instance = new Feature("hotbarSwap");

    public static final SettingInt override = new SettingInt(8, "override", instance.key());

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && event.key == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.modifiers == 2) {
            if (mc.screen instanceof InventoryScreen screen) {
                Slot focusedSlot = Utils.getFocusedSlot();
                if (focusedSlot != null && !focusedSlot.getItem().isEmpty()) {
                    int focusedSlotId = focusedSlot.getContainerSlot();
                    if (focusedSlotId >= 9 && focusedSlotId <= 35) {
                        if (event.action == GLFW.GLFW_PRESS) {
                            int button = focusedSlotId % 9;
                            if (button == 8) {
                                button = override.value() - 1;
                            }
                            mc.gameMode.handleContainerInput(screen.getMenu().containerId, focusedSlotId, button, ContainerInput.SWAP, mc.player);
                        }
                        event.cancel();
                    }
                }
            }
        }
    }
}
