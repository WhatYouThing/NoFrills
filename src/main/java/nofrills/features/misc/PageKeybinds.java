package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import nofrills.config.Feature;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class PageKeybinds {
    public static final Feature instance = new Feature("pageKeybinds");

    public static final SettingKeybind next = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "next", instance.key());
    public static final SettingKeybind previous = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "previous", instance.key());

    private static ButtonType getButtonType(ItemStack stack) {
        if (!stack.isEmpty() && Utils.getSkyblockId(stack).isEmpty()) {
            Item item = stack.getItem();
            String name = Utils.toPlain(stack.getName());
            if (item.equals(Items.ARROW) || item.equals(Items.PLAYER_HEAD)) {
                if (name.startsWith("Next Page") || name.endsWith("Next Page")) return ButtonType.Next;
                if (name.startsWith("Previous Page") || name.endsWith("Previous Page")) return ButtonType.Previous;
            }
        }
        return ButtonType.None;
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && mc.currentScreen instanceof GenericContainerScreen container) {
            for (Slot slot : Utils.getContainerSlots(container.getScreenHandler())) {
                ButtonType type = getButtonType(slot.getStack());
                if ((type.equals(ButtonType.Next) && next.value() == event.key) || (type.equals(ButtonType.Previous) && previous.value() == event.key)) {
                    if (event.action == GLFW.GLFW_PRESS) {
                        int loreSize = Utils.getLoreLines(slot.getStack()).size();
                        mc.interactionManager.clickSlot(
                                container.getScreenHandler().syncId,
                                slot.id,
                                loreSize > 1 ? GLFW.GLFW_MOUSE_BUTTON_LEFT : GLFW.GLFW_MOUSE_BUTTON_3,
                                loreSize > 1 ? SlotActionType.PICKUP : SlotActionType.CLONE,
                                mc.player
                        );
                    }
                    event.cancel();
                    break;
                }
            }
        }
    }

    public enum ButtonType {
        Next,
        Previous,
        None
    }
}
