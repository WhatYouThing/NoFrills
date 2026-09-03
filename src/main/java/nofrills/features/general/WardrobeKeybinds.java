package nofrills.features.general;

import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.*;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;

import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

@EventListener
public class WardrobeKeybinds {
    public static final Feature instance = new Feature("wardrobeKeybinds");

    public static final SettingEnum<KeybindStyle> style = new SettingEnum<>(KeybindStyle.Simple, KeybindStyle.class, "style", instance.key());
    public static final SettingBool noUnequip = new SettingBool(false, "noUnequip", instance.key());
    public static final SettingBool playSound = new SettingBool(false, "sound", instance.key());
    public static final SettingString sound = new SettingString("entity.horse.armor", "soundIdentifier", instance);
    public static final SettingDouble volume = new SettingDouble(0.7, "volume", instance);
    public static final SettingDouble pitch = new SettingDouble(1.0, "pitch", instance);
    public static final SettingKeybind custom1 = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "custom1", instance.key());
    public static final SettingKeybind custom2 = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "custom2", instance.key());
    public static final SettingKeybind custom3 = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "custom3", instance.key());
    public static final SettingKeybind custom4 = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "custom4", instance.key());
    public static final SettingKeybind custom5 = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "custom5", instance.key());
    public static final SettingKeybind custom6 = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "custom6", instance.key());
    public static final SettingKeybind custom7 = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "custom7", instance.key());
    public static final SettingKeybind custom8 = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "custom8", instance.key());
    public static final SettingKeybind custom9 = new SettingKeybind(SettingKeybind.UNKNOWN_KEY, "custom9", instance.key());

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
            Items.DYE.lime(),
            Items.DYE.pink(),
            Items.DYE.gray()
    );
    private static final Pattern armorPattern = Pattern.compile("\\([0-9]*/[0-9]*\\) Armor Sets");
    private static final Pattern equipmentPattern = Pattern.compile("\\([0-9]*/[0-9]*\\) Equipment Sets");

    private static int getWardrobePage(String title) {
        if (armorPattern.matcher(title).matches() || equipmentPattern.matcher(title).matches()) {
            String page = title.substring(title.indexOf("(") + 1, title.indexOf("/"));
            return Utils.parseInt(page).orElse(-1);
        }
        return -1;
    }

    private static int getTargetSlot(InputEvent event, int page) {
        return switch (style.value()) {
            case Simple -> {
                if (event.key >= InputConstants.KEY_1 && event.key <= InputConstants.KEY_9) {
                    yield event.key - 48 + (page - 1) * 9;
                }
                yield -1;
            }
            case Hotbar -> {
                for (int i = 1; i <= 9; i++) {
                    KeyMapping binding = mc.options.keyHotbarSlots[i - 1]; // could crash if someone is doing some voodoo
                    if (Utils.matchesKey(binding, event.keyInput, event.mouseInput)) {
                        yield i + (page - 1) * 9;
                    }
                }
                yield -1;
            }
            case Custom -> {
                for (int i = 1; i <= 9; i++) {
                    SettingKeybind binding = customKeys.get(i - 1);
                    if (binding.value() == event.key) {
                        yield i + (page - 1) * 9;
                    }
                }
                yield -1;
            }
        };
    }

    private static boolean isEquipButton(Slot slot, int target) {
        ItemStack stack = slot.getItem();
        Item item = stack.getItem();
        String name = Utils.toPlain(stack.getHoverName());
        if (!stack.isEmpty() && target != -1 && name.startsWith(Utils.format("Slot {}:", target))) {
            if (noUnequip.value() && item.equals(Items.DYE.lime())) {
                return false;
            }
            return validButtons.stream().anyMatch(item::equals);
        }
        return false;
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && mc.gui.screen() instanceof AbstractContainerScreen<?> container) {
            int page = getWardrobePage(Utils.toPlain(container.getTitle()).trim());
            if (page == -1) return;
            int target = getTargetSlot(event, page);
            if (target != -1) {
                for (Slot slot : Utils.getContainerSlots(container.getMenu())) {
                    if (isEquipButton(slot, target)) {
                        if (event.action == InputConstants.PRESS) {
                            Utils.click(container.getMenu().containerId, slot.index, InputConstants.MOUSE_BUTTON_MIDDLE, ContainerInput.CLONE);
                            if (playSound.value()) {
                                Utils.playSound(sound.value(), volume.valueFloat(), pitch.valueFloat());
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
