package nofrills.features.hunting;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class FusionKeybinds {
    public static final Feature instance = new Feature("fusionKeybinds");

    // ta? https://github.com/hannibal002/SkyHanni/commit/571f3976570092cc054f201a8fd71c7d672393f2

    public static final SettingKeybind repeat = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "repeat", instance.key());
    public static final SettingKeybind confirm = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "confirm", instance.key());
    public static final SettingKeybind cancel = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "cancel", instance.key());

    private static boolean isValidBind(ItemStack stack, int key) {
        if (stack != null && !stack.isEmpty()) {
            String name = Formatting.strip(stack.getName().getString());
            return (name.equals("Repeat Previous Fusion") && key == repeat.value()) ||
                    (name.equals("Fusion") && stack.getItem().equals(Items.LIME_TERRACOTTA) && key == confirm.value()) ||
                    (name.equals("Cancel") && stack.getItem().equals(Items.RED_TERRACOTTA) && key == cancel.value());
        }
        return false;
    }

    @EventHandler
    private static void onKey(InputEvent event) {
        if (instance.isActive() && event.action == GLFW.GLFW_PRESS && mc.currentScreen instanceof GenericContainerScreen container) {
            String title = container.getTitle().getString();
            if (title.equals("Fusion Box") || title.equals("Confirm Fusion")) {
                for (Slot slot : container.getScreenHandler().slots) {
                    if (isValidBind(slot.getStack(), event.key)) {
                        mc.interactionManager.clickSlot(container.getScreenHandler().syncId, slot.id, GLFW.GLFW_MOUSE_BUTTON_3, SlotActionType.CLONE, mc.player);
                        event.cancel();
                        return;
                    }
                }
            }
        }
    }
}
