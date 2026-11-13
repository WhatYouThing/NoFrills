package nofrills.features.general;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.config.*;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT;
import static com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT;
import static nofrills.Main.mc;

@EventListener
public class LoadoutKeybinds {
    public static final Feature instance = new Feature("loadoutKeybinds");

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);
    public static final SettingKeybind editBindKey = new SettingKeybind(InputConstants.UNKNOWN, "editBindKey", instance);
    public static final SettingBool playSound = new SettingBool(false, "playSound", instance);
    public static final SettingSound sound = new SettingSound("entity.horse.armor", 1.0, 1.0, "sound", instance);

    private static final Pattern loadoutsPattern = Pattern.compile("\\([0-9]*/[0-9]*\\) Loadouts");
    private static String binding = "";

    private static boolean isLoadoutsMenu(String title) {
        return loadoutsPattern.matcher(title).matches();
    }

    private static boolean isLoadoutButton(ItemStack stack) {
        List<String> lines = Utils.getLoreLines(stack);
        return lines.contains("Left-click to equip!") || lines.contains("Right-click to edit");
    }

    private static void updateBindText(Slot slot) {
        ItemStack stack = slot.getItem();
        String name = Utils.toPlain(stack.getHoverName());
        if (isLoadoutButton(stack)) {
            for (Map.Entry<String, JsonElement> entry : data.value().entrySet()) {
                if (entry.getValue().getAsString().equals(name)) {
                    String keyName = Utils.getInputKey(entry.getKey()).getDisplayName().getString();
                    SlotOptions.setCount(slot, keyName.contains(" ")
                            ? Arrays.stream(keyName.split(" ")).map(part -> part.substring(0, 1)).collect(Collectors.joining())
                            : keyName.substring(0, Math.min(keyName.length(), 2))
                    );
                    return;
                }
            }
        }
        SlotOptions.clearCount(slot);
    }

    private static void updateBindTextAll(AbstractContainerMenu menu) {
        for (Slot slot : Utils.getContainerSlots(menu)) {
            updateBindText(slot);
        }
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && mc.gui.screen() instanceof AbstractContainerScreen<?> screen && isLoadoutsMenu(screen.getTitle().getString())) {
            if (!binding.isEmpty()) {
                if (event.action == InputConstants.PRESS) {
                    if (event.isKey(editBindKey) || event.isMouse(MOUSE_BUTTON_LEFT) || event.isMouse(MOUSE_BUTTON_RIGHT)) {
                        Utils.infoRaw(Component.literal("Invalid key, not binding to loadout slot.").withStyle(ChatFormatting.RED));
                    } else {
                        data.edit(obj -> {
                            obj.entrySet().removeIf(entry -> entry.getValue().getAsString().equals(binding));
                            obj.addProperty(event.key.getName(), binding);
                        });
                        Utils.infoRaw(Component.literal("Successfully bound key to loadout slot: " + binding + ".").withStyle(ChatFormatting.GREEN));
                        Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 0.0f);
                        updateBindTextAll(screen.getMenu());
                    }
                    binding = "";
                }
                event.cancel();
            } else if (event.isKey(editBindKey)) {
                Slot focused = Utils.getFocusedSlot();
                if (focused == null) return;
                ItemStack stack = focused.getItem();
                event.consume(() -> {
                    if (isLoadoutButton(stack)) {
                        String name = Utils.toPlain(stack.getHoverName());
                        if (data.value().entrySet().stream().anyMatch(entry -> entry.getValue().getAsString().equals(name))) {
                            data.edit(obj -> obj.entrySet().removeIf(entry -> entry.getValue().getAsString().equals(name)));
                            Utils.infoRaw(Component.literal("Cleared bound key from loadout slot: " + name + ".").withStyle(ChatFormatting.GREEN));
                            Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 0.0f);
                            updateBindTextAll(screen.getMenu());
                        } else {
                            Utils.infoRaw(Component.literal("Press any key to bind to loadout slot: " + name + ".").withStyle(ChatFormatting.GRAY));
                            Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
                            binding = name;
                        }
                    }
                });
            } else if (data.value().has(event.key.getName())) {
                String name = data.value().get(event.key.getName()).getAsString();
                for (Slot slot : Utils.getContainerSlots(screen.getMenu())) {
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    if (name.equals(Utils.toPlain(stack.getHoverName()))) {
                        event.consume(() -> {
                            Utils.leftClick(screen.getMenu().containerId, slot.index);
                            if (playSound.value()) {
                                Utils.playSound(sound.sound().value(), sound.volume().valueFloat(), sound.pitch().valueFloat());
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onSlot(SlotUpdateEvent event) {
        if (instance.isActive() && event.slot != null && isLoadoutsMenu(event.title)) {
            updateBindText(event.slot);
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        binding = "";
    }
}
