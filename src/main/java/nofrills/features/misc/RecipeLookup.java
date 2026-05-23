package nofrills.features.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import nofrills.config.Feature;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

import static nofrills.Main.mc;

public class RecipeLookup {
    public static final Feature instance = new Feature("recipeLookup");

    public static final SettingKeybind keybind = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "bind", instance.key());

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && keybind.value() == event.key && event.action == GLFW.GLFW_PRESS) {
            if (mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof GenericContainerScreen) {
                Slot focused = Utils.getFocusedSlot();
                if (focused != null) {
                    ItemStack stack = focused.getStack();
                    if (stack.isEmpty()) return;
                    String itemId = Utils.getSkyblockId(stack);
                    if (!itemId.isEmpty()) {
                        if (itemId.equals("PET")) {
                            NbtCompound data = Utils.getCustomData(stack);
                            if (!data.contains("petInfo")) return;
                            JsonObject petData = JsonParser.parseString(data.getString("petInfo").orElse("")).getAsJsonObject();
                            Utils.sendMessage("/recipe " + Utils.uppercaseFirst(Utils.toLower(petData.get("type").getAsString()), true));
                        } else {
                            Utils.sendMessage("/viewrecipe " + itemId);
                        }
                        event.cancel();
                    } else if (mc.currentScreen.getTitle().getString().startsWith("Museum")) {
                        String entryName = Utils.toPlain(stack.getName());
                        if (entryName.endsWith("Armor") || entryName.endsWith("Set") || entryName.endsWith("Equipment")) {
                            String[] words = entryName.split(" ");
                            entryName = String.join(" ", Arrays.copyOf(words, words.length - 1));
                        }
                        Utils.sendMessage("/recipe " + entryName);
                        event.cancel();
                    }
                }
            }
        }
    }
}
