package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import nofrills.config.*;
import nofrills.events.InputEvent;
import nofrills.events.ScreenRenderEvent;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class SlotBinding {
    public static final Feature instance = new Feature("slotBinding");

    public static final SettingKeybind keybind = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "keybind", instance.key());
    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance.key());
    public static final SettingBool lines = new SettingBool(false, "lines", instance.key());
    public static final SettingDouble lineWidth = new SettingDouble(2.0, "lineWidth", instance.key());
    public static final SettingBool borders = new SettingBool(false, "borders", instance.key());
    public static final SettingColor binding = new SettingColor(RenderColor.fromHex(0x00ff00), "bindingColor", instance.key());
    public static final SettingColor bound = new SettingColor(RenderColor.fromHex(0x00ffff), "boundColor", instance.key());

    public static int lastSlot = -1;

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        list.add(new Settings.Description("Using Binds", "Shift + Left click to swap items with the other bound slot."));
        list.add(new Settings.Description("Adding Binds", "Hover over a slot, press the keybind, move your cursor to another slot, and release the keybind."));
        list.add(new Settings.Description("Deleting Binds", "Press and release the keybind over a slot to clear every bound slot."));
        list.add(new Settings.Separator("Presets"));
        Settings.BigButton button = new Settings.BigButton("Save New Preset", btn -> {
            if (!data.value().has("presets")) {
                data.value().add("presets", new JsonArray());
            }
            data.edit(object -> {
                JsonObject preset = new JsonObject();
                preset.addProperty("name", "New preset");
                for (int i = 1; i <= 8; i++) {
                    String hotbarName = getHotbarName(i);
                    if (object.has(hotbarName)) {
                        preset.add(hotbarName, object.get(hotbarName).getAsJsonObject().deepCopy());
                    }
                }
                object.get("presets").getAsJsonArray().add(preset);
            });
            mc.setScreen(buildSettings());
        });
        button.button.tooltip(Text.literal("Saves your current slot binding configuration as a preset.\nCan be loaded at any time to quickly change your binds."));
        button.button.verticalSizing(Sizing.fixed(18));
        list.add(button);
        if (data.value().has("presets")) {
            JsonArray presets = data.value().get("presets").getAsJsonArray();
            for (int i = 0; i < presets.size(); i++) {
                list.add(new Setting(i));
            }
        }
        list.add(new Settings.Separator("Settings"));
        list.add(new Settings.Keybind("Keybind", SlotBinding.keybind, "The keybind used for creating and removing slot binding combinations."));
        list.add(new Settings.Toggle("Show Lines", SlotBinding.lines, "Draw lines between the slot you're hovering over, and any slots bound to it."));
        list.add(new Settings.Toggle("Show Borders", SlotBinding.borders, "Draw borders around any slot that is bound to the slot you're hovering over."));
        list.add(new Settings.SliderDouble("Line Width", 0.1, 5.0, 0.1, SlotBinding.lineWidth, "The width of the lines"));
        list.add(new Settings.ColorPicker("Binding Color", true, SlotBinding.binding, "The color used to display the bind you are currently creating."));
        list.add(new Settings.ColorPicker("Bound Color", true, SlotBinding.bound, "The color used to display existing slot binds."));
        return list;
    }

    public static Settings buildSettings() {
        Settings settings = new Settings(getSettingsList());
        settings.setTitle(Text.literal("Slot Binding"));
        return settings;
    }

    private static void sendSuccess(String message) {
        Utils.infoFormat("§a{}", message);
        Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    private static void sendError(String message) {
        Utils.infoFormat("§c{}", message);
        Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.MASTER, 1.0f, 0.0f);
    }

    private static void sendAlert(String message) {
        Utils.infoFormat("§e{}", message);
    }

    public static boolean isHotbar(int slotId) {
        return slotId >= 36 && slotId <= 43;
    }

    public static boolean isValid(int slotId) {
        return isHotbar(slotId) || (slotId >= 9 && slotId <= 35) || (slotId >= 5 && slotId <= 8);
    }

    public static boolean isBindValid(int slot1, int slot2) {
        return isHotbar(slot1) || isHotbar(slot2);
    }

    public static int toHotbarNumber(int slotId) {
        return slotId % 9 + 1;
    }

    public static boolean isBinding() {
        return instance.isActive() && lastSlot != -1;
    }

    public static String getHotbarName(int slot) {
        return Utils.format("hotbar{}", slot);
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.currentScreen instanceof InventoryScreen inventory) {
            Slot focusedSlot = Utils.getFocusedSlot();
            if (event.key == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.action == GLFW.GLFW_PRESS && event.modifiers == GLFW.GLFW_MOD_SHIFT && focusedSlot != null) {
                int syncId = inventory.getScreenHandler().syncId;
                if (isHotbar(focusedSlot.id)) {
                    int hotbarNumber = toHotbarNumber(focusedSlot.id);
                    String hotbarName = getHotbarName(hotbarNumber);
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
                        String name = getHotbarName(i);
                        if (data.value().has(name)) {
                            JsonArray array = data.value().get(name).getAsJsonObject().get("binds").getAsJsonArray();
                            if (array.contains(new JsonPrimitive(focusedSlot.id))) {
                                mc.interactionManager.clickSlot(syncId, focusedSlot.id, i - 1, SlotActionType.SWAP, mc.player);
                                data.edit(value -> value.get(name).getAsJsonObject().addProperty("last", focusedSlot.id));
                                event.cancel();
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
                            String hotbarName = getHotbarName(toHotbarNumber(focusedSlot.id));
                            if (data.value().has(hotbarName)) {
                                data.edit(value -> {
                                    value.get(hotbarName).getAsJsonObject().add("binds", new JsonArray());
                                    value.get(hotbarName).getAsJsonObject().addProperty("last", 0);
                                });
                            }
                            sendSuccess(Utils.format("Cleared every bind from hotbar slot {}.", toHotbarNumber(focusedSlot.id)));
                        } else if (isValid(focusedSlot.id)) {
                            for (int i = 1; i <= 8; i++) {
                                int slot = i;
                                String name = getHotbarName(slot);
                                if (data.value().has(name)) {
                                    data.edit(value -> {
                                        JsonArray array = value.get(name).getAsJsonObject().get("binds").getAsJsonArray();
                                        if (array.remove(new JsonPrimitive(focusedSlot.id))) {
                                            sendSuccess(Utils.format("Successfully unbound slot from hotbar slot {}.", slot));
                                        }
                                    });
                                }
                            }
                        }
                    } else if (lastSlot != -1 && focusedSlot != null) {
                        if (isValid(lastSlot) && isValid(focusedSlot.id) && isBindValid(lastSlot, focusedSlot.id)) {
                            int hotbar = isHotbar(lastSlot) ? lastSlot : focusedSlot.id;
                            String hotbarName = getHotbarName(toHotbarNumber(hotbar));
                            int slot = isHotbar(lastSlot) ? focusedSlot.id : lastSlot;
                            data.edit(value -> {
                                for (int i = 1; i <= 8; i++) {
                                    String name = getHotbarName(i);
                                    if (value.has(name)) {
                                        JsonArray array = value.get(name).getAsJsonObject().get("binds").getAsJsonArray();
                                        if (array.remove(new JsonPrimitive(slot))) {
                                            sendAlert(Utils.format("The target is already bound to hotbar slot {}, replacing the bind.", i));
                                            break;
                                        }
                                    }
                                }
                                if (!value.has(hotbarName)) {
                                    JsonObject object = new JsonObject();
                                    object.addProperty("last", 0);
                                    object.add("binds", new JsonArray());
                                    value.add(hotbarName, object);
                                }
                                value.get(hotbarName).getAsJsonObject().get("binds").getAsJsonArray().add(slot);
                                sendSuccess("Slots bound successfully!");
                            });
                        } else {
                            sendError("Invalid slot binding combination detected, doing nothing.");
                        }
                    }
                    lastSlot = -1;
                }
            }
        }
    }

    @EventHandler
    private static void onRender(ScreenRenderEvent.Before event) {
        if (instance.isActive() && mc.currentScreen instanceof InventoryScreen && event.focusedSlot != null) {
            if (isHotbar(event.focusedSlot.id)) {
                String name = getHotbarName(toHotbarNumber(event.focusedSlot.id));
                if (data.value().has(name)) {
                    for (JsonElement element : data.value().get(name).getAsJsonObject().get("binds").getAsJsonArray()) {
                        if (lines.value()) {
                            event.drawLine(event.focusedSlot.id, element.getAsInt(), lineWidth.value(), bound.value());
                        }
                        if (borders.value()) {
                            event.drawBorder(element.getAsInt(), bound.value());
                        }
                    }
                }
            } else if (isValid(event.focusedSlot.id)) {
                for (int i = 1; i <= 8; i++) {
                    String name = getHotbarName(i);
                    if (data.value().has(name)) {
                        for (JsonElement element : data.value().get(name).getAsJsonObject().get("binds").getAsJsonArray()) {
                            if (element.getAsInt() == event.focusedSlot.id) {
                                if (lines.value()) {
                                    event.drawLine(event.focusedSlot.id, i + 35, lineWidth.value(), bound.value());
                                }
                                if (borders.value()) {
                                    event.drawBorder(i + 35, bound.value());
                                }
                            }
                        }
                    }
                }
            }
            if (lastSlot != -1) {
                event.drawBorder(lastSlot, binding.value());
                event.drawBorder(event.focusedSlot.id, binding.value());
                event.drawLine(lastSlot, event.focusedSlot.id, lineWidth.value(), binding.value());
            }
        }
    }

    public static class Setting extends FlowLayout {
        public int index;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.index = index;
            FlatTextbox input = new FlatTextbox(Sizing.fixed(200));
            input.margins(Insets.of(0, 0, 0, 5));
            input.tooltip(Text.literal("The name of this slot binding preset."));
            input.text(this.getData(data.value()).get("name").getAsString());
            input.onChanged().subscribe(value -> data.edit(object -> this.getData(object).addProperty("name", value)));
            ButtonComponent loadButton = Components.button(Text.literal("Load").withColor(0xffffff), button -> {
                data.edit(object -> {
                    JsonObject preset = this.getData(object);
                    for (int i = 1; i <= 8; i++) {
                        String hotbarName = getHotbarName(i);
                        if (preset.has(hotbarName)) {
                            object.add(hotbarName, preset.get(hotbarName).getAsJsonObject().deepCopy());
                        } else {
                            object.remove(hotbarName);
                        }
                    }
                });
            });
            loadButton.horizontalSizing(Sizing.fixed(42)).verticalSizing(Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 0));
            loadButton.renderer(Settings.buttonRenderer);
            ButtonComponent deleteButton = Components.button(Text.literal("Delete").withColor(0xffffff), button -> {
                data.edit(object -> object.get("presets").getAsJsonArray().remove(this.index));
                mc.setScreen(buildSettings());
            });
            deleteButton.positioning(Positioning.relative(100, 0)).verticalSizing(Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 0));
            deleteButton.renderer(Settings.buttonRendererWhite);
            this.child(input);
            this.child(loadButton);
            this.child(deleteButton);
        }

        public JsonObject getData(JsonObject object) {
            return object.get("presets").getAsJsonArray().get(this.index).getAsJsonObject();
        }
    }
}
