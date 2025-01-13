package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.util.Formatting;
import nofrills.events.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class SkyblockData {
    public static final InstanceType[] instances = {
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
            new InstanceType("k5", "KUUDRA_INFERNAL")
    };
    private static final Pattern scoreRegex = Pattern.compile("Team Score: [0-9]* (.*)");
    private static String location = "";
    private static String area = "";
    private static boolean inSkyblock = false;
    private static boolean instanceOver = false;
    private static List<String> lines = new ArrayList<>();

    /*
        Returns the current location from the scoreboard, such as "⏣ Your Island". The location prefix is not omitted.
    */
    public static String getLocation() {
        return location;
    }

    /*
        Returns the current area from the tab list, such as "Area: Private Island". The area/dungeon prefix is omitted.
    */
    public static String getArea() {
        return area;
    }

    public static boolean isInSkyblock() {
        return inSkyblock;
    }

    public static boolean isInstanceOver() {
        return instanceOver;
    }

    /*
        Returns a list with every line that is currently displayed on the scoreboard.
    */
    public static List<String> getLines() {
        return new ArrayList<>(lines); // return a copy to avoid a potential concurrent modification exception
    }

    @EventHandler
    public static void onTabList(TabListUpdateEvent event) {
        for (PlayerListS2CPacket.Entry entry : event.entries) {
            String name = Formatting.strip(entry.displayName().getString()).trim();
            if (name.startsWith("Area:") || name.startsWith("Dungeon:")) {
                area = name.split(":", 2)[1].trim();
                break;
            }
        }
    }

    @EventHandler
    public static void onTabList(ObjectiveUpdateEvent event) {
        Scoreboard scoreboard = mc.player.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
        if (objective != null) {
            inSkyblock = Formatting.strip(objective.getDisplayName().getString()).contains("SKYBLOCK");
        } else {
            inSkyblock = false;
        }
    }

    @EventHandler
    public static void onScoreboard(ScoreboardUpdateEvent event) {
        List<String> currentLines = new ArrayList<>();
        Scoreboard scoreboard = mc.player.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
        for (ScoreHolder scoreHolder : scoreboard.getKnownScoreHolders()) {
            if (scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) {
                Team team = scoreboard.getScoreHolderTeam(scoreHolder.getNameForScoreboard());
                if (team != null) {
                    String line = team.getPrefix().getString() + team.getSuffix().getString();
                    if (!line.trim().isEmpty()) {
                        String cleanLine = Formatting.strip(line).trim();
                        if (cleanLine.startsWith(Utils.Symbols.zone) || cleanLine.startsWith(Utils.Symbols.zoneRift)) {
                            location = cleanLine;
                        }
                        currentLines.add(cleanLine);
                    }
                }
            }
        }
        lines = currentLines;
        if (Utils.isInKuudra() && !instanceOver) {
            instanceOver = getLines().stream().anyMatch(line -> line.startsWith("Instance Shutdown"));
        }
    }

    @EventHandler
    public static void onChat(ChatMsgEvent event) {
        if (Utils.isInDungeons() && !instanceOver) {
            if (scoreRegex.matcher(event.messagePlain.trim()).matches()) {
                instanceOver = true;
            }
        }
    }

    @EventHandler
    public static void onJoinServer(ServerJoinEvent event) {
        if (instanceOver) {
            instanceOver = false;
        }
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
