package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ReceivePacketEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class SkyblockData {
    public static final List<InstanceType> instances = List.of(
            new InstanceType("f0", "CATACOMBS_ENTRANCE"),
            new InstanceType("f1", "CATACOMBS_FLOOR_ONE"),
            new InstanceType("f2", "CATACOMBS_FLOOR_TWO"),
            new InstanceType("f3", "CATACOMBS_FLOOR_THREE"),
            new InstanceType("f4", "CATACOMBS_FLOOR_FOUR "),
            new InstanceType("f5", "CATACOMBS_FLOOR_FIVE"),
            new InstanceType("f6", "CATACOMBS_FLOOR_SIX"),
            new InstanceType("f7", "CATACOMBS_FLOOR_SEVEN"),
            new InstanceType("m1", "MASTER_CATACOMBS_FLOOR_ONE"),
            new InstanceType("m2", "MASTER_CATACOMBS_FLOOR_TWO"),
            new InstanceType("m3", "MASTER_CATACOMBS_FLOOR_THREE"),
            new InstanceType("m4", "MASTER_CATACOMBS_FLOOR_FOUR"),
            new InstanceType("m5", "MASTER_CATACOMBS_FLOOR_FIVE"),
            new InstanceType("m6", "MASTER_CATACOMBS_FLOOR_SIX"),
            new InstanceType("m7", "MASTER_CATACOMBS_FLOOR_SEVEN"),
            new InstanceType("k1", "KUUDRA_NORMAL"),
            new InstanceType("k2", "KUUDRA_HOT"),
            new InstanceType("k3", "KUUDRA_BURNING"),
            new InstanceType("k4", "KUUDRA_FIERY"),
            new InstanceType("k5", "KUUDRA_INFERNAL"),
            new InstanceType("t1", "KUUDRA_NORMAL"),
            new InstanceType("t2", "KUUDRA_HOT"),
            new InstanceType("t3", "KUUDRA_BURNING"),
            new InstanceType("t4", "KUUDRA_FIERY"),
            new InstanceType("t5", "KUUDRA_INFERNAL")
    );
    private static final Pattern scoreRegex = Pattern.compile("Team Score: [0-9]* (.*)");
    private static String location = "";
    private static String area = "";
    private static boolean inSkyblock = false;
    private static boolean instanceOver = false;
    private static List<String> tabListLines = new ArrayList<>();
    private static List<String> lines = new ArrayList<>();
    private static boolean showPing = false;
    private static boolean tabListDirty = true;
    private static boolean scoreboardDirty = true;

    /**
     * Returns the current location from the scoreboard, such as "⏣ Your Island". The location prefix is not omitted.
     */
    public static String getLocation() {
        return location;
    }

    /**
     * Returns the current area from the tab list, such as "Area: Private Island". The area/dungeon prefix is omitted.
     */
    public static String getArea() {
        return area;
    }

    public static boolean isInSkyblock() {
        return inSkyblock;
    }

    public static boolean isInInstance() {
        return Utils.isInDungeons() || Utils.isInKuudra();
    }

    public static boolean isInstanceOver() {
        return instanceOver;
    }

    public static List<String> getTabListLines() {
        return tabListLines;
    }

    public static List<String> getLines() {
        return lines;
    }

    public static void showPing() {
        showPing = true;
        Utils.sendPingPacket();
    }

    public static void markTabListDirty() {
        tabListDirty = true;
    }

    private static void updateTabListIfDirty() {
        List<String> lines = new ArrayList<>();
        for (PlayerListEntry entry : mc.inGameHud.getPlayerListHud().collectPlayerEntries()) {
            if (entry != null && entry.getDisplayName() != null) {
                String name = Utils.toPlain(entry.getDisplayName()).trim();
                if (name.isEmpty()) continue;
                if (name.startsWith("Area: ") || name.startsWith("Dungeon: ")) {
                    area = name.split(":", 2)[1].trim();
                }
                lines.add(name);
            }
        }
        tabListLines = lines;
    }

    private static void updateTabList() {
        if (tabListDirty) {
            updateTabListIfDirty();
            tabListDirty = false;
        }
    }

    public static void updateObjective() {
        if (mc.player != null) {
            Scoreboard scoreboard = mc.player.networkHandler.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
            if (objective != null) {
                inSkyblock = Utils.toPlain(objective.getDisplayName()).contains("SKYBLOCK");
            }
        }
    }

    public static void markScoreboardDirty() {
        scoreboardDirty = true;
    }

    private static void updateScoreboardIfDirty() {
        if (mc.player != null) {
            List<String> currentLines = new ArrayList<>();
            Scoreboard scoreboard = mc.player.networkHandler.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
            for (ScoreHolder scoreHolder : scoreboard.getKnownScoreHolders()) {
                if (scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) {
                    Team team = scoreboard.getScoreHolderTeam(scoreHolder.getNameForScoreboard());
                    if (team != null) {
                        String line = Formatting.strip(team.getPrefix().getString() + team.getSuffix().getString()).trim();
                        if (!line.isEmpty()) {
                            if (line.startsWith(Utils.Symbols.zone) || line.startsWith(Utils.Symbols.zoneRift)) {
                                location = line;
                            }
                            if (Utils.isInKuudra() && !instanceOver) {
                                instanceOver = line.startsWith("Instance Shutdown");
                            }
                            currentLines.add(line);
                        }
                    }
                }
            }
            lines = currentLines;
            SlayerUtil.updateQuestState(currentLines);
        }
    }

    private static void updateScoreboard() {
        if (scoreboardDirty) {
            updateScoreboardIfDirty();
            scoreboardDirty = false;
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (Utils.isInDungeons()) {
            if (!instanceOver && scoreRegex.matcher(event.messagePlain.trim()).matches()) {
                instanceOver = true;
            }
        }
    }

    @EventHandler
    private static void onJoinServer(ServerJoinEvent event) {
        instanceOver = false;
        inSkyblock = false;
        location = "";
        area = "";
        lines.clear();
    }

    @EventHandler
    private static void onPing(ReceivePacketEvent event) {
        if (showPing && event.packet instanceof PingResultS2CPacket pingPacket) {
            Utils.infoFormat("§aPing: §f{}ms", Util.getMeasuringTimeMs() - pingPacket.startTime());
            showPing = false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onWorldTick(WorldTickEvent event) {
        updateTabList();
        updateScoreboard();
    }

    public static class InstanceType {
        public String name;
        public String type;

        public InstanceType(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
}
