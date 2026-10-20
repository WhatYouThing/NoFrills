package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.util.Util;
import net.minecraft.world.scores.*;
import nofrills.events.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

@EventListener
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
    private static final Pattern dungeonScoreRegex = Pattern.compile("Team Score: [0-9]* (.*)");
    private static final Pattern kuudraTokensRegex = Pattern.compile("Tokens Earned: [0-9]*");
    private static String location = "";
    private static String area = "";
    private static boolean inSkyblock = false;
    private static boolean instanceOver = false;
    private static List<String> tabListLines = new ArrayList<>();
    private static List<String> lines = new ArrayList<>();
    private static boolean showPing = false;
    private static boolean tabListDirty = true;
    private static boolean scoreboardDirty = true;
    private static PerkpocalypsePerks currentPerks = PerkpocalypsePerks.EMPTY;

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

    public static PerkpocalypsePerks getPerkpocalypsePerks() {
        return currentPerks;
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
        for (PlayerInfo entry : mc.gui.hud.getTabList().getPlayerInfos()) {
            if (entry.getTabListDisplayName() != null) {
                String name = Utils.toPlain(entry.getTabListDisplayName()).trim();
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
            Scoreboard scoreboard = mc.player.connection.scoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BY_ID.apply(1));
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
            Scoreboard scoreboard = mc.player.connection.scoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BY_ID.apply(1));
            for (ScoreHolder scoreHolder : scoreboard.getTrackedPlayers()) {
                if (scoreboard.listPlayerScores(scoreHolder).containsKey(objective)) {
                    PlayerTeam team = scoreboard.getPlayersTeam(scoreHolder.getScoreboardName());
                    if (team != null) {
                        String line = ChatFormatting.stripFormatting(team.getPlayerPrefix().getString() + team.getPlayerSuffix().getString()).trim();
                        if (!line.isEmpty()) {
                            if (line.startsWith(Utils.Symbols.zone) || line.startsWith(Utils.Symbols.zoneRift)) {
                                location = line;
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
        if (!instanceOver) {
            if (Utils.isInDungeons() && dungeonScoreRegex.matcher(event.messagePlain.trim()).matches()) {
                instanceOver = true;
            } else if (Utils.isInKuudra() && kuudraTokensRegex.matcher(event.messagePlain.trim()).matches()) {
                instanceOver = true;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onJoinServer(ServerJoinEvent event) {
        instanceOver = false;
        inSkyblock = false;
        location = "";
        area = "";
        lines.clear();
    }

    @EventHandler
    private static void onPing(ReceivePacketEvent event) {
        if (showPing && event.packet instanceof ClientboundPongResponsePacket(long time)) {
            long ping = Util.getMillis() - time;
            Utils.infoRaw(Component.literal("Ping: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(ping + "ms").withColor(Utils.getPercentageColor(ping / 300.0, true).getHex()))
            );
            showPing = false;
        }
    }

    @EventHandler
    private static void onSlot(SlotUpdateEvent event) {
        if (event.title.equals("Calendar and Events") && Utils.toPlain(event.stack.getHoverName()).equals("Mayor Jerry")) {
            List<Component> lore = Utils.getLoreText(event.stack);
            Optional<Component> perkStart = lore.stream()
                    .filter(c -> Utils.toPlain(c).trim().equals("Perkpocalypse Perks:"))
                    .findFirst();
            if (perkStart.isEmpty()) return;
            Matcher matcher = Utils.pattern("Next set of perks in (?<time>.*)!").matcher(Utils.toPlain(lore.getLast()));
            if (!matcher.matches()) return;
            int startIndex = lore.indexOf(perkStart.get()) + 2;
            Style perkStyle = lore.get(startIndex).getStyle();
            List<String> perkList = lore.stream()
                    .filter(c -> lore.indexOf(c) >= startIndex && c.getStyle().equals(perkStyle) && c.getSiblings().isEmpty())
                    .map(Utils::toPlain)
                    .filter(s -> !s.isBlank())
                    .toList();
            currentPerks = new PerkpocalypsePerks(perkList, Utils.parseTime(matcher.group("time")).getTimeInMillis());
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

    public static class PerkpocalypsePerks {
        public static final PerkpocalypsePerks EMPTY = new PerkpocalypsePerks(List.of(), 0L);

        public List<String> perks;
        public long until;

        public PerkpocalypsePerks(List<String> perks, long until) {
            this.perks = perks;
            this.until = until;
        }

        public boolean areActive() {
            return this.until > Instant.now().toEpochMilli();
        }
    }
}
