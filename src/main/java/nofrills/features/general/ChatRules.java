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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nofrills.config.Config;
import nofrills.config.Feature;
import nofrills.config.SettingJson;
import nofrills.events.ChatMsgEvent;
import nofrills.events.WorldTickEvent;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.EnumButton;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.hud.clickgui.components.ToggleButton;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class ChatRules {
    public static final Feature instance = new Feature("chatRules");

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);

    private static final List<Rule> ruleCache = new ArrayList<>();
    private static final Pattern fallbackPattern = Pattern.compile("null");
    private static int hash = 0;

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        Settings.BigButton button = new Settings.BigButton("Add New Chat Rule", btn -> {
            if (!data.value().has("rules")) {
                data.value().add("rules", new JsonArray());
            }
            data.value().get("rules").getAsJsonArray().add(new Rule().toObject());
            data.save();
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

    private static boolean matchRule(Rule rule, String msg) {
        String match = rule.caseSensitive ? rule.match : Utils.toLower(rule.match);
        String message = rule.caseSensitive ? msg : Utils.toLower(msg);
        return rule.enabled && switch (rule.matchType) {
            case Equals -> message.equals(match);
            case Contains -> message.contains(match);
            case StartsWith -> message.startsWith(match);
            case EndsWith -> message.endsWith(match);
            case Regex -> rule.getRegex().matcher(message).matches();
        };
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onMsg(ChatMsgEvent event) {
        if (instance.isActive()) {
            for (Rule rule : new ArrayList<>(ruleCache)) {
                if (matchRule(rule, event.messagePlain)) {
                    if (!rule.title.isEmpty())
                        Utils.showTitle(rule.title.replaceAll("&", "§"), "", rule.titleFadeIn, rule.titleStay, rule.titleFadeOut);
                    if (!rule.sound.isEmpty())
                        Utils.playSound(SoundEvent.of(Identifier.of(rule.sound)), SoundCategory.MASTER, rule.soundVolume, rule.soundPitch);
                    if (rule.cancel) event.cancel();
                    break;
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && (hash == 0 || hash != Config.getHash())) {
            ruleCache.clear();
            if (data.value().has("rules")) {
                for (JsonElement rule : data.value().getAsJsonArray("rules")) {
                    ruleCache.add(Rule.fromObject(rule.getAsJsonObject()));
                }
            }
            hash = Config.getHash();
        }
    }

    public enum MatchType {
        Equals,
        Contains,
        StartsWith,
        EndsWith,
        Regex
    }

    public static class Rule {
        public String name = "New Rule";
        public String match = "";
        public boolean enabled = false;
        public boolean caseSensitive = false;
        public MatchType matchType = MatchType.Equals;
        public boolean cancel = false;
        public String title = "";
        public int titleFadeIn = 5;
        public int titleStay = 30;
        public int titleFadeOut = 5;
        public String sound = "";
        public float soundVolume = 1.0f;
        public float soundPitch = 1.0f;
        public Pattern regex;

        public Rule() {
        }

        public static Rule fromObject(JsonObject obj) {
            Rule rule = new Rule();
            rule.name = obj.get("name").getAsString();
            rule.match = obj.get("match").getAsString();
            rule.enabled = obj.get("enabled").getAsBoolean();
            rule.caseSensitive = obj.get("caseSensitive").getAsBoolean();
            rule.matchType = Arrays.stream(MatchType.values())
                    .filter(value -> value.name().equals(obj.get("matchType").getAsString()))
                    .findFirst()
                    .orElse(MatchType.Equals);
            rule.cancel = obj.get("cancel").getAsBoolean();
            rule.title = obj.get("title").getAsString();
            rule.titleFadeIn = obj.get("titleFadeIn").getAsInt();
            rule.titleStay = obj.get("titleStay").getAsInt();
            rule.titleFadeOut = obj.get("titleFadeOut").getAsInt();
            rule.sound = obj.get("sound").getAsString();
            rule.soundVolume = obj.get("soundVolume").getAsFloat();
            rule.soundPitch = obj.get("soundPitch").getAsFloat();
            return rule;
        }

        public JsonObject toObject() {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", this.name);
            obj.addProperty("match", this.match);
            obj.addProperty("enabled", this.enabled);
            obj.addProperty("caseSensitive", this.caseSensitive);
            obj.addProperty("matchType", this.matchType.name());
            obj.addProperty("cancel", this.cancel);
            obj.addProperty("title", this.title);
            obj.addProperty("titleFadeIn", this.titleFadeIn);
            obj.addProperty("titleStay", this.titleStay);
            obj.addProperty("titleFadeOut", this.titleFadeOut);
            obj.addProperty("sound", this.sound);
            obj.addProperty("soundVolume", this.soundVolume);
            obj.addProperty("soundPitch", this.soundPitch);
            return obj;
        }

        public Pattern getRegex() {
            if (this.regex == null) {
                try {
                    this.regex = Pattern.compile(this.match);
                } catch (Exception exception) {
                    Utils.infoFormat("Failed to compile Regex pattern for chat rule \"{}\": {}", this.name, exception.getMessage());
                    this.regex = fallbackPattern;
                }
            }
            return this.regex;
        }
    }

    public static class Setting extends FlowLayout {
        public int index;
        public Rule rule;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);

            this.index = index;
            this.rule = Rule.fromObject(this.getData());
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);

            FlatTextbox input = new FlatTextbox(Sizing.fixed(140));
            input.margins(Insets.of(0, 0, 0, 6));
            input.text(rule.name);
            input.tooltip(Text.literal("The name of this chat rule."));
            input.onChanged().subscribe(value -> {
                rule.name = value;
                this.save();
            });
            ToggleButton mainToggle = new ToggleButton(rule.enabled);
            mainToggle.verticalSizing(Sizing.fixed(18));
            mainToggle.margins(Insets.of(1, 0, 0, 3));
            mainToggle.tooltip(Text.literal("The main toggle for this chat rule."));
            mainToggle.onToggled().subscribe(toggle -> {
                rule.enabled = toggle;
                this.save();
            });
            ButtonComponent editButton = Components.button(Text.literal("Edit").withColor(0xffffff), button -> mc.setScreen(this.buildRuleSettings()));
            editButton.verticalSizing(Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 0));
            editButton.horizontalSizing(Sizing.fixed(49));
            editButton.renderer(Settings.buttonRendererWhite);
            ButtonComponent delete = Components.button(Text.literal("Delete").withColor(0xffffff), button -> {
                data.value().get("rules").getAsJsonArray().remove(this.index);
                data.save();
                mc.setScreen(buildSettings());
            });
            delete.positioning(Positioning.relative(100, 50)).verticalSizing(Sizing.fixed(18));
            delete.renderer(Settings.buttonRendererWhite);

            this.child(input);
            this.child(mainToggle);
            this.child(editButton);
            this.child(delete);
        }

        public JsonObject getData() {
            return data.value().get("rules").getAsJsonArray().get(this.index).getAsJsonObject();
        }

        public void save() {
            data.value().get("rules").getAsJsonArray().set(this.index, this.rule.toObject());
            data.save();
        }

        public FlowLayout buildMatchTextSetting() {
            FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Text.literal("Match Text"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            FlatTextbox input = new FlatTextbox(Sizing.fixed(200));
            input.text(rule.match);
            input.tooltip(Text.literal("The text/regex this rule would match with."));
            input.onChanged().subscribe(value -> {
                rule.match = value;
                this.save();
            });
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
            EnumButton<MatchType> button = new EnumButton<>(rule.matchType.name(), MatchType.Equals, MatchType.class);
            button.horizontalSizing(Sizing.fixed(80));
            button.tooltip(Text.literal("The type of match to perform.\n\nEquals: Message must be equal to the matching text.\nContains: Message must contain the matching text.\nStartsWith: Message must start with the matching text.\nEndsWith: Message must end with the matching text.\nRegex: Message must match the Java regular expression, advanced users only."));
            button.onChanged().subscribe(value -> {
                rule.matchType = Arrays.stream(MatchType.values()).filter(type -> type.name().equals(value)).findFirst().orElse(MatchType.Equals);
                this.save();
            });
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
            ToggleButton button = new ToggleButton(rule.caseSensitive);
            button.tooltip(Text.literal("If enabled, the rule will account for character casing."));
            button.onToggled().subscribe(toggle -> {
                rule.caseSensitive = toggle;
                this.save();
            });
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
            ToggleButton button = new ToggleButton(rule.cancel);
            button.tooltip(Text.literal("If enabled, the rule will hide/cancel matching chat messages."));
            button.onToggled().subscribe(toggle -> {
                rule.cancel = toggle;
                this.save();
            });
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
            inputTitle.text(rule.title);
            inputTitle.tooltip(Text.literal("The title to show on screen if the rule matches. Leave blank to disable.\nYou can use the & symbol to insert formatting codes."));
            inputTitle.onChanged().subscribe(value -> {
                rule.title = value;
                this.save();
            });
            FlatTextbox inputFadeIn = new FlatTextbox(Sizing.fixed(25));
            inputFadeIn.text(String.valueOf(rule.titleFadeIn));
            inputFadeIn.tooltip(Text.literal("The amount of ticks the title should fade in for."));
            inputFadeIn.onChanged().subscribe(value -> {
                Optional<Integer> ticks = Utils.parseInt(value);
                if (ticks.isPresent()) {
                    rule.titleFadeIn = ticks.get();
                    this.save();
                }
            });
            FlatTextbox inputStay = new FlatTextbox(Sizing.fixed(25));
            inputStay.text(String.valueOf(rule.titleStay));
            inputStay.tooltip(Text.literal("The amount of ticks the title should stay for."));
            inputStay.onChanged().subscribe(value -> {
                Optional<Integer> ticks = Utils.parseInt(value);
                if (ticks.isPresent()) {
                    rule.titleStay = ticks.get();
                    this.save();
                }
            });
            FlatTextbox inputFadeOut = new FlatTextbox(Sizing.fixed(25));
            inputFadeOut.text(String.valueOf(rule.titleFadeOut));
            inputFadeOut.tooltip(Text.literal("The amount of ticks the title should fade out for."));
            inputFadeOut.onChanged().subscribe(value -> {
                Optional<Integer> ticks = Utils.parseInt(value);
                if (ticks.isPresent()) {
                    rule.titleFadeOut = ticks.get();
                    this.save();
                }
            });
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
            inputSound.text(rule.sound);
            inputSound.tooltip(Text.literal("The sound identifier to play if the rule matches. Leave blank to disable."));
            inputSound.onChanged().subscribe(value -> {
                rule.sound = value;
                this.save();
            });
            FlatTextbox inputVolume = new FlatTextbox(Sizing.fixed(25));
            inputVolume.text(String.valueOf(rule.soundVolume));
            inputVolume.tooltip(Text.literal("The volume of the sound."));
            inputVolume.onChanged().subscribe(value -> {
                Optional<Double> volume = Utils.parseDouble(value);
                if (volume.isPresent()) {
                    rule.soundVolume = volume.get().floatValue();
                    this.save();
                }
            });
            FlatTextbox inputPitch = new FlatTextbox(Sizing.fixed(25));
            inputPitch.text(String.valueOf(rule.soundPitch));
            inputPitch.tooltip(Text.literal("The pitch of the sound."));
            inputPitch.onChanged().subscribe(value -> {
                Optional<Double> pitch = Utils.parseDouble(value);
                if (pitch.isPresent()) {
                    rule.soundPitch = pitch.get().floatValue();
                    this.save();
                }
            });
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
            settings.setTitle(Text.literal("Chat Rule: " + this.rule.name));
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
