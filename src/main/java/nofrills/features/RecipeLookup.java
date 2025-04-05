package nofrills.features;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Formatting;
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
                if (focused != null) {
                    ItemStack stack = focused.getStack();
                    String itemId = Utils.getSkyblockId(stack);
                    if (!itemId.isEmpty()) {
                        if (itemId.contains("GENERATOR")) {
                            int index = itemId.lastIndexOf("_");
                            Utils.sendMessage("/recipe " + itemId.substring(0, index));
                        } else if (itemId.equals("PET")) {
                            NbtCompound data = Utils.getCustomData(stack);
                            JsonObject petData = JsonParser.parseString(data.getString("petInfo")).getAsJsonObject();
                            String petName = petData.get("type").getAsString().replaceAll("_", " ");
                            Utils.sendMessage("/recipe " + petName + " PET");
                        } else {
                            Utils.sendMessage("/recipe " + itemId);
                        }
                        event.cancel();
                    } else if (!stack.isEmpty() && mc.currentScreen.getTitle().getString().startsWith("Museum")) {
                        Utils.sendMessage("/recipe " + Formatting.strip(stack.getName().getString()));
                        event.cancel();
                    }
                }
            }
        }
    }
}
