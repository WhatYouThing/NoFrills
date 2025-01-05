package nofrills.features;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import nofrills.mixin.HandledScreenAccessor;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class RecipeLookup {
    @EventHandler
    public static void onKey(InputEvent event) {
        if (Utils.Keybinds.recipeLookup.matchesKey(event.key, 0) && event.action == GLFW.GLFW_PRESS) {
            if (mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof GenericContainerScreen) {
                Slot focused = ((HandledScreenAccessor) mc.currentScreen).getFocusedSlot();
                if (focused == null) {
                    return;
                }
                ItemStack item = focused.getStack();
                NbtComponent component = item.get(DataComponentTypes.CUSTOM_DATA);
                if (!item.isEmpty() && component != null) {
                    NbtCompound data = component.copyNbt();
                    String itemId = data.getString("id");
                    if (!itemId.isEmpty()) {
                        if (itemId.contains("GENERATOR")) {
                            int index = itemId.lastIndexOf("_");
                            String id = itemId.substring(0, index);
                            Utils.info(Utils.Symbols.format + "7Looking up recipes for " + Utils.Symbols.format + "a" + id.replace("GENERATOR", "MINION").replaceAll("_", " ") + ".");
                            Utils.sendMessage("/recipe " + id);
                        } else if (itemId.equals("PET")) {
                            JsonObject petData = JsonParser.parseString(data.getString("petInfo")).getAsJsonObject();
                            String petName = petData.get("type").getAsString().replaceAll("_", " ");
                            Utils.info(Utils.Symbols.format + "7Looking up recipes for " + Utils.Symbols.format + "a" + petName + " PET.");
                            Utils.sendMessage("/recipe " + petName + " PET");
                        } else {
                            Utils.info(Utils.Symbols.format + "7Looking up recipes for " + Utils.Symbols.format + "a" + itemId.replaceAll("_", " ") + ".");
                            Utils.sendMessage("/recipe " + itemId);
                        }
                        event.cancel();
                    }
                }
            }
        }
    }
}
