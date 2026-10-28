package nofrills.features.hunting;

import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.Feature;
import nofrills.config.SettingKeybind;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

@EventListener
public class FusionKeybinds {
    public static final Feature instance = new Feature("fusionKeybinds");

    // ta? https://github.com/hannibal002/SkyHanni/commit/571f3976570092cc054f201a8fd71c7d672393f2

    public static final SettingKeybind repeat = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "repeat", instance.key());
    public static final SettingKeybind confirm = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "confirm", instance.key());
    public static final SettingKeybind cancel = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "cancel", instance.key());

    private static boolean isBindValid(ItemStack stack, String title, int key) {
        if (!stack.isEmpty()) {
            String name = Utils.toPlain(stack.getHoverName());
            if (title.contains("Fusion Box")) {
                return name.equals("Repeat Previous Fusion") && key == repeat.value();
            }
            if (title.equals("Confirm Fusion")) {
                Item item = stack.getItem();
                return (item.equals(Items.DYED_TERRACOTTA.lime()) && key == confirm.value()) || (item.equals(Items.DYED_TERRACOTTA.red()) && key == cancel.value());
            }
        }
        return false;
    }

    @EventHandler
    private static void onKey(InputEvent event) {
        if (instance.isActive() && mc.gui.screen() instanceof ContainerScreen container) {
            String title = container.getTitle().getString();
            if (!title.contains("Fusion Box") && !title.equals("Confirm Fusion")) {
                return;
            }
            for (Slot slot : container.getMenu().slots) {
                if (isBindValid(slot.getItem(), title, event.key)) {
                    if (event.action == InputConstants.PRESS) {
                        Utils.click(container.getMenu().containerId, slot.index, InputConstants.MOUSE_BUTTON_MIDDLE, ContainerInput.CLONE);
                    }
                    event.cancel();
                    return;
                }
            }
        }
    }
}
