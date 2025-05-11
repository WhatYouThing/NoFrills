package nofrills.features;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Config;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class SlotBinding {
    private static int lastSlot = -1;

    private static void success(String message) {
        Utils.infoFormat("§a{}", message);
        Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0f, 1.0f);
    }

    private static void error(String message) {
        Utils.infoFormat("§c{}", message);
        Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.MASTER, 1.0f, 0.0f);
    }

    private static void alert(String message) {
        Utils.infoFormat("§e{}", message);
    }

    private static boolean isHotbar(int slotId) {
        return slotId >= 36 && slotId <= 43;
    }

    private static boolean isInventory(int slotId) {
        return slotId >= 9 && slotId <= 35;
    }

    private static boolean isArmor(int slotId) {
        return slotId >= 5 && slotId <= 8;
    }

    private static boolean isValid(int slotId) {
        return isHotbar(slotId) || isInventory(slotId) || isArmor(slotId);
    }

    private static boolean isBindValid(int slot1, int slot2) {
        return isHotbar(slot1) || isHotbar(slot2);
    }

    public static int toHotbarNumber(int slotId) {
        return slotId % 9 + 1;
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (Utils.Keybinds.bindSlots.matchesKey(event.key, 0) && mc.currentScreen instanceof InventoryScreen) {
            Slot focusedSlot = Utils.getFocusedSlot();
            if (event.action == GLFW.GLFW_PRESS && focusedSlot != null && isValid(focusedSlot.id)) {
                lastSlot = focusedSlot.id;
            } else if (event.action == GLFW.GLFW_RELEASE) {
                if (lastSlot != -1 && focusedSlot != null && isValid(focusedSlot.id)) {
                    if (isBindValid(lastSlot, focusedSlot.id)) {
                        int hotbar = isHotbar(lastSlot) ? lastSlot : focusedSlot.id;
                        String hotbarName = "hotbar" + toHotbarNumber(hotbar);
                        int slot = isHotbar(lastSlot) ? focusedSlot.id : lastSlot;
                        for (int i = 1; i <= 8; i++) {
                            String name = "hotbar" + i;
                            if (Config.slotBindData.has(name)) {
                                JsonArray array = Config.slotBindData.get(name).getAsJsonObject().get("binds").getAsJsonArray();
                                for (JsonElement element : array.deepCopy()) {
                                    if (element.getAsInt() == slot) {
                                        array.remove(element);
                                        alert(Utils.format("The target slot is already bound to hotbar slot {}, replacing with the new bind.", i));
                                    }
                                }
                            }
                        }
                        if (!Config.slotBindData.has(hotbarName)) {
                            JsonObject object = new JsonObject();
                            object.addProperty("last", 0);
                            object.add("binds", new JsonArray());
                            Config.slotBindData.add(hotbarName, object);
                        }
                        Config.slotBindData.get(hotbarName).getAsJsonObject().get("binds").getAsJsonArray().add(slot);
                        Config.configHandler.save();
                        success("Slots bound successfully!");
                    } else {
                        error("Impossible slot binding combination detected, doing nothing. Combinations with no hotbar slot are impossible, as the items cannot be swapped with a single input.");
                    }
                } else {
                    error("Invalid slot binding combination, doing nothing.");
                }
                lastSlot = -1;
            }
        }
    }
}
