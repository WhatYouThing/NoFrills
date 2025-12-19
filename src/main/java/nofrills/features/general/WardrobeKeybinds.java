package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingEnum;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static nofrills.Main.mc;

public class WardrobeKeybinds {
    public static final Feature instance = new Feature("wardrobeKeybinds");

    public static final SettingEnum<KeybindStyle> style = new SettingEnum<>(KeybindStyle.Simple, KeybindStyle.class, "style", instance.key());
    public static final SettingBool noUnequip = new SettingBool(false, "noUnequip", instance.key());
    public static final SettingBool sound = new SettingBool(false, "sound", instance.key());
    public static final SettingKeybind custom1 = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "custom1", instance.key());
    public static final SettingKeybind custom2 = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "custom2", instance.key());
    public static final SettingKeybind custom3 = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "custom3", instance.key());
    public static final SettingKeybind custom4 = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "custom4", instance.key());
    public static final SettingKeybind custom5 = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "custom5", instance.key());
    public static final SettingKeybind custom6 = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "custom6", instance.key());
    public static final SettingKeybind custom7 = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "custom7", instance.key());
    public static final SettingKeybind custom8 = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "custom8", instance.key());
    public static final SettingKeybind custom9 = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "custom9", instance.key());

    private static final List<SettingKeybind> customKeys = List.of(
            custom1,
            custom2,
            custom3,
            custom4,
            custom5,
            custom6,
            custom7,
            custom8,
            custom9
    );
    private static final List<Item> validButtons = List.of(
            Items.LIME_DYE,
            Items.PINK_DYE,
            Items.GRAY_DYE
    );

    private static int getWardrobePage(String title) {
        if (title.startsWith("Wardrobe (") && title.endsWith(")")) {
            return Utils.parseInt(title.replace("Wardrobe (", "").split("/")[0]).orElse(-1);
        }
        return -1;
    }

    private static int getTargetSlot(int key, int page) {
        return switch (style.value()) {
            case Simple -> {
                if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
                    yield key - 48 + (page - 1) * 9;
                }
                yield -1;
            }
            case Hotbar -> {
                for (int i = 1; i <= 9; i++) {
                    KeyBinding binding = mc.options.hotbarKeys[i - 1]; // could crash if someone is doing some voodoo
                    if (Utils.matchesKey(binding, key)) {
                        yield i + (page - 1) * 9;
                    }
                }
                yield -1;
            }
            case Custom -> {
                for (int i = 1; i <= 9; i++) {
                    SettingKeybind binding = customKeys.get(i - 1);
                    if (binding.value() == key) {
                        yield i + (page - 1) * 9;
                    }
                }
                yield -1;
            }
        };
    }

    private static boolean isEquipButton(Slot slot, int target) {
        ItemStack stack = slot.getStack();
        Item item = stack.getItem();
        String name = Utils.toPlain(stack.getName());
        if (!stack.isEmpty() && target != -1 && name.startsWith(Utils.format("Slot {}:", target))) {
            if (noUnequip.value() && item.equals(Items.LIME_DYE)) {
                return false;
            }
            return validButtons.stream().anyMatch(item::equals);
        }
        return false;
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && mc.currentScreen instanceof GenericContainerScreen container) {
            int page = getWardrobePage(container.getTitle().getString());
            if (page == -1) return;
            int target = getTargetSlot(event.key, page);
            if (target != -1) {
                for (Slot slot : Utils.getContainerSlots(container.getScreenHandler())) {
                    if (isEquipButton(slot, target)) {
                        if (event.action == GLFW.GLFW_PRESS) {
                            mc.interactionManager.clickSlot(container.getScreenHandler().syncId, slot.id, GLFW.GLFW_MOUSE_BUTTON_LEFT, SlotActionType.PICKUP, mc.player);
                            if (sound.value()) {
                                Utils.playSound(SoundEvents.ENTITY_HORSE_ARMOR, SoundCategory.MASTER, 0.69f, 1.0f);
                            }
                        }
                        break;
                    }
                }
                event.cancel();
            }
        }
    }

    public enum KeybindStyle {
        Simple,
        Hotbar,
        Custom
    }
}
