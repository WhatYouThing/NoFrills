package nofrills.features.keybinds;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Formatting;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

import static nofrills.Main.mc;

public class RecipeLookup {
    public static final KeyBinding bind = new KeyBinding("key.nofrills.recipeLookup", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.nofrills");

    @EventHandler
    public static void onKey(InputEvent event) {
        if (bind.matchesKey(event.key, 0) && event.action == GLFW.GLFW_PRESS) {
            if (mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof GenericContainerScreen) {
                Slot focused = Utils.getFocusedSlot();
                if (focused != null) {
                    ItemStack stack = focused.getStack();
                    String itemId = Utils.getSkyblockId(stack);
                    if (!itemId.isEmpty()) {
                        if (itemId.contains("GENERATOR")) {
                            int index = itemId.lastIndexOf("_");
                            Utils.sendMessage("/recipe " + itemId.substring(0, index));
                        } else if (itemId.equals("PET")) {
                            NbtCompound data = Utils.getCustomData(stack);
                            JsonObject petData = JsonParser.parseString(data.getString("petInfo").orElse("")).getAsJsonObject();
                            String petName = petData.get("type").getAsString().replaceAll("_", " ");
                            Utils.sendMessage("/recipe " + petName + " PET");
                        } else {
                            Utils.sendMessage("/recipe " + itemId);
                        }
                        event.cancel();
                    } else if (!stack.isEmpty() && mc.currentScreen.getTitle().getString().startsWith("Museum")) {
                        String entryName = Formatting.strip(stack.getName().getString());
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
