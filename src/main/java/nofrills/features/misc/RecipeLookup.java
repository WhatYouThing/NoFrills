package nofrills.features.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingEnum;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

import static nofrills.Main.mc;

public class RecipeLookup {
    public static final Feature instance = new Feature("recipeLookup");

    public static final SettingEnum<Mode> mode = new SettingEnum<>(Mode.Recipe, Mode.class, "mode", instance);
    public static final SettingKeybind keybind = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "bind", instance.key());

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && keybind.value() == event.key && event.action == GLFW.GLFW_PRESS) {
            if (mc.screen instanceof InventoryScreen || mc.screen instanceof ContainerScreen) {
                Slot focused = Utils.getFocusedSlot();
                if (focused != null) {
                    ItemStack stack = focused.getItem();
                    if (stack.isEmpty()) return;
                    String itemId = Utils.getSkyblockId(stack);
                    if (!itemId.isEmpty()) {
                        if (itemId.equals("PET")) {
                            CompoundTag data = Utils.getCustomData(stack);
                            if (!data.contains("petInfo")) return;
                            JsonObject petData = JsonParser.parseString(data.getString("petInfo").orElse("")).getAsJsonObject();
                            Utils.sendMessage("/recipe " + Utils.uppercaseFirst(Utils.toLower(petData.get("type").getAsString()), true));
                        } else {
                            String command;
                            if (mode.value() == Mode.Viewrecipe) {
                                command = "/viewrecipe ";
                            } else {
                                command = "/recipe ";
                            }
                            Utils.sendMessage(command + itemId);
                        }
                        event.cancel();
                    } else if (mc.screen.getTitle().getString().startsWith("Museum")) {
                        String entryName = Utils.toPlain(stack.getHoverName());
                        if (entryName.endsWith("Armor") || entryName.endsWith("Set") || entryName.endsWith("Equipment")) {
                            String[] words = entryName.split(" ");
                            entryName = String.join(" ", Arrays.copyOf(words, words.length - 1));
                        }
                        Utils.sendMessage("/recipe " + entryName);
                        event.cancel();
                    } else if (stack.getCustomName().getString().equals("Accept Offer")) {
                        // will only give recipe of the first item (in case of multi-item requests like Bartender)
                        // might be worth revisiting later with a stateful approach
                        String target = Utils.getLoreLines(stack).stream()
                                .dropWhile(s -> !s.contains("Items Required:"))
                                .skip(1)
                                .findFirst()
                                .orElse("");
                        if (!target.isEmpty()) {
                            int index = target.lastIndexOf(" x");
                            target = index > 0 ? target.substring(0, index) : target;
                            Utils.sendMessage("/recipe " + target);
                        }
                    }
                }
            }
        }
    }

    public enum Mode {
        Recipe,
        Viewrecipe
    }
}
