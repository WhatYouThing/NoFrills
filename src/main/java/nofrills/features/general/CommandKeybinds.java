package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

import java.util.ArrayList;
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
                JsonObject obj = new JsonObject();
                obj.addProperty("name", "New Keybind");
                obj.addProperty("key", -1);
                obj.addProperty("command", "");
                obj.addProperty("enabled", true);
                obj.addProperty("allowInGui", false);
                obj.addProperty("modifier", Modifier.Any.name());
                obj.add("classFilterList", new JsonArray());
                obj.add("islandFilterList", new JsonArray());
                object.get("binds").getAsJsonArray().add(obj);
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

    private static int getRequiredModifier(Modifier modifier) {
        return switch (modifier) {
            case Any -> -1;
            case None -> 0;
            case Shift -> InputConstants.MOD_SHIFT;
            case Alt -> InputConstants.MOD_ALT;
            case Ctrl -> InputConstants.MOD_CONTROL;
        };
    }

    @EventHandler(priority = EventPriority.LOW)
    public static void onKey(InputEvent event) {
        if (instance.isActive() && data.value().has("binds")) {
            for (JsonElement entry : data.value().get("binds").getAsJsonArray()) {
                JsonObject bind = entry.getAsJsonObject();
                if (!bind.has("enabled") || !bind.get("enabled").getAsBoolean()) {
                    continue;
                }
                if (!isValidScreen(bind.has("allowInGui") && bind.get("allowInGui").getAsBoolean())) {
                    continue;
                }
                if (bind.get("key").getAsInt() != event.key) {
                    continue;
                }
                if (bind.has("modifier")) {
                    String modifier = bind.get("modifier").getAsString();
                    int required = getRequiredModifier(getModifierType(modifier));
                    if (required != -1 && event.modifiers != required) {
                        continue;
                    }
                }
                if (bind.has("classFilterList")) {
                    String className = Utils.toLower(DungeonUtil.getPlayerClass());
                    List<String> list = bind.get("classFilterList").getAsJsonArray().asList().stream()
                            .map(e -> Utils.toLower(e.getAsString().trim()))
                            .toList();
                    if (!className.isEmpty() && !list.contains(className)) {
                        continue;
                    }
                }
                if (bind.has("islandFilterList")) {
                    String area = Utils.toLower(SkyblockData.getArea());
                    List<String> list = bind.get("islandFilterList").getAsJsonArray().asList().stream()
                            .map(e -> Utils.toLower(e.getAsString().trim()))
                            .toList();
                    if (!area.isEmpty() && !list.contains(area)) {
                        continue;
                    }
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
            return object.get("binds").getAsJsonArray().get(this.index).getAsJsonObject();
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
                    this.getData().has("allowInGui") && this.getData().get("allowInGui").getAsBoolean(),
                    false,
                    "Allow this keybind to work while any container GUI (inventory/chest/furnace/etc.) is open.",
                    b -> data.edit(obj -> this.getData(obj).addProperty("allowInGui", b))
            );
        }

        public FlowLayout buildKeybindSetting() {
            return new Settings.Keybind(
                    "Keybind",
                    this.getData().get("key").getAsInt(),
                    SettingKeybind.UNKNOWN_KEY,
                    "The key bound to this command.",
                    k -> data.edit(object -> this.getData(object).addProperty("key", k))
            );
        }

        public FlowLayout buildModifierSetting() {
            return new Settings.EnumToggle<>(
                    "Modifier Key",
                    getModifierType(this.getData().has("modifier") ? this.getData().get("modifier").getAsString() : "Any"),
                    Modifier.Any,
                    Modifier.class,
                    "The modifier key required to execute the keybind.\n\nAny: Executes regardless of modifier.\nNone: Executes only if no modifier (Shift, Alt, etc.) is held.\nShift: Executes only if Shift is held.\nCtrl: Executes only if Ctrl is held.\nAlt: Executes only if Alt is held.",
                    e -> data.edit(object -> this.getData(object).addProperty("modifier", e.name()))
            );
        }

        public FlowLayout buildClassFilterSetting() {
            return new Settings.TextEntryList(
                    "Class Filter",
                    () -> this.getData().has("classFilterList")
                            ? this.getData().get("classFilterList").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList()
                            : DungeonUtil.getDungeonClasses().stream().toList(),
                    "Prevents this keybind from working if your selected dungeon class is not on this list.\nLeave empty to disable class filtering.",
                    l -> data.edit(obj -> this.getData(obj).add("classFilterList", Utils.toJsonArray(l)))
            );
        }

        public FlowLayout buildIslandFilterSetting() {
            return new Settings.TextEntryList(
                    "Island Filter",
                    () -> this.getData().has("islandFilterList")
                            ? this.getData().get("islandFilterList").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList()
                            : List.of(),
                    "Prevents this keybind from working if the current tablist area name is not on this list.\nLeave empty to disable island filtering.",
                    l -> data.edit(obj -> this.getData(obj).add("islandFilterList", Utils.toJsonArray(l)))
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
                    Component.literal("Command Keybind: " + (this.getData().has("name") ? this.getData().get("name").getAsString() : "New Keybind")),
                    buildSettings()
            );
        }
    }
}
