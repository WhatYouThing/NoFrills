package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
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
import nofrills.compat.LegacyInput;

import static nofrills.Main.mc;

@EventListener
public class GuiKeybinds {
    public static final Feature instance = new Feature("guiKeybinds");

    public static final SettingKeybind next = new SettingKeybind(LegacyInput.KEY_UNKNOWN, "next", instance.key());
    public static final SettingKeybind previous = new SettingKeybind(LegacyInput.KEY_UNKNOWN, "previous", instance.key());
    public static final SettingKeybind up = new SettingKeybind(LegacyInput.KEY_UNKNOWN, "up", instance.key());
    public static final SettingKeybind down = new SettingKeybind(LegacyInput.KEY_UNKNOWN, "down", instance.key());
    public static final SettingKeybind back = new SettingKeybind(LegacyInput.KEY_UNKNOWN, "back", instance.key());

    private static ButtonType getButtonType(ItemStack stack) {
        if (!stack.isEmpty() && Utils.getSkyblockId(stack).isEmpty()) {
            Item item = stack.getItem();
            if (item.equals(Items.ARROW) || item.equals(Items.PLAYER_HEAD)) {
                String name = Utils.toPlain(stack.getHoverName());
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
        boolean extraLines = Utils.getLoreLines(slot.getItem()).size() > 1;
        mc.gameMode.handleContainerInput(
                syncId,
                slot.index,
                extraLines ? LegacyInput.MOUSE_BUTTON_LEFT : LegacyInput.MOUSE_BUTTON_3,
                extraLines ? ContainerInput.PICKUP : ContainerInput.CLONE,
                mc.player
        );
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && mc.gui.screen() instanceof ContainerScreen container) {
            ChestMenu handler = container.getMenu();
            for (Slot slot : Utils.getContainerSlots(handler)) {
                ButtonType type = getButtonType(slot.getItem());
                if (!type.equals(ButtonType.None) && getBoundKey(type).key() == event.key) {
                    if (event.action == LegacyInput.PRESS) click(handler.containerId, slot);
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
