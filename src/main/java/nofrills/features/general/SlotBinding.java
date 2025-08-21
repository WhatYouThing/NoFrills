package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.*;
import nofrills.events.InputEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class SlotBinding {
    public static final Feature instance = new Feature("slotBinding");

    public final static SettingKeybind keybind = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "keybind", instance.key());
    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance.key());
    public static final SettingBool lines = new SettingBool(false, "lines", instance.key());
    public static final SettingDouble lineWidth = new SettingDouble(2.0, "lineWidth", instance.key());
    public static final SettingBool borders = new SettingBool(false, "borders", instance.key());
    public static final SettingColor binding = new SettingColor(RenderColor.fromHex(0x00ff00), "bindingColor", instance.key());
    public static final SettingColor bound = new SettingColor(RenderColor.fromHex(0x00ffff), "boundColor", instance.key());

    public static int lastSlot = -1;

    private static void success(String message) {
        Utils.infoFormat("§a{}", message);
        Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0f, 1.0f);
    }

    private static void error(String message) {
        Utils.infoFormat("§4{}", message);
        Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.MASTER, 1.0f, 0.0f);
    }

    private static void alert(String message) {
        Utils.infoFormat("§e{}", message);
    }

    public static boolean isHotbar(int slotId) {
        return slotId >= 36 && slotId <= 43;
    }

    public static boolean isInventory(int slotId) {
        return slotId >= 9 && slotId <= 35;
    }

    public static boolean isArmor(int slotId) {
        return slotId >= 5 && slotId <= 8;
    }

    public static boolean isValid(int slotId) {
        return isHotbar(slotId) || isInventory(slotId) || isArmor(slotId);
    }

    public static boolean isBindValid(int slot1, int slot2) {
        return isHotbar(slot1) || isHotbar(slot2);
    }

    public static int toHotbarNumber(int slotId) {
        return slotId % 9 + 1;
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.currentScreen instanceof InventoryScreen inventory) {
            Slot focusedSlot = Utils.getFocusedSlot();
            if (event.key == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.action == GLFW.GLFW_PRESS && event.modifiers == GLFW.GLFW_MOD_SHIFT && focusedSlot != null) {
                int syncId = inventory.getScreenHandler().syncId;
                if (isHotbar(focusedSlot.id)) {
                    int hotbarNumber = toHotbarNumber(focusedSlot.id);
                    String hotbarName = "hotbar" + hotbarNumber;
                    if (data.value().has(hotbarName)) {
                        JsonObject object = data.value().get(hotbarName).getAsJsonObject();
                        JsonArray binds = object.get("binds").getAsJsonArray();
                        int last = object.get("last").getAsInt();
                        int first = !binds.isEmpty() ? binds.get(0).getAsInt() : 0;
                        if (last != 0 || first != 0) {
                            mc.interactionManager.clickSlot(syncId, last != 0 ? last : first, hotbarNumber - 1, SlotActionType.SWAP, mc.player);
                            event.cancel();
                        }
                    }
                } else if (isValid(focusedSlot.id)) {
                    for (int i = 1; i <= 8; i++) {
                        String name = "hotbar" + i;
                        if (data.value().has(name)) {
                            JsonArray array = data.value().get(name).getAsJsonObject().get("binds").getAsJsonArray();
                            for (JsonElement element : array.deepCopy()) {
                                if (element.getAsInt() == focusedSlot.id) {
                                    mc.interactionManager.clickSlot(syncId, focusedSlot.id, i - 1, SlotActionType.SWAP, mc.player);
                                    data.value().get(name).getAsJsonObject().addProperty("last", focusedSlot.id);
                                    event.cancel();
                                }
                            }
                        }
                    }
                }
            }
            if (keybind.value() == event.key) {
                if (event.action == GLFW.GLFW_PRESS && focusedSlot != null) {
                    lastSlot = focusedSlot.id;
                }
                if (event.action == GLFW.GLFW_RELEASE) {
                    if (focusedSlot != null && lastSlot == focusedSlot.id) {
                        if (isHotbar(focusedSlot.id)) {
                            String hotbarName = "hotbar" + toHotbarNumber(focusedSlot.id);
                            if (data.value().has(hotbarName)) {
                                data.value().get(hotbarName).getAsJsonObject().add("binds", new JsonArray());
                                data.value().get(hotbarName).getAsJsonObject().addProperty("last", 0);
                            }
                            success(Utils.format("Cleared every bind from hotbar slot {}.", toHotbarNumber(focusedSlot.id)));
                        } else if (isValid(focusedSlot.id)) {
                            for (int i = 1; i <= 8; i++) {
                                String name = "hotbar" + i;
                                if (data.value().has(name)) {
                                    JsonArray array = data.value().get(name).getAsJsonObject().get("binds").getAsJsonArray();
                                    for (JsonElement element : array.deepCopy()) {
                                        if (element.getAsInt() == focusedSlot.id) {
                                            array.remove(element);
                                            success(Utils.format("Successfully unbound slot from hotbar slot {}.", i));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (lastSlot != -1 && focusedSlot != null) {
                        if (isValid(lastSlot) && isValid(focusedSlot.id) && isBindValid(lastSlot, focusedSlot.id)) {
                            int hotbar = isHotbar(lastSlot) ? lastSlot : focusedSlot.id;
                            String hotbarName = "hotbar" + toHotbarNumber(hotbar);
                            int slot = isHotbar(lastSlot) ? focusedSlot.id : lastSlot;
                            for (int i = 1; i <= 8; i++) {
                                String name = "hotbar" + i;
                                if (data.value().has(name)) {
                                    JsonArray array = data.value().get(name).getAsJsonObject().get("binds").getAsJsonArray();
                                    for (JsonElement element : array.deepCopy()) {
                                        if (element.getAsInt() == slot) {
                                            array.remove(element);
                                            alert(Utils.format("The target is already bound to hotbar slot {}, replacing the bind.", i));
                                        }
                                    }
                                }
                            }
                            if (!data.value().has(hotbarName)) {
                                JsonObject object = new JsonObject();
                                object.addProperty("last", 0);
                                object.add("binds", new JsonArray());
                                data.value().add(hotbarName, object);
                            }
                            data.value().get(hotbarName).getAsJsonObject().get("binds").getAsJsonArray().add(slot);
                            success("Slots bound successfully!");
                        } else {
                            error("Invalid slot binding combination detected, doing nothing.");
                        }
                    }
                    lastSlot = -1;
                }
            }
        }
    }
}
