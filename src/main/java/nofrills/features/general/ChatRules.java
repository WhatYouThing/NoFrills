package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.chat.Component;
import nofrills.config.Config;
import nofrills.config.Feature;
import nofrills.config.SettingJson;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EventListener;
import nofrills.hud.HudManager;
import nofrills.hud.InnerSettingsScreen;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.FlatTextbox;
import nofrills.hud.clickgui.components.ToggleButton;
import nofrills.misc.DungeonUtil;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

@EventListener
public final class ChatRules {
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
                obj.add("classFilterList", new JsonArray());
                obj.add("islandFilterList", new JsonArray());
                obj.addProperty("title", "");
                obj.addProperty("titleFadeIn", 0);
                obj.addProperty("titleStay", 30);
                obj.addProperty("titleFadeOut", 10);
                obj.addProperty("titleVanilla", true);
                obj.addProperty("sound", "");
                obj.addProperty("soundVolume", 1.0f);
                obj.addProperty("soundPitch", 1.0f);
                object.get("rules").getAsJsonArray().add(obj);
            });
            Config.computeHash();
            mc.gui.setScreen(buildSettings());
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
        settings.setTitle(Component.literal("Chat Rules"));
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
        if (rule.has("classFilterList")) {
            String className = Utils.toLower(DungeonUtil.getPlayerClass());
            List<String> list = rule.get("classFilterList").getAsJsonArray().asList().stream()
                    .map(e -> Utils.toLower(e.getAsString().trim()))
                    .toList();
            if (!className.isEmpty() && !list.contains(className)) {
                return false;
            }
        }
        if (rule.has("islandFilterList")) {
            String area = Utils.toLower(SkyblockData.getArea());
            List<String> list = rule.get("islandFilterList").getAsJsonArray().asList().stream()
                    .map(e -> Utils.toLower(e.getAsString().trim()))
                    .toList();
            if (!area.isEmpty() && !list.contains(area)) {
                return false;
            }
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
                    yield Utils.pattern(rule.get("match").getAsString(), (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE) | Pattern.UNICODE_CASE)
                            .matcher(msg).matches();
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
                        if (obj.has("titleVanilla") && obj.get("titleVanilla").getAsBoolean()) {
                            Utils.showTitle(
                                    Utils.ofUserFormatted(title),
                                    Component.empty(),
                                    obj.get("titleFadeIn").getAsInt(),
                                    obj.get("titleStay").getAsInt(),
                                    obj.get("titleFadeOut").getAsInt()
                            );
                        } else {
                            HudManager.setCustomTitle(Utils.ofUserFormatted(title), obj.get("titleStay").getAsInt());
                        }
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

    public static final class Setting extends FlowLayout {
        public int index;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);

            this.index = index;
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);

            FlatTextbox input = new FlatTextbox(Sizing.fixed(140));
            input.margins(Insets.of(0, 0, 0, 6));
            input.text(this.getData().get("name").getAsString());
            input.tooltip(Component.literal("The name of this chat rule."));
            input.onChanged().subscribe(value -> data.edit(obj -> this.getData(obj).addProperty("name", value)));
            ToggleButton mainToggle = new ToggleButton(this.getData().get("enabled").getAsBoolean());
            mainToggle.sizing(Sizing.fixed(50), Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 3));
            mainToggle.tooltip(Component.literal("The main toggle for this chat rule."));
            mainToggle.onToggled().subscribe(toggle -> data.edit(obj -> this.getData(obj).addProperty("enabled", toggle)));
            ButtonComponent editButton = UIComponents.button(Component.literal("Edit").withColor(0xffffff), button -> mc.gui.setScreen(this.buildRuleSettings()));
            editButton.sizing(Sizing.fixed(48), Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 0));
            editButton.renderer(Settings.buttonRendererWhite);
            ButtonComponent delete = UIComponents.button(Component.literal("Delete").withColor(0xffffff), button -> {
                data.edit(object -> object.get("rules").getAsJsonArray().remove(this.index));
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
            return object.get("rules").getAsJsonArray().get(this.index).getAsJsonObject();
        }

        public JsonObject getData() {
            return this.getData(data.value());
        }

        public FlowLayout buildMatchTextSetting() {
            return new Settings.TextInput(
                    "Match Text",
                    this.getData().get("match").getAsString(),
                    "",
                    "The text/regex this rule would match with.",
                    s -> data.edit(obj -> this.getData(obj).addProperty("match", s))
            );
        }

        public FlowLayout buildMatchTypeSetting() {
            return new Settings.EnumToggle<>(
                    "Match Type",
                    getRuleMatchType(this.getData()),
                    MatchType.Equals,
                    MatchType.class,
                    "The type of match to perform.\n\nEquals: Message must be equal to the matching text.\nContains: Message must contain the matching text.\nStartsWith: Message must start with the matching text.\nEndsWith: Message must end with the matching text.\nRegex: Message must match the Java regular expression, advanced users only.",
                    e -> data.edit(obj -> this.getData(obj).addProperty("matchType", e.name()))
            );
        }

        public FlowLayout buildCaseSensitiveSetting() {
            return new Settings.Toggle(
                    "Case Sensitive",
                    this.getData().get("caseSensitive").getAsBoolean(),
                    false,
                    "If enabled, the rule will account for character casing.",
                    b -> data.edit(obj -> this.getData(obj).addProperty("caseSensitive", b))
            );
        }

        public FlowLayout buildCancelSetting() {
            return new Settings.Toggle(
                    "Cancel Message",
                    this.getData().get("cancel").getAsBoolean(),
                    false,
                    "If enabled, the rule will hide/cancel matching chat messages.",
                    b -> data.edit(obj -> this.getData(obj).addProperty("cancel", b))
            );
        }

        public FlowLayout buildClassFilterSetting() {
            return new Settings.TextEntryList(
                    "Class Filter",
                    () -> this.getData().has("classFilterList")
                            ? this.getData().get("classFilterList").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList()
                            : DungeonUtil.getDungeonClasses().stream().toList(),
                    "Prevents this rule from matching if your selected dungeon class is not on this list.\nLeave empty to disable class filtering.",
                    l -> data.edit(obj -> this.getData(obj).add("classFilterList", Utils.toJsonArray(l)))
            );
        }

        public FlowLayout buildIslandFilterSetting() {
            return new Settings.TextEntryList(
                    "Island Filter",
                    () -> this.getData().has("islandFilterList")
                            ? this.getData().get("islandFilterList").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList()
                            : List.of(),
                    "Prevents this rule from matching if the current tablist area name is not on this list.\nLeave empty to disable island filtering.",
                    l -> data.edit(obj -> this.getData(obj).add("islandFilterList", Utils.toJsonArray(l)))
            );
        }

        public FlowLayout buildTitleSetting() {
            return new Settings.TitleInput(
                    "Title",
                    this.getData().get("title").getAsString(),
                    "",
                    this.getData().get("titleFadeIn").getAsInt(),
                    0,
                    this.getData().get("titleStay").getAsInt(),
                    30,
                    this.getData().get("titleFadeOut").getAsInt(),
                    10,
                    this.getData().has("titleVanilla") && this.getData().get("titleVanilla").getAsBoolean(),
                    true,
                    "The title to display when this rule matches.",
                    s -> data.edit(obj -> this.getData(obj).addProperty("title", s)),
                    i -> data.edit(obj -> this.getData(obj).addProperty("titleFadeIn", i)),
                    i -> data.edit(obj -> this.getData(obj).addProperty("titleStay", i)),
                    i -> data.edit(obj -> this.getData(obj).addProperty("titleFadeOut", i)),
                    b -> data.edit(obj -> this.getData(obj).addProperty("titleVanilla", b))
            );
        }

        public FlowLayout buildSoundSetting() {
            return new Settings.SoundInput(
                    "Sound",
                    this.getData().get("sound").getAsString(),
                    "",
                    this.getData().get("soundVolume").getAsDouble(),
                    1.0,
                    this.getData().get("soundPitch").getAsDouble(),
                    1.0,
                    "The sound to play when this rule matches.",
                    s -> data.edit(obj -> this.getData(obj).addProperty("sound", s)),
                    d -> data.edit(obj -> this.getData(obj).addProperty("soundVolume", d)),
                    d -> data.edit(obj -> this.getData(obj).addProperty("soundPitch", d))
            );
        }

        public InnerSettingsScreen buildRuleSettings() {
            List<FlowLayout> list = new ArrayList<>();
            list.add(this.buildMatchTextSetting());
            list.add(this.buildMatchTypeSetting());
            list.add(this.buildCaseSensitiveSetting());
            list.add(this.buildCancelSetting());
            list.add(this.buildClassFilterSetting());
            list.add(this.buildIslandFilterSetting());
            list.add(this.buildTitleSetting());
            list.add(this.buildSoundSetting());
            return new InnerSettingsScreen(list, Component.literal("Chat Rule: " + this.getData().get("name").getAsString()), buildSettings());
        }
    }
}
