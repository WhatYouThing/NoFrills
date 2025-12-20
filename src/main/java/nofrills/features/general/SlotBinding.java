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
import java.util.Optional;

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

    private static final BoundSlot emptySlot = new BoundSlot(-1);
    public static BoundSlot lastSlot = emptySlot;

    public static boolean isBinding() {
        return instance.isActive() && lastSlot.isValid();
    }

    public static List<BoundSlot> getHotbarSlots() {
        List<BoundSlot> list = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            BoundSlot slot = new BoundSlot(i);
            if (slot.hasData()) {
                list.add(slot);
            }
        }
        return list;
    }

    public static void savePreset(String name) {
        if (!data.value().has("presets")) {
            data.value().add("presets", new JsonArray());
        }
        data.edit(object -> {
            JsonObject preset = new JsonObject();
            preset.addProperty("name", name);
            for (BoundSlot slot : getHotbarSlots()) {
                preset.add(slot.getName(), object.get(slot.getName()).getAsJsonObject().deepCopy());
            }
            object.get("presets").getAsJsonArray().add(preset);
        });
    }

    public static void loadPreset(JsonObject preset) {
        data.edit(object -> {
            for (int i = 1; i <= 8; i++) {
                BoundSlot slot = new BoundSlot(i);
                if (preset.has(slot.getName())) {
                    object.add(slot.getName(), preset.get(slot.getName()).getAsJsonObject().deepCopy());
                } else {
                    object.remove(slot.getName());
                }
            }
        });
    }

    public static Optional<JsonObject> findPresetByName(String name) {
        if (data.value().has("presets")) {
            for (JsonElement element : data.value().get("presets").getAsJsonArray()) {
                JsonObject preset = element.getAsJsonObject();
                if (name.equalsIgnoreCase(preset.get("name").getAsString())) {
                    return Optional.of(preset);
                }
            }
        }
        return Optional.empty();
    }

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        list.add(new Settings.Description("Using Binds", "Shift + Left click to swap items with the other bound slot."));
        list.add(new Settings.Description("Adding Binds", "Hover over a slot, press the keybind, move your cursor to another slot, and release the keybind."));
        list.add(new Settings.Description("Deleting Binds", "Press and release the keybind over a slot to clear every bound slot."));
        list.add(new Settings.Separator("Presets"));
        Settings.BigButton button = new Settings.BigButton("Save New Preset", btn -> {
            savePreset("New preset");
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

    private static void sendError() {
        Utils.info("§cInvalid slot binding combination detected, doing nothing.");
        Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.MASTER, 1.0f, 0.0f);
    }

    private static void sendAlert(String message) {
        Utils.infoFormat("§e{}", message);
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.currentScreen instanceof InventoryScreen inventory) {
            BoundSlot focused = new BoundSlot(Utils.getFocusedSlot());
            if (event.key == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.action == GLFW.GLFW_PRESS && event.modifiers == GLFW.GLFW_MOD_SHIFT && focused.isValid()) {
                int syncId = inventory.getScreenHandler().syncId;
                if (focused.isHotbar() && focused.hasData()) {
                    JsonObject object = data.value().get(focused.getName()).getAsJsonObject();
                    JsonArray binds = object.get("binds").getAsJsonArray();
                    int last = object.get("last").getAsInt();
                    int first = !binds.isEmpty() ? binds.get(0).getAsInt() : 0;
                    if (last != 0 || first != 0) {
                        mc.interactionManager.clickSlot(syncId, last != 0 ? last : first, focused.toHotbar() - 1, SlotActionType.SWAP, mc.player);
                        event.cancel();
                    }
                } else {
                    for (BoundSlot slot : getHotbarSlots()) {
                        JsonArray array = data.value().get(slot.getName()).getAsJsonObject().get("binds").getAsJsonArray();
                        if (array.contains(new JsonPrimitive(focused.id))) {
                            mc.interactionManager.clickSlot(syncId, focused.id, slot.toHotbar() - 1, SlotActionType.SWAP, mc.player);
                            data.edit(value -> value.get(slot.getName()).getAsJsonObject().addProperty("last", focused.id));
                            event.cancel();
                        }
                    }
                }
            }
            if (keybind.value() == event.key) {
                if (event.action == GLFW.GLFW_PRESS && focused.isValid()) {
                    lastSlot = focused;
                }
                if (event.action == GLFW.GLFW_RELEASE) {
                    if (focused.isValid() && lastSlot.equals(focused)) {
                        if (focused.isHotbar() && focused.hasData()) {
                            data.edit(value -> {
                                value.get(focused.getName()).getAsJsonObject().add("binds", new JsonArray());
                                value.get(focused.getName()).getAsJsonObject().addProperty("last", 0);
                            });
                            sendSuccess(Utils.format("Cleared every bind from hotbar slot {}.", focused.toHotbar()));
                        } else {
                            for (BoundSlot slot : getHotbarSlots()) {
                                data.edit(value -> {
                                    JsonArray array = value.get(slot.getName()).getAsJsonObject().get("binds").getAsJsonArray();
                                    if (array.remove(new JsonPrimitive(focused.id))) {
                                        sendSuccess(Utils.format("Successfully unbound slot from hotbar slot {}.", slot.toHotbar()));
                                    }
                                });
                            }
                        }
                    } else if (lastSlot.isValid() && focused.canBindTo(lastSlot.id)) {
                        BoundSlot hotbar = lastSlot.isHotbar() ? lastSlot : focused;
                        BoundSlot slot = lastSlot.isHotbar() ? focused : lastSlot;
                        data.edit(value -> {
                            for (BoundSlot hotbarSlot : getHotbarSlots()) {
                                JsonArray array = value.get(hotbarSlot.getName()).getAsJsonObject().get("binds").getAsJsonArray();
                                if (array.remove(new JsonPrimitive(slot.id))) {
                                    sendAlert(Utils.format("The target is already bound to hotbar slot {}, replacing the bind.", slot.toHotbar()));
                                    break;
                                }
                            }
                            if (!value.has(hotbar.getName())) {
                                JsonObject object = new JsonObject();
                                object.addProperty("last", 0);
                                object.add("binds", new JsonArray());
                                value.add(hotbar.getName(), object);
                            }
                            value.get(hotbar.getName()).getAsJsonObject().get("binds").getAsJsonArray().add(slot.id);
                            sendSuccess("Slots bound successfully!");
                        });
                    } else {
                        sendError();
                    }
                    lastSlot = emptySlot;
                }
            }
        }
    }

    @EventHandler
    private static void onRender(ScreenRenderEvent.Before event) {
        if (instance.isActive() && mc.currentScreen instanceof InventoryScreen && event.focusedSlot != null) {
            BoundSlot focused = new BoundSlot(event.focusedSlot);
            if (focused.isHotbar() && focused.hasData()) {
                for (JsonElement element : data.value().get(focused.getName()).getAsJsonObject().get("binds").getAsJsonArray()) {
                    if (lines.value()) {
                        event.drawLine(focused.id, element.getAsInt(), lineWidth.value(), bound.value());
                    }
                    if (borders.value()) {
                        event.drawBorder(element.getAsInt(), bound.value());
                    }
                }
            } else if (focused.isValid()) {
                for (BoundSlot slot : getHotbarSlots()) {
                    for (JsonElement element : data.value().get(slot.getName()).getAsJsonObject().get("binds").getAsJsonArray()) {
                        if (element.getAsInt() == focused.id) {
                            if (lines.value()) {
                                event.drawLine(focused.id, slot.toHotbar() + 35, lineWidth.value(), bound.value());
                            }
                            if (borders.value()) {
                                event.drawBorder(slot.toHotbar() + 35, bound.value());
                            }
                        }
                    }
                }
            }
            if (lastSlot.isValid()) {
                event.drawBorder(lastSlot.id, binding.value());
                event.drawBorder(focused.id, binding.value());
                event.drawLine(lastSlot.id, event.focusedSlot.id, lineWidth.value(), binding.value());
            }
        }
    }

    public static class BoundSlot {
        public int id;

        public BoundSlot(int id) {
            this.id = id;
        }

        public BoundSlot(Slot slot) {
            this(slot != null ? slot.id : -1);
        }

        public boolean isHotbar(int id) {
            return id >= 36 && id <= 43;
        }

        public boolean isHotbar() {
            return this.isHotbar(this.id);
        }

        public boolean isInventory() {
            return this.id >= 9 && this.id <= 35;
        }

        public boolean isArmor() {
            return this.id >= 5 && this.id <= 8;
        }

        public boolean isValid() {
            return this.isHotbar() || this.isInventory() || this.isArmor();
        }

        public boolean canBindTo(int other) {
            return this.isHotbar() || this.isHotbar(other);
        }

        public int toHotbar() {
            return this.id > 9 ? this.id % 9 + 1 : this.id;
        }

        public String getName() {
            return "hotbar" + this.toHotbar();
        }

        public boolean hasData() {
            return data.value().has(this.getName());
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof BoundSlot boundSlot && this.id == boundSlot.id;
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
            ButtonComponent loadButton = Components.button(Text.literal("Load").withColor(0xffffff), button -> loadPreset(this.getData(data.value())));
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
