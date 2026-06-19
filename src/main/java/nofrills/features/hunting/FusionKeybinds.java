package nofrills.features.hunting;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.Feature;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class FusionKeybinds {
    public static final Feature instance = new Feature("fusionKeybinds");

    // ta? https://github.com/hannibal002/SkyHanni/commit/571f3976570092cc054f201a8fd71c7d672393f2

    public static final SettingKeybind repeat = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "repeat", instance.key());
    public static final SettingKeybind confirm = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "confirm", instance.key());
    public static final SettingKeybind cancel = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "cancel", instance.key());

    private static boolean isBindValid(ItemStack stack, String title, int key) {
        if (!stack.isEmpty()) {
            String name = Utils.toPlain(stack.getHoverName());
            if (title.equals("Fusion Box")) {
                return name.equals("Repeat Previous Fusion") && key == repeat.value();
            }
            if (title.equals("Confirm Fusion")) {
                Item item = stack.getItem();
                return (item.equals(Items.LIME_TERRACOTTA) && key == confirm.value()) || (item.equals(Items.RED_TERRACOTTA) && key == cancel.value());
            }
        }
        return false;
    }

    @EventHandler
    private static void onKey(InputEvent event) {
        if (instance.isActive() && mc.screen instanceof ContainerScreen container) {
            String title = container.getTitle().getString();
            if (!title.equals("Fusion Box") && !title.equals("Confirm Fusion")) {
                return;
            }
            for (Slot slot : container.getMenu().slots) {
                if (isBindValid(slot.getItem(), title, event.key)) {
                    if (event.action == GLFW.GLFW_PRESS) {
                        mc.gameMode.handleContainerInput(container.getMenu().containerId, slot.index, GLFW.GLFW_MOUSE_BUTTON_3, ContainerInput.CLONE, mc.player);
                    }
                    event.cancel();
                    return;
                }
            }
        }
    }
}
