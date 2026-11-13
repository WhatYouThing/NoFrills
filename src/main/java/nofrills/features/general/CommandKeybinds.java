package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingJson;
import nofrills.config.SettingKeybind;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.hud.InnerSettingsScreen;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.ToggleButton;
import nofrills.misc.DungeonUtil;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static nofrills.Main.mc;

@EventListener
public final class CommandKeybinds {
    public static final Feature instance = new Feature("customKeybinds");

    public static final SettingBool allowAllInGui = new SettingBool(false, "allowInGui", instance.key());
    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance.key());

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        Settings.Toggle allowAllInGuiToggle = new Settings.Toggle("Allow All in GUI", allowAllInGui, "Allow all keybinds to work while any container GUI (inventory/chest/furnace/etc.) is open.");
        list.add(allowAllInGuiToggle);
        Settings.BigButton button = new Settings.BigButton("Add New Keybind", btn -> {
            data.edit(object -> {
                if (!object.has("binds")) {
                    object.add("binds", new JsonArray());
                }
                object.get("binds").getAsJsonArray().add(fillDefaults(new JsonObject()));
            });
            mc.gui.setScreen(buildSettings());
        });
        button.button.verticalSizing(Sizing.fixed(18));
        list.add(button);
        if (data.value().has("binds")) {
            JsonArray binds = data.value().get("binds").getAsJsonArray();
            for (int i = 0; i < binds.size(); i++) {
                list.add(new Setting(i));
            }
        }
        return list;
    }

    public static Settings buildSettings() {
        Settings settings = new Settings(getSettingsList());
        settings.setTitle(Component.literal("Command Keybinds"));
        return settings;
    }

    private static JsonObject fillDefaults(JsonObject object) {
        List<Pair<String, JsonPrimitive>> values = List.of(
                Pair.of("name", new JsonPrimitive("New Keybind")),
                Pair.of("command", new JsonPrimitive("")),
                Pair.of("enabled", new JsonPrimitive(true)),
                Pair.of("allowInGui", new JsonPrimitive(false)),
                Pair.of("modifier", new JsonPrimitive(Modifier.Any.name()))
        );
        if (object.has("key") && object.get("key").getAsJsonPrimitive().isNumber()) {
            object.addProperty("key", SettingKeybind.upgrade(object.get("key")).orElse(InputConstants.UNKNOWN).getName());
        } else if (!object.has("key")) {
            object.addProperty("key", InputConstants.UNKNOWN.getName());
        }
        if (object.has("classFilter")) {
            object.add("classFilterList",
                    Utils.toJsonArray(Arrays.stream(object.get("classFilter").getAsString().split(",")).map(String::trim).toList())
            );
            object.remove("classFilter");
        } else if (!object.has("classFilterList")) {
            object.add("classFilterList", new JsonArray());
        }
        if (object.has("islandFilter")) {
            object.add("islandFilterList",
                    Utils.toJsonArray(Arrays.stream(object.get("islandFilter").getAsString().split(",")).map(String::trim).toList())
            );
            object.remove("islandFilter");
        } else if (!object.has("islandFilterList")) {
            object.add("islandFilterList", new JsonArray());
        }
        for (Pair<String, JsonPrimitive> value : values) {
            if (!object.has(value.getKey())) {
                object.add(value.getKey(), value.getValue());
            }
        }
        return object;
    }

    private static boolean isValidScreen(boolean allowBindInGui) {
        if ((allowAllInGui.value() || allowBindInGui) && mc.gui.screen() instanceof AbstractContainerScreen<?>) {
            return !(mc.gui.screen() instanceof AnvilScreen);
        }
        return mc.gui.screen() == null;
    }

    private static Modifier getModifierType(String modifier) {
        for (Modifier value : Modifier.values()) {
            if (value.name().equals(modifier)) {
                return value;
            }
        }
        return Modifier.Any;
    }

    private static boolean matchesModifier(Modifier modifier, int modifiers) {
        boolean shift = (modifiers & InputConstants.MOD_SHIFT) != 0;
        boolean alt = (modifiers & InputConstants.MOD_ALT) != 0;
        boolean ctrl = (modifiers & InputConstants.MOD_CONTROL) != 0;
        return switch (modifier) {
            case Any -> true;
            case None -> !shift && !alt && !ctrl;
            case Shift -> shift && !alt && !ctrl;
            case Alt -> alt && !shift && !ctrl;
            case Ctrl -> ctrl && !shift && !alt;
        };
    }

    @EventHandler(priority = EventPriority.LOW)
    public static void onKey(InputEvent event) {
        if (instance.isActive() && data.value().has("binds")) {
            for (JsonElement entry : data.value().get("binds").getAsJsonArray()) {
                JsonObject bind = fillDefaults(entry.getAsJsonObject());
                if (!bind.get("enabled").getAsBoolean()) {
                    continue;
                }
                if (!isValidScreen(bind.get("allowInGui").getAsBoolean())) {
                    continue;
                }
                if (!event.isKey(bind.get("key").getAsString())) {
                    continue;
                }
                String modifier = bind.get("modifier").getAsString();
                if (!matchesModifier(getModifierType(modifier), event.modifiers)) {
                    continue;
                }
                String className = Utils.toLower(DungeonUtil.getPlayerClass());
                List<String> classList = bind.get("classFilterList").getAsJsonArray().asList().stream()
                        .map(e -> Utils.toLower(e.getAsString().trim()))
                        .toList();
                if (!classList.isEmpty() && !className.isEmpty() && !classList.contains(className)) {
                    continue;
                }
                String area = Utils.toLower(SkyblockData.getArea());
                List<String> islandList = bind.get("islandFilterList").getAsJsonArray().asList().stream()
                        .map(e -> Utils.toLower(e.getAsString().trim()))
                        .toList();
                if (!islandList.isEmpty() && !area.isEmpty() && !islandList.contains(area)) {
                    continue;
                }
                String command = bind.get("command").getAsString();
                if (!command.isEmpty()) {
                    event.consume(() -> Utils.sendMessage(command));
                    break;
                }
            }
        }
    }

    public enum Modifier {
        Any,
        None,
        Shift,
        Ctrl,
        Alt
    }

    public static final class Setting extends FlowLayout {
        public int index;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);

            this.index = index;
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);

            FlatTextbox input = new FlatTextbox(Sizing.fixed(140));
            input.margins(Insets.of(0, 0, 0, 6));
            input.text(this.getData().has("name") ? this.getData().get("name").getAsString() : "New Keybind");
            input.tooltip(Component.literal("The name of this command keybind."));
            input.onChanged().subscribe(value -> data.edit(obj -> this.getData(obj).addProperty("name", value)));
            ToggleButton mainToggle = new ToggleButton(this.getData().has("enabled") && this.getData().get("enabled").getAsBoolean());
            mainToggle.sizing(Sizing.fixed(50), Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 3));
            mainToggle.tooltip(Component.literal("The main toggle for this command keybind."));
            mainToggle.onToggled().subscribe(toggle -> data.edit(obj -> this.getData(obj).addProperty("enabled", toggle)));
            ButtonComponent editButton = UIComponents.button(Component.literal("Edit").withColor(0xffffff), button -> mc.gui.setScreen(this.buildKeybindSettings()));
            editButton.sizing(Sizing.fixed(48), Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 0));
            editButton.renderer(Settings.buttonRendererWhite);
            ButtonComponent delete = UIComponents.button(Component.literal("Delete").withColor(0xffffff), button -> {
                data.edit(object -> object.get("binds").getAsJsonArray().remove(this.index));
                mc.gui.setScreen(buildSettings());
            });
            delete.positioning(Positioning.relative(100, 50)).verticalSizing(Sizing.fixed(18));
            delete.renderer(Settings.buttonRendererWhite);

            this.child(input);
            this.child(mainToggle);
            this.child(editButton);
            this.child(delete);
        }

        public JsonObject getData(JsonObject object) {
            return fillDefaults(object.get("binds").getAsJsonArray().get(this.index).getAsJsonObject());
        }

        public JsonObject getData() {
            return this.getData(data.value());
        }

        public FlowLayout buildCommandInputSetting() {
            return new Settings.TextInput(
                    "Command",
                    this.getData().get("command").getAsString(),
                    "",
                    "The message/command that this keybind will send.",
                    s -> data.edit(obj -> this.getData(obj).addProperty("command", s))
            );
        }

        public FlowLayout buildAllowInGuiSetting() {
            return new Settings.Toggle(
                    "Allow in GUI",
                    this.getData().get("allowInGui").getAsBoolean(),
                    false,
                    "Allow this keybind to work while any container GUI (inventory/chest/furnace/etc.) is open.",
                    b -> data.edit(obj -> this.getData(obj).addProperty("allowInGui", b))
            );
        }

        public FlowLayout buildKeybindSetting() {
            return new Settings.Keybind(
                    "Keybind",
                    Utils.getInputKey(this.getData().get("key").getAsString()),
                    InputConstants.UNKNOWN,
                    "The key bound to this command.",
                    k -> data.edit(object -> this.getData(object).addProperty("key", k.getName()))
            );
        }

        public FlowLayout buildModifierSetting() {
            return new Settings.EnumToggle<>(
                    "Modifier Key",
                    getModifierType(this.getData().get("modifier").getAsString()),
                    Modifier.Any,
                    Modifier.class,
                    "The modifier key required to execute the keybind.\n\nAny: Executes regardless of modifier.\nNone: Executes only if no modifier (Shift, Alt, etc.) is held.\nShift: Executes only if Shift is held.\nCtrl: Executes only if Ctrl is held.\nAlt: Executes only if Alt is held.",
                    e -> data.edit(object -> this.getData(object).addProperty("modifier", e.name()))
            );
        }

        public FlowLayout buildClassFilterSetting() {
            return new Settings.TextEntryList(
                    "Class Filter",
                    () -> this.getData().get("classFilterList").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList(),
                    "Prevents this keybind from working if your selected dungeon class is not on this list.\nLeave empty to disable class filtering.",
                    l -> data.edit(obj -> this.getData(obj).add("classFilterList", Utils.toJsonArray(l))),
                    s -> DungeonUtil.getDungeonClasses().stream()
                            .filter(c -> Utils.toLower(c).startsWith(Utils.toLower(s)))
                            .sorted()
                            .toList()
            );
        }

        public FlowLayout buildIslandFilterSetting() {
            return new Settings.TextEntryList(
                    "Island Filter",
                    () -> this.getData().get("islandFilterList").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList(),
                    "Prevents this keybind from working if the current tablist area name is not on this list.\nLeave empty to disable island filtering.",
                    l -> data.edit(obj -> this.getData(obj).add("islandFilterList", Utils.toJsonArray(l))),
                    s -> SkyblockData.getKnownAreas().stream()
                            .filter(a -> Utils.toLower(a).startsWith(Utils.toLower(s)))
                            .sorted()
                            .toList()
            );
        }

        public InnerSettingsScreen buildKeybindSettings() {
            List<FlowLayout> list = new ArrayList<>();
            list.add(this.buildCommandInputSetting());
            list.add(this.buildAllowInGuiSetting());
            list.add(this.buildKeybindSetting());
            list.add(this.buildModifierSetting());
            list.add(this.buildClassFilterSetting());
            list.add(this.buildIslandFilterSetting());
            return new InnerSettingsScreen(
                    list,
                    Component.literal("Command Keybind: " + this.getData().get("name").getAsString()),
                    buildSettings()
            );
        }
    }
}
