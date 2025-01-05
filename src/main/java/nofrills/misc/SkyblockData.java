package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.util.Formatting;
import nofrills.events.ObjectiveUpdateEvent;
import nofrills.events.ScoreboardUpdateEvent;
import nofrills.events.TabListUpdateEvent;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class SkyblockData {
    private static String location = "";
    private static String area = "";
    private static boolean inSkyblock = false;
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
    }
}
