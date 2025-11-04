package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingJson;
import nofrills.events.InputEvent;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.EnumButton;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.KeybindButton;
import nofrills.hud.clickgui.components.ToggleButton;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class CustomKeybinds {
    public static final Feature instance = new Feature("customKeybinds");

    public static final SettingBool allowInGui = new SettingBool(false, "allowInGui", instance.key());
    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance.key());

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        Settings.Toggle allowInGuiToggle = new Settings.Toggle("Allow in GUI", allowInGui, "Allow keybinds to work while any container GUI (inventory/chest/furnace/etc.) is open.");
        list.add(allowInGuiToggle);
        Settings.BigButton button = new Settings.BigButton("Add New Custom Keybind", btn -> {
            if (!data.value().has("binds")) {
                data.value().add("binds", new JsonArray());
            }
            JsonObject object = new JsonObject();
            object.addProperty("key", GLFW.GLFW_KEY_UNKNOWN);
            object.addProperty("command", "");
            object.addProperty("enabled", true);
            object.addProperty("modifier", Modifier.Any.name());
            data.value().get("binds").getAsJsonArray().add(object);
            mc.setScreen(buildSettings());
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
        settings.setTitle(Text.literal("Custom Keybinds"));
        return settings;
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
            case Shift -> GLFW.GLFW_MOD_SHIFT;
            case Alt -> GLFW.GLFW_MOD_ALT;
            case Ctrl -> GLFW.GLFW_MOD_CONTROL;
        };
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && ((allowInGui.value() && mc.currentScreen instanceof HandledScreen) || mc.currentScreen == null)) {
            if (data.value().has("binds")) {
                for (JsonElement entry : data.value().get("binds").getAsJsonArray()) {
                    JsonObject bind = entry.getAsJsonObject();
                    if (!bind.has("enabled") || !bind.get("enabled").getAsBoolean()) {
                        continue;
                    }
                    if (bind.has("modifier")) {
                        String modifier = bind.get("modifier").getAsString();
                        int required = getRequiredModifier(getModifierType(modifier));
                        if (required != -1 && event.modifiers != required) {
                            continue;
                        }
                    }
                    String command = bind.get("command").getAsString();
                    if (bind.get("key").getAsInt() == event.key && !command.isEmpty()) {
                        if (event.action == GLFW.GLFW_PRESS) {
                            Utils.sendMessage(command);
                        }
                        event.cancel();
                        return;
                    }
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

    public static class Setting extends FlowLayout {
        public int index;
        public FlatTextbox input;
        public FlowLayout options;
        public KeybindButton keybind;
        public EnumButton<Modifier> modifier;
        public ButtonComponent delete;
        public ToggleButton toggle;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.index = index;
            this.input = new FlatTextbox(Sizing.fixed(240));
            this.input.margins(Insets.of(0, 0, 0, 6));
            this.input.text(getData().get("command").getAsString());
            this.input.tooltip(Text.literal("The message/command that this keybind will send."));
            this.input.onChanged().subscribe(value -> getData().addProperty("command", value));
            this.options = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            this.keybind = new KeybindButton();
            this.keybind.verticalSizing(Sizing.fixed(18)).horizontalSizing(Sizing.fixed(100)).margins(Insets.of(3, 0, 1, 0));
            this.keybind.bind(getData().get("key").getAsInt());
            this.keybind.tooltip(Text.literal("The key bound to this command."));
            this.keybind.onBound().subscribe(key -> getData().addProperty("key", key));
            this.modifier = new EnumButton<>(getData().has("modifier") ? getData().get("modifier").getAsString() : "Any", Modifier.Any, Modifier.class);
            this.modifier.onChanged().subscribe(value -> getData().addProperty("modifier", value));
            this.modifier.margins(Insets.of(3, 0, 5, 0));
            this.modifier.sizing(Sizing.fixed(80), Sizing.fixed(18));
            this.modifier.tooltip(Text.literal("The modifier key required to execute the keybind.\n\nAny: Executes regardless of modifier.\nNone: Executes only if no modifier (Shift, Alt, etc.) is held.\nShift: Executes only if Shift is held.\nCtrl: Executes only if Ctrl is held.\nAlt: Executes only if Alt is held."));
            this.toggle = new ToggleButton(getData().has("enabled") && getData().get("enabled").getAsBoolean());
            this.toggle.onToggled().subscribe(value -> this.getData().addProperty("enabled", value));
            this.toggle.verticalSizing(Sizing.fixed(18)).horizontalSizing(Sizing.fixed(54));
            this.toggle.tooltip(Text.literal("The toggle for the keybind, allows you to disable the keybind without having to delete it."));
            this.toggle.margins(Insets.of(3, 0, 5, 0));
            this.delete = Components.button(Text.literal("Delete").withColor(0xffffff), button -> {
                data.value().get("binds").getAsJsonArray().remove(this.index);
                mc.setScreen(buildSettings());
            });
            this.delete.positioning(Positioning.relative(100, 50)).verticalSizing(Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 0));
            this.delete.renderer((context, btn, delta) -> {
                context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0xff101010);
                Rendering.drawBorder(context, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), 0xffffffff);
            });
            this.options.child(this.keybind);
            this.options.child(this.modifier);
            this.options.child(this.toggle);
            this.child(this.input);
            this.child(this.options);
            this.child(this.delete);
        }

        public JsonObject getData() {
            return data.value().get("binds").getAsJsonArray().get(this.index).getAsJsonObject();
        }
    }
}
