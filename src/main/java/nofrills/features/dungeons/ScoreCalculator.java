package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingEnum;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.events.PartyChatMsgEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.NoFrillsAPI;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.Optional;

public class ScoreCalculator {
    public static final Feature instance = new Feature("scoreCalculator");

    public static final SettingEnum<PaulState> paulState = new SettingEnum<>(PaulState.Auto, PaulState.class, "paulState", instance);
    public static final SettingBool sendMsg270 = new SettingBool(false, "sendMsg270", instance);
    public static final SettingString msg270 = new SettingString("/pc 270 Score!", "msg270", instance);
    public static final SettingBool showTitle270 = new SettingBool(false, "showTitle270", instance);
    public static final SettingString title270 = new SettingString("&c&l270", "title270", instance);
    public static final SettingBool sendMsg300 = new SettingBool(false, "sendMsg300", instance);
    public static final SettingString msg300 = new SettingString("/pc 300 Score!", "msg300", instance);
    public static final SettingBool showTitle300 = new SettingBool(false, "showTitle300", instance);
    public static final SettingString title300 = new SettingString("&c&l300", "title300", instance);

    private static int score = 0;
    private static int deaths = 0;
    private static boolean bloodDone = false;
    private static boolean mimic = false;
    private static boolean prince = false;
    private static boolean sent270 = false;
    private static boolean sent300 = false;

    private static String getLineValue(String line) {
        if (line.contains("%")) {
            line = line.substring(0, line.indexOf("%"));
        }
        return line.substring(line.indexOf(":") + 1).trim();
    }

    private static void processAlert(SettingBool send, SettingString msg, SettingBool doTitle, SettingString title) {
        if (send.value()) {
            Utils.sendMessage(msg.value());
        }
        if (doTitle.value()) {
            Utils.showTitle(title.value().replaceAll("&", "§"), "", 0, 30, 10);
            Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
        }
    }

    private static boolean isEZPZ() {
        return switch (paulState.value()) {
            case Auto -> NoFrillsAPI.electionPerks.contains("EZPZ");
            case Active -> true;
            case Inactive -> false;
        };
    }

    private static double getClearedPercent() {
        for (String line : SkyblockData.getLines()) {
            if (line.startsWith("Cleared: ")) {
                Optional<Double> value = Utils.parseDouble(getLineValue(line));
                if (value.isPresent()) {
                    return value.get() * 0.01;
                }
            }
        }
        return 1.0;
    }

    private static double getSecretsFound() {
        for (String line : SkyblockData.getTabListLines()) {
            if (line.startsWith("Secrets Found: ") && line.endsWith("%")) {
                return Utils.parseDouble(getLineValue(line)).orElse(0.0) * 0.01;
            }
        }
        return 0.0;
    }

    private static int getClearedRooms() {
        for (String line : SkyblockData.getTabListLines()) {
            if (line.startsWith("Completed Rooms: ")) {
                return Utils.parseInt(getLineValue(line)).orElse(0);
            }
        }
        return 0;
    }

    private static int getTotalClearedRooms() {
        int rooms = getClearedRooms();
        if (!bloodDone) {
            rooms += 1;
        }
        if (!DungeonUtil.isInBossRoom()) {
            rooms += 1;
        }
        return rooms;
    }

    private static double getSecretsNeeded() {
        return switch (DungeonUtil.getCurrentFloor()) {
            case "F1" -> 0.3;
            case "F2" -> 0.4;
            case "F3" -> 0.5;
            case "F4" -> 0.6;
            case "F5" -> 0.7;
            case "F6" -> 0.85;
            default -> 1.0;
        };
    }

    private static int getTotalRooms() {
        return (int) Math.round(getClearedRooms() / getClearedPercent());
    }

    private static int getDeathPenalty() {
        if (deaths > 0) { // need to assume spirit pet on 1st death, api key application got declined a month after applying
            return deaths * 2 - 1;
        }
        return 0;
    }

    private static int getPuzzlePenalty() {
        int failed = 0;
        for (String line : SkyblockData.getTabListLines()) {
            if (line.contains(": [✖]") || line.contains(": [✦]")) {
                failed += 1;
            }
        }
        return 10 * failed;
    }

    private static int getSkillScore(double clearedRooms, double totalRooms) {
        int skillScore = Math.min((int) Math.floor(80.0 * clearedRooms / totalRooms), 80);
        return 20 + Math.clamp(skillScore - getPuzzlePenalty() - getDeathPenalty(), 0, 80);
    }

    private static int getExploreScore(double clearedRooms, double totalRooms, double secretsFound, double secretsNeeded) {
        int clearScore = Math.min((int) Math.floor(60.0 * clearedRooms / totalRooms), 60);
        int secretScore = Math.min((int) Math.floor(40.0 * secretsFound / secretsNeeded), 40);
        return Math.clamp(clearScore + secretScore, 0, 100);
    }

    private static int getSpeedScore() {
        return 100;
    }

    private static int getBonusScore() {
        int bonus = 0;
        if (mimic) {
            bonus += 2;
        }
        if (prince) {
            bonus += 1;
        }
        if (isEZPZ()) {
            bonus += 10;
        }
        for (String line : SkyblockData.getTabListLines()) {
            if (line.startsWith("Crypts: ")) {
                bonus += Math.clamp(Utils.parseInt(getLineValue(line)).orElse(0), 0, 5);
            }
        }
        return bonus;
    }

    public static void mimicKilled() {
        mimic = true;
    }

    public static void princeKilled() {
        prince = true;
    }

    public static int getScore() {
        return score;
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && DungeonUtil.isDungeonStarted()) {
            int totalRooms = getTotalRooms();
            int clearedRooms = getTotalClearedRooms();
            double secretsFound = getSecretsFound();
            double secretsNeeded = getSecretsNeeded();
            score = getSkillScore(clearedRooms, totalRooms)
                    + getExploreScore(clearedRooms, totalRooms, secretsFound, secretsNeeded)
                    + getSpeedScore()
                    + getBonusScore();
            if (score >= 300 && !sent300) {
                processAlert(sendMsg300, msg300, showTitle300, title300);
                sent300 = true;
                sent270 = true;
            }
            if (score >= 270 && !sent270) {
                processAlert(sendMsg270, msg270, showTitle270, title270);
                sent270 = true;
            }
        }
    }

    @EventHandler
    private static void onMsg(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            String msg = event.messagePlain.trim();
            if (msg.equals("[BOSS] The Watcher: You have proven yourself. You may pass.")) {
                bloodDone = true;
                return;
            }
            if (msg.startsWith(Utils.Symbols.skull)) {
                if (event.messagePlain.endsWith(" ghost.") || event.messagePlain.endsWith(" died.")) {
                    deaths += 1;
                }
            }
        }
    }

    @EventHandler
    private static void onPartyMsg(PartyChatMsgEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            String msg = Utils.toLower(event.message);
            if (msg.contains("kill") || msg.contains("dead")) {
                if (msg.contains("mimic")) {
                    mimicKilled();
                } else if (msg.contains("prince")) {
                    princeKilled();
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        score = 0;
        deaths = 0;
        bloodDone = false;
        mimic = false;
        prince = false;
        sent270 = false;
        sent300 = false;
    }

    public enum PaulState {
        Auto,
        Active,
        Inactive
    }
}
