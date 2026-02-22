package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingJson;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static nofrills.Main.mc;

public class CommandShortcuts {
    public static final Feature instance = new Feature("commandShortcuts");

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        if (instance.isActive() && data.value().has("shortcuts")) {
            for (JsonElement element : data.value().get("shortcuts").getAsJsonArray()) {
                JsonObject shortcut = element.getAsJsonObject();
                String shortcutName = shortcut.get("shortcut").getAsString();
                if (shortcutName.isEmpty()) {
                    continue;
                }
                String name = shortcutName.startsWith("/") ? shortcutName.substring(1) : shortcutName;
                LiteralArgumentBuilder<FabricClientCommandSource> command = literal(name).executes(context -> {
                    Utils.sendMessage(shortcut.get("message").getAsString());
                    return SINGLE_SUCCESS;
                });
                dispatcher.register(command);
            }
        }
    }

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        Settings.BigButton button = new Settings.BigButton("Add New Shortcut", btn -> {
            data.edit(object -> {
                if (!object.has("shortcuts")) {
                    object.add("shortcuts", new JsonArray());
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("shortcut", "");
                obj.addProperty("message", "");
                object.get("shortcuts").getAsJsonArray().add(obj);
            });
            mc.setScreen(buildSettings());
        });
        button.button.verticalSizing(Sizing.fixed(18));
        list.add(button);
        if (data.value().has("shortcuts")) {
            JsonArray shortcuts = data.value().get("shortcuts").getAsJsonArray();
            for (int i = 0; i < shortcuts.size(); i++) {
                list.add(new Setting(i));
            }
        }
        return list;
    }

    public static Settings buildSettings() {
        Settings settings = new Settings(getSettingsList());
        settings.setTitle(Text.literal("Command Shortcuts"));
        return settings;
    }

    public static class Setting extends FlowLayout {
        public int index;
        public FlowLayout options;
        public FlatTextbox shortcutInput;
        public FlatTextbox messageInput;
        public ButtonComponent delete;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);

            this.index = index;
            this.options = Containers.horizontalFlow(Sizing.content(), Sizing.content());

            this.shortcutInput = new FlatTextbox(Sizing.fixed(118));
            this.shortcutInput.margins(Insets.of(0, 0, 0, 5));
            this.shortcutInput.text(this.getData(data.value()).get("shortcut").getAsString());
            this.shortcutInput.tooltip(Text.literal("The command name of this shortcut. Example: /dn"));
            this.shortcutInput.onChanged().subscribe(value -> data.edit(object -> this.getData(object).addProperty("shortcut", value)));

            this.messageInput = new FlatTextbox(Sizing.fixed(118));
            this.messageInput.margins(Insets.of(0, 0, 0, 5));
            this.messageInput.text(this.getData(data.value()).get("message").getAsString());
            this.messageInput.tooltip(Text.literal("The message/command that this shortcut will send. Example: /warp dungeon_hub"));
            this.messageInput.onChanged().subscribe(value -> data.edit(object -> this.getData(object).addProperty("message", value)));

            this.delete = Components.button(Text.literal("Delete").withColor(0xffffff), button -> {
                data.edit(object -> object.get("shortcuts").getAsJsonArray().remove(this.index));
                mc.setScreen(buildSettings());
            });
            this.delete.positioning(Positioning.relative(100, 50)).verticalSizing(Sizing.fixed(18));
            this.delete.renderer((context, btn, delta) -> {
                context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0xff101010);
                Rendering.drawBorder(context, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), 0xffffffff);
            });

            this.options.child(this.shortcutInput);
            this.options.child(this.messageInput);
            this.child(this.options);
            this.child(this.delete);
        }

        public JsonObject getData(JsonObject object) {
            return object.get("shortcuts").getAsJsonArray().get(this.index).getAsJsonObject();
        }
    }
}
