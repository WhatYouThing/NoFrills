package nofrills.features.general;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.events.OverlayMsgEvent;
import nofrills.events.WorldTickEvent;
import nofrills.hud.HudManager;
import nofrills.hud.clickgui.Settings;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.hud.clickgui.components.ToggleButton;
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
            FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
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
                new Thread(SkillTracker::saveData).start();
            });
            ButtonComponent reset = Components.button(Text.literal("Reset Session"), button -> {
                data.add(skill, getDefaultData());
                mc.setScreen(buildSettings());
                new Thread(SkillTracker::saveData).start();
            });
            reset.renderer(Settings.buttonRendererWhite);
            layout.child(label);
            layout.child(toggle);
            layout.child(reset);
            list.add(layout);
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
        try {
            Utils.atomicWrite(sessionPath, data.toString());
        } catch (Exception exception) {
            LOGGER.error("Unable to save NoFrills Skill Tracker file!", exception);
        }
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

    public static boolean isSessionActive(String skill) {
        return data.has(skill) && data.get(skill).getAsJsonObject().get("active").getAsBoolean();
    }

    public static boolean isSessionPaused(String skill) {
        if (data.has(skill)) {
            if (skill.equals("Catacombs") && Utils.isInDungeons()) {
                return false;
            }
            return data.get(skill).getAsJsonObject().get("pauseTicks").getAsLong() >= 1200;
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
        if (active.isEmpty()) {
            return Text.literal("§bSkill Tracker\n§7None tracked.");
        }
        MutableText text = Text.literal("§bSkill Tracker");
        for (String skill : active) {
            MutableText sessionText = Text.literal("§a" + skill);
            JsonObject obj = data.has(skill) ? data.get(skill).getAsJsonObject() : getDefaultData();
            long totalTicks = obj.get("totalTicks").getAsLong();
            long countedTicks = obj.get("countedTicks").getAsLong();
            double currentExp = obj.get("currentExp").getAsDouble();
            sessionText.append(Utils.format("\nTime Elapsed: {}", Utils.ticksToTime(totalTicks)));
            sessionText.append(Utils.format("\nTime Counted: {}", Utils.ticksToTime(countedTicks)));
            sessionText.append(Utils.format("\nEXP Gained: {}", Utils.formatSeparator(currentExp)));
            sessionText.append(Utils.format("\nEXP Per Hour: {}", Utils.formatSeparator(currentExp / (countedTicks / 72000.0))));
            text.append("\n").append(sessionText);
        }
        return text;
    }

    private static Optional<Double> parseExp(String exp) {
        return Utils.parseDouble(exp.replaceAll(",", ""));
    }

    @EventHandler
    private static void onOverlay(OverlayMsgEvent event) {
        String msg = event.messagePlain;
        if (instance.isActive()) {
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
            if (HudManager.skillTrackerElement.instance.isActive()) {
                HudManager.skillTrackerElement.setText(getText());
            }
            saveTicks++;
            if (saveTicks >= 1200) {
                if (skills.stream().anyMatch(SkillTracker::isSessionActive)) {
                    new Thread(SkillTracker::saveData).start();
                }
                saveTicks = 0;
            }
        }
    }
}
