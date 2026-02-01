package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.config.Feature;
import nofrills.config.SettingJson;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.EnumButton;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.hud.clickgui.components.ToggleButton;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class ChatRules {
    public static final Feature instance = new Feature("chatRules");

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        Settings.BigButton button = new Settings.BigButton("Add New Chat Rule", btn -> {
            data.edit(object -> {
                if (!object.has("rules")) {
                    object.add("rules", new JsonArray());
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("name", "New Rule");
                obj.addProperty("match", "");
                obj.addProperty("enabled", false);
                obj.addProperty("caseSensitive", false);
                obj.addProperty("matchType", MatchType.Equals.name());
                obj.addProperty("cancel", false);
                obj.addProperty("title", "");
                obj.addProperty("titleFadeIn", 0);
                obj.addProperty("titleStay", 30);
                obj.addProperty("titleFadeOut", 10);
                obj.addProperty("sound", "");
                obj.addProperty("soundVolume", 1.0f);
                obj.addProperty("soundPitch", 1.0f);
                object.get("rules").getAsJsonArray().add(obj);
            });
            Config.computeHash();
            mc.setScreen(buildSettings());
        });
        button.button.verticalSizing(Sizing.fixed(18));
        list.add(button);
        if (data.value().has("rules")) {
            JsonArray rules = data.value().get("rules").getAsJsonArray();
            for (int i = 0; i < rules.size(); i++) {
                list.add(new Setting(i));
            }
        }
        return list;
    }

    public static Settings buildSettings() {
        Settings settings = new Settings(getSettingsList());
        settings.setTitle(Text.literal("Chat Rules"));
        return settings;
    }

    private static MatchType getRuleMatchType(JsonObject rule) {
        String type = rule.get("matchType").getAsString();
        for (MatchType value : MatchType.values()) {
            if (value.name().equals(type)) {
                return value;
            }
        }
        return MatchType.Equals;
    }

    private static boolean matchRule(JsonObject rule, String msg) {
        if (!rule.get("enabled").getAsBoolean()) {
            return false;
        }
        boolean caseSensitive = rule.get("caseSensitive").getAsBoolean();
        String match = caseSensitive ? rule.get("match").getAsString() : Utils.toLower(rule.get("match").getAsString());
        String message = caseSensitive ? msg : Utils.toLower(msg);
        return switch (getRuleMatchType(rule)) {
            case Equals -> message.equals(match);
            case Contains -> message.contains(match);
            case StartsWith -> message.startsWith(match);
            case EndsWith -> message.endsWith(match);
            case Regex -> {
                try {
                    yield Pattern.compile(rule.get("match").getAsString()).matcher(msg).matches();
                } catch (Exception exception) {
                    Utils.infoFormat("§cFailed to compile Regex pattern for chat rule {}: {}", rule.get("name").getAsString(), exception.getMessage());
                    yield false;
                }
            }
        };
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onMsg(ChatMsgEvent event) {
        if (instance.isActive() && data.value().has("rules")) {
            for (JsonElement rule : data.value().getAsJsonArray("rules")) {
                JsonObject obj = rule.getAsJsonObject();
                if (matchRule(obj, event.messagePlain)) {
                    String title = obj.get("title").getAsString();
                    if (!title.isEmpty()) {
                        Utils.showTitle(title.replaceAll("&", "§"), "", obj.get("titleFadeIn").getAsInt(), obj.get("titleStay").getAsInt(), obj.get("titleFadeOut").getAsInt());
                    }
                    String sound = obj.get("sound").getAsString();
                    if (!sound.isEmpty()) {
                        Utils.playSound(sound, obj.get("soundVolume").getAsFloat(), obj.get("soundPitch").getAsFloat());
                    }
                    if (obj.get("cancel").getAsBoolean()) {
                        event.cancel();
                    }
                    break;
                }
            }
        }
    }

    public enum MatchType {
        Equals,
        Contains,
        StartsWith,
        EndsWith,
        Regex
    }

    public static class Setting extends FlowLayout {
        public int index;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);

            this.index = index;
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);

            FlatTextbox input = new FlatTextbox(Sizing.fixed(140));
            input.margins(Insets.of(0, 0, 0, 6));
            input.text(this.getData().get("name").getAsString());
            input.tooltip(Text.literal("The name of this chat rule."));
            input.onChanged().subscribe(value -> data.edit(obj -> this.getData(obj).addProperty("name", value)));
            ToggleButton mainToggle = new ToggleButton(this.getData().get("enabled").getAsBoolean());
            mainToggle.verticalSizing(Sizing.fixed(18));
            mainToggle.margins(Insets.of(1, 0, 0, 3));
            mainToggle.tooltip(Text.literal("The main toggle for this chat rule."));
            mainToggle.onToggled().subscribe(toggle -> data.edit(obj -> this.getData(obj).addProperty("enabled", toggle)));
            ButtonComponent editButton = Components.button(Text.literal("Edit").withColor(0xffffff), button -> mc.setScreen(this.buildRuleSettings()));
            editButton.verticalSizing(Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 0));
            editButton.horizontalSizing(Sizing.fixed(49));
            editButton.renderer(Settings.buttonRendererWhite);
            ButtonComponent delete = Components.button(Text.literal("Delete").withColor(0xffffff), button -> {
                data.edit(object -> object.get("rules").getAsJsonArray().remove(this.index));
                mc.setScreen(buildSettings());
            });
            delete.positioning(Positioning.relative(100, 50)).verticalSizing(Sizing.fixed(18));
            delete.renderer(Settings.buttonRendererWhite);

            this.child(input);
            this.child(mainToggle);
            this.child(editButton);
            this.child(delete);
        }

        public JsonObject getData(JsonObject object) {
            return object.get("rules").getAsJsonArray().get(this.index).getAsJsonObject();
        }

        public JsonObject getData() {
            return this.getData(data.value());
        }

        public FlowLayout buildMatchTextSetting() {
            FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Text.literal("Match Text"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            FlatTextbox input = new FlatTextbox(Sizing.fixed(200));
            input.text(this.getData().get("match").getAsString());
            input.tooltip(Text.literal("The text/regex this rule would match with."));
            input.onChanged().subscribe(value -> data.edit(obj -> this.getData(obj).addProperty("match", value)));
            layout.child(label);
            layout.child(input);
            return layout;
        }

        public FlowLayout buildMatchTypeSetting() {
            FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Text.literal("Match Type"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            EnumButton<MatchType> button = new EnumButton<>(this.getData().get("matchType").getAsString(), MatchType.Equals, MatchType.class);
            button.horizontalSizing(Sizing.fixed(80));
            button.tooltip(Text.literal("The type of match to perform.\n\nEquals: Message must be equal to the matching text.\nContains: Message must contain the matching text.\nStartsWith: Message must start with the matching text.\nEndsWith: Message must end with the matching text.\nRegex: Message must match the Java regular expression, advanced users only."));
            button.onChanged().subscribe(value -> data.edit(obj -> this.getData(obj).addProperty("matchType", value)));
            layout.child(label);
            layout.child(button);
            return layout;
        }

        public FlowLayout buildCaseSensitiveSetting() {
            FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Text.literal("Case Sensitive"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            ToggleButton button = new ToggleButton(this.getData().get("caseSensitive").getAsBoolean());
            button.tooltip(Text.literal("If enabled, the rule will account for character casing."));
            button.onToggled().subscribe(toggle -> data.edit(obj -> this.getData(obj).addProperty("caseSensitive", toggle)));
            layout.child(label);
            layout.child(button);
            return layout;
        }

        public FlowLayout buildCancelSetting() {
            FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Text.literal("Cancel Message"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            ToggleButton button = new ToggleButton(this.getData().get("cancel").getAsBoolean());
            button.tooltip(Text.literal("If enabled, the rule will hide/cancel matching chat messages."));
            button.onToggled().subscribe(toggle -> data.edit(obj -> this.getData(obj).addProperty("cancel", toggle)));
            layout.child(label);
            layout.child(button);
            return layout;
        }

        public FlowLayout buildTitleSetting() {
            FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Text.literal("Title"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            FlatTextbox inputTitle = new FlatTextbox(Sizing.fixed(140));
            inputTitle.text(this.getData().get("title").getAsString());
            inputTitle.tooltip(Text.literal("The title to show on screen if the rule matches. Leave blank to disable.\nYou can use the & symbol to insert formatting codes."));
            inputTitle.onChanged().subscribe(value -> data.edit(obj -> this.getData(obj).addProperty("title", value)));
            FlatTextbox inputFadeIn = new FlatTextbox(Sizing.fixed(25));
            inputFadeIn.text(String.valueOf(this.getData().get("titleFadeIn").getAsInt()));
            inputFadeIn.tooltip(Text.literal("The amount of ticks the title should fade in for."));
            inputFadeIn.onChanged().subscribe(value -> Utils.parseInt(value).ifPresent(integer -> data.edit(obj -> this.getData(obj).addProperty("titleFadeIn", integer))));
            FlatTextbox inputStay = new FlatTextbox(Sizing.fixed(25));
            inputStay.text(String.valueOf(this.getData().get("titleStay").getAsInt()));
            inputStay.tooltip(Text.literal("The amount of ticks the title should stay for."));
            inputStay.onChanged().subscribe(value -> Utils.parseInt(value).ifPresent(integer -> data.edit(obj -> this.getData(obj).addProperty("titleStay", integer))));
            FlatTextbox inputFadeOut = new FlatTextbox(Sizing.fixed(25));
            inputFadeOut.text(String.valueOf(this.getData().get("titleFadeOut").getAsInt()));
            inputFadeOut.tooltip(Text.literal("The amount of ticks the title should fade out for."));
            inputFadeOut.onChanged().subscribe(value -> Utils.parseInt(value).ifPresent(integer -> data.edit(obj -> this.getData(obj).addProperty("titleFadeOut", integer))));
            layout.child(label);
            layout.child(inputTitle);
            layout.child(inputFadeIn);
            layout.child(inputStay);
            layout.child(inputFadeOut);
            return layout;
        }

        public FlowLayout buildSoundSetting() {
            FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Text.literal("Sound"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            FlatTextbox inputSound = new FlatTextbox(Sizing.fixed(150));
            inputSound.text(this.getData().get("sound").getAsString());
            inputSound.tooltip(Text.literal("The sound identifier to play if the rule matches. Leave blank to disable."));
            inputSound.onChanged().subscribe(value -> data.edit(obj -> this.getData(obj).addProperty("sound", value)));
            FlatTextbox inputVolume = new FlatTextbox(Sizing.fixed(25));
            inputVolume.text(String.valueOf(this.getData().get("soundVolume").getAsFloat()));
            inputVolume.tooltip(Text.literal("The volume of the sound."));
            inputVolume.onChanged().subscribe(value -> Utils.parseDouble(value).ifPresent(val -> data.edit(obj -> this.getData(obj).addProperty("soundVolume", val.floatValue()))));
            FlatTextbox inputPitch = new FlatTextbox(Sizing.fixed(25));
            inputPitch.text(String.valueOf(this.getData().get("soundPitch").getAsFloat()));
            inputPitch.tooltip(Text.literal("The pitch of the sound."));
            inputPitch.onChanged().subscribe(value -> Utils.parseDouble(value).ifPresent(val -> data.edit(obj -> this.getData(obj).addProperty("soundPitch", val.floatValue()))));
            layout.child(label);
            layout.child(inputSound);
            layout.child(inputVolume);
            layout.child(inputPitch);
            return layout;
        }

        public ChatRulesSettings buildRuleSettings() {
            List<FlowLayout> list = new ArrayList<>();
            list.add(this.buildMatchTextSetting());
            list.add(this.buildMatchTypeSetting());
            list.add(this.buildCaseSensitiveSetting());
            list.add(this.buildCancelSetting());
            list.add(this.buildTitleSetting());
            list.add(this.buildSoundSetting());
            ChatRulesSettings settings = new ChatRulesSettings(list);
            settings.setTitle(Text.literal("Chat Rule: " + this.getData().get("name").getAsString()));
            return settings;
        }
    }

    public static class ChatRulesSettings extends Settings {
        public ChatRulesSettings(List<FlowLayout> settings) {
            super(settings);
        }

        @Override
        public void close() {
            mc.setScreen(buildSettings());
        }
    }

}
