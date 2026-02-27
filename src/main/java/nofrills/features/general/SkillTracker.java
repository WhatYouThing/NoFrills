package nofrills.features.general;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.ChatMsgEvent;
import nofrills.events.OverlayMsgEvent;
import nofrills.events.WorldTickEvent;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.hud.clickgui.components.ToggleButton;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

public class SkillTracker {
    public static final Feature instance = new Feature("skillTracker");

    public static final SettingColor combatColor = new SettingColor(RenderColor.fromHex(0x55ffff), "combatColor", instance);
    public static final SettingColor farmingColor = new SettingColor(RenderColor.fromHex(0x55ff55), "farmingColor", instance);
    public static final SettingColor fishingColor = new SettingColor(RenderColor.fromHex(0x5555ff), "fishingColor", instance);
    public static final SettingColor miningColor = new SettingColor(RenderColor.fromHex(0xaa00aa), "miningColor", instance);
    public static final SettingColor foragingColor = new SettingColor(RenderColor.fromHex(0x00aa00), "foragingColor", instance);
    public static final SettingColor enchantingColor = new SettingColor(RenderColor.fromHex(0xff55ff), "enchantingColor", instance);
    public static final SettingColor alchemyColor = new SettingColor(RenderColor.fromHex(0xffff55), "alchemyColor", instance);
    public static final SettingColor carpentryColor = new SettingColor(RenderColor.fromHex(0xaa0000), "carpentryColor", instance);
    public static final SettingColor huntingColor = new SettingColor(RenderColor.fromHex(0x00aaaa), "huntingColor", instance);
    public static final SettingColor catacombsColor = new SettingColor(RenderColor.fromHex(0xff5555), "catacombsColor", instance);

    private static final Path sessionPath = Config.getFolderPath().resolve("SkillTracker.json");
    private static final List<String> skills = List.of(
            "Combat",
            "Farming",
            "Fishing",
            "Mining",
            "Foraging",
            "Enchanting",
            "Alchemy",
            "Carpentry",
            "Hunting",
            "Catacombs"
    );
    private static final JsonObject data = loadData();
    private static int saveTicks = 0;

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        for (String skill : skills) {
            FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            PlainLabel label = new PlainLabel(Text.literal(skill));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            ToggleButton toggle = new ToggleButton(isSessionActive(skill));
            toggle.margins(Insets.of(0, 0, 0, 5));
            toggle.onToggled().subscribe(value -> {
                if (!data.has(skill)) {
                    data.add(skill, getDefaultData());
                }
                data.get(skill).getAsJsonObject().addProperty("active", value);
                saveData();
            });
            ButtonComponent reset = UIComponents.button(Text.literal("Reset Session"), button -> {
                data.add(skill, getDefaultData());
                mc.setScreen(buildSettings());
                saveData();
            });
            reset.renderer(Settings.buttonRendererWhite);
            layout.child(label);
            layout.child(toggle);
            layout.child(reset);
            list.add(layout);
        }
        for (String skill : skills) {
            SettingColor color = getSessionColor(skill);
            if (color != null) {
                list.add(new Settings.ColorPicker(skill + " Color", false, color, "The color used for " + skill + " on the Skill Tracker Display HUD element."));
            }
        }
        return list;
    }

    public static Settings buildSettings() {
        Settings settings = new Settings(getSettingsList());
        settings.setTitle(Text.literal("Skill Tracker"));
        return settings;
    }

    private static JsonObject loadData() {
        if (Files.exists(sessionPath)) {
            try {
                return JsonParser.parseString(Files.readString(sessionPath)).getAsJsonObject();
            } catch (Exception exception) {
                LOGGER.error("Unable to load NoFrills Skill Tracker file!", exception);
            }
        }
        return new JsonObject();
    }

    public static void saveData() {
        Thread.startVirtualThread(() -> {
            try {
                Utils.atomicWrite(sessionPath, data.toString());
            } catch (Exception exception) {
                LOGGER.error("Unable to save NoFrills Skill Tracker file!", exception);
            }
        });
    }

    public static JsonObject getDefaultData() {
        JsonObject obj = new JsonObject();
        obj.addProperty("active", false);
        obj.addProperty("countedTicks", 0L);
        obj.addProperty("totalTicks", 0L);
        obj.addProperty("pauseTicks", 0L);
        obj.addProperty("lastPart", "");
        obj.addProperty("lastExp", 0.0);
        obj.addProperty("currentExp", 0.0);
        return obj;
    }

    private static SettingColor getSessionColor(String skill) {
        return switch (skill) {
            case "Combat" -> combatColor;
            case "Farming" -> farmingColor;
            case "Fishing" -> fishingColor;
            case "Mining" -> miningColor;
            case "Foraging" -> foragingColor;
            case "Enchanting" -> enchantingColor;
            case "Alchemy" -> alchemyColor;
            case "Carpentry" -> carpentryColor;
            case "Hunting" -> huntingColor;
            case "Catacombs" -> catacombsColor;
            default -> null;
        };
    }

    public static boolean isSessionActive(String skill) {
        return data.has(skill) && data.get(skill).getAsJsonObject().get("active").getAsBoolean();
    }

    public static boolean isSessionPaused(String skill) {
        if (data.has(skill)) {
            if (skill.equals("Catacombs") && Utils.isInDungeons()) {
                return false;
            }
            return data.get(skill).getAsJsonObject().get("pauseTicks").getAsLong() >= 600;
        }
        return false;
    }

    public static void tickSession(String skill) {
        if (data.has(skill)) {
            JsonObject obj = data.get(skill).getAsJsonObject();
            if (!isSessionPaused(skill)) {
                obj.addProperty("countedTicks", obj.get("countedTicks").getAsLong() + 1);
                obj.addProperty("pauseTicks", obj.get("pauseTicks").getAsLong() + 1);
            }
            obj.addProperty("totalTicks", obj.get("totalTicks").getAsLong() + 1);
        }
    }

    public static void addSessionExp(String skill, double exp) {
        if (data.has(skill)) {
            JsonObject obj = data.get(skill).getAsJsonObject();
            obj.addProperty("currentExp", obj.get("currentExp").getAsDouble() + exp);
            obj.addProperty("pauseTicks", 0);
        }
    }

    public static MutableText getText() {
        List<String> active = skills.stream().filter(SkillTracker::isSessionActive).toList();
        MutableText text = Text.literal("Skill Tracker");
        if (active.isEmpty()) {
            return text.append("\n§7None tracked.");
        }
        for (String skill : active) {
            SettingColor color = getSessionColor(skill);
            MutableText sessionText = color != null ? Text.literal(skill).withColor(color.value().argb) : Text.literal(skill);
            JsonObject obj = data.has(skill) ? data.get(skill).getAsJsonObject() : getDefaultData();
            long totalTicks = obj.get("totalTicks").getAsLong();
            long countedTicks = obj.get("countedTicks").getAsLong();
            double currentExp = obj.get("currentExp").getAsDouble();
            sessionText.append(Utils.format("\nEXP Per Hour: {}", Utils.formatSeparator(currentExp / (countedTicks / 72000.0))));
            sessionText.append(Utils.format("\nEXP Gained: {}", Utils.formatSeparator(currentExp)));
            sessionText.append(Utils.format("\nTime Counted: {}", Utils.ticksToTime(countedTicks)));
            sessionText.append(Utils.format("\nTime Elapsed: {}", Utils.ticksToTime(totalTicks)));
            text.append("\n").append(sessionText);
        }
        return text;
    }

    private static Optional<Double> parseExp(String exp) {
        return Utils.parseDouble(exp.replaceAll(",", ""));
    }

    @EventHandler
    private static void onOverlay(OverlayMsgEvent event) {
        if (instance.isActive()) {
            String msg = event.messagePlain;
            int index = msg.indexOf("+");
            if (index == -1) return;
            for (String skill : skills) {
                if (msg.contains(skill) && isSessionActive(skill)) {
                    String expPart = msg.substring(index, msg.indexOf(")", index) + 1);
                    JsonObject obj = data.get(skill).getAsJsonObject();
                    String lastPart = obj.get("lastPart").getAsString();
                    double lastExp = obj.get("lastExp").getAsDouble();
                    if (lastPart.equals(expPart)) {
                        continue;
                    }
                    if (!expPart.endsWith("/0)")) {
                        addSessionExp(skill, parseExp(expPart.substring(1, expPart.indexOf(" "))).orElse(0.0));
                    } else {
                        double exp = parseExp(expPart.substring(expPart.indexOf("(") + 1, expPart.indexOf("/"))).orElse(0.0);
                        if (exp != 0.0) {
                            if (lastExp != 0.0) {
                                addSessionExp(skill, exp - lastExp);
                            }
                            obj.addProperty("lastExp", exp);
                        }
                    }
                    obj.addProperty("lastPart", expPart);
                    break;
                }
            }
        }
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive()) {
            String msg = event.messagePlain.trim();
            if (msg.startsWith("+") && msg.endsWith(" Catacombs Experience") && isSessionActive("Catacombs")) {
                addSessionExp("Catacombs", parseExp(msg.substring(1, msg.indexOf(" "))).orElse(0.0));
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            for (String skill : skills) {
                if (isSessionActive(skill)) tickSession(skill);
            }
            saveTicks++;
            if (saveTicks >= 1200) {
                if (skills.stream().anyMatch(SkillTracker::isSessionActive)) {
                    saveData();
                }
                saveTicks = 0;
            }
        }
    }
}
