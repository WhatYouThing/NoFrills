package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import nofrills.config.Feature;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class GuiKeybinds {
    public static final Feature instance = new Feature("guiKeybinds");

    public static final SettingKeybind next = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "next", instance.key());
    public static final SettingKeybind previous = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "previous", instance.key());
    public static final SettingKeybind up = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "up", instance.key());
    public static final SettingKeybind down = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "down", instance.key());
    public static final SettingKeybind back = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "back", instance.key());

    private static ButtonType getButtonType(ItemStack stack) {
        if (!stack.isEmpty() && Utils.getSkyblockId(stack).isEmpty()) {
            Item item = stack.getItem();
            if (item.equals(Items.ARROW) || item.equals(Items.PLAYER_HEAD)) {
                String name = Utils.toPlain(stack.getName());
                if (name.contains("Next Page") || name.contains("Scroll Right")) return ButtonType.Next;
                if (name.contains("Previous Page") || name.contains("Scroll Left")) return ButtonType.Previous;
                if (name.equals("Scroll Up")) return ButtonType.Up;
                if (name.equals("Scroll Down")) return ButtonType.Down;
                if (name.endsWith("Back")) return ButtonType.Back;
            }
        }
        return ButtonType.None;
    }

    private static SettingKeybind getBoundKey(ButtonType type) {
        return switch (type) {
            case Next -> next;
            case Previous -> previous;
            case Up -> up;
            case Down -> down;
            case Back -> back;
            case None -> null;
        };
    }

    private static void click(int syncId, Slot slot) {
        boolean extraLines = Utils.getLoreLines(slot.getStack()).size() > 1;
        mc.interactionManager.clickSlot(
                syncId,
                slot.id,
                extraLines ? GLFW.GLFW_MOUSE_BUTTON_LEFT : GLFW.GLFW_MOUSE_BUTTON_3,
                extraLines ? SlotActionType.PICKUP : SlotActionType.CLONE,
                mc.player
        );
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && mc.currentScreen instanceof GenericContainerScreen container) {
            GenericContainerScreenHandler handler = container.getScreenHandler();
            for (Slot slot : Utils.getContainerSlots(handler)) {
                ButtonType type = getButtonType(slot.getStack());
                if (!type.equals(ButtonType.None) && getBoundKey(type).key() == event.key) {
                    if (event.action == GLFW.GLFW_PRESS) click(handler.syncId, slot);
                    event.cancel();
                    break;
                }
            }
        }
    }

    public enum ButtonType {
        Next,
        Previous,
        Up,
        Down,
        Back,
        None
    }
}
