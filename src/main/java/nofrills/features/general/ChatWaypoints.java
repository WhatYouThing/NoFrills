package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingInt;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static nofrills.Main.mc;

public class ChatWaypoints {
    public static final Feature instance = new Feature("chatWaypoints");

    public static final SettingBool partyWaypoints = new SettingBool(false, "partyEnabled", instance.key());
    public static final SettingInt partyDuration = new SettingInt(120, "partyDuration", instance.key());
    public static final SettingBool partyClear = new SettingBool(false, "partyClearOnArrive", instance.key());
    public static final SettingColor partyColor = new SettingColor(RenderColor.fromArgb(0xaa5555ff), "partyColor", instance.key());
    public static final SettingBool allWaypoints = new SettingBool(false, "allEnabled", instance.key());
    public static final SettingInt allDuration = new SettingInt(60, "allDuration", instance.key());
    public static final SettingBool allClear = new SettingBool(false, "allClearOnArrive", instance.key());
    public static final SettingColor allColor = new SettingColor(RenderColor.fromArgb(0xaa55ffff), "allColor", instance.key());

    private static final List<PlayerWaypoint> waypointList = new ArrayList<>();

    private static boolean isPlayerValid(String name) {
        if (mc.player != null && mc.player.getName().getString().equals(name)) {
            return false;
        }
        return mc.getNetworkHandler() != null && mc.getNetworkHandler().getPlayerListEntry(name) != null;
    }

    private static void highlightCoords(String message, String sender, boolean party) {
        List<Double> coords = new ArrayList<>();
        boolean skipNextError = false;
        for (String coord : message.split(" ")) {
            if (coord.endsWith(",") && coord.indexOf(",") == coord.lastIndexOf(",")) { // remove comma from patcher format
                coord = coord.replace(",", "");
                skipNextError = true;
            }
            try {
                coords.add(Double.parseDouble(coord));
            } catch (NumberFormatException ignored) {
                if (!skipNextError) {
                    coords.clear();
                }
                skipNextError = false;
            }
            if (coords.size() == 3) {
                BlockPos pos = new BlockPos(
                        (int) Math.floor(coords.getFirst()),
                        (int) Math.floor(coords.get(1)),
                        (int) Math.floor(coords.get(2))
                );
                if (pos.getY() < 0 || pos.getY() > 256) {
                    break;
                }
                int duration = party ? partyDuration.value() * 20 : allDuration.value() * 20;
                waypointList.removeIf(waypoint -> waypoint.name.getString().equals(sender));
                waypointList.add(new PlayerWaypoint(sender, pos, duration, party));
                break;
            }
        }
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        String msg = event.getPlainMessage();
        if (instance.isActive() && allWaypoints.value()) {
            if (msg.startsWith("[NPC]") || msg.startsWith("[BOSS]") || msg.startsWith("Guild > ") || msg.startsWith("Party > ")) {
                return;
            }
            int msgStart = msg.indexOf(":");
            if (msgStart != -1) {
                String senderInfo = msg.substring(0, msgStart);
                String sender = senderInfo.contains(" ") ? Arrays.stream(senderInfo.split(" ")).toList().getLast().trim() : senderInfo;
                if (isPlayerValid(sender)) {
                    highlightCoords(msg.substring(msgStart), sender, false);
                }
            }
        }
    }

    @EventHandler
    private static void onPartyMessage(PartyChatMsgEvent event) {
        if (instance.isActive() && partyWaypoints.value() && !event.self) {
            highlightCoords(event.message, event.sender, true);
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && !waypointList.isEmpty()) {
            for (PlayerWaypoint waypoint : new ArrayList<>(waypointList)) {
                if (waypoint.duration > 0) {
                    waypoint.duration--;
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !waypointList.isEmpty()) {
            List<PlayerWaypoint> waypoints = new ArrayList<>(waypointList);
            for (PlayerWaypoint waypoint : waypoints) {
                if (waypoint.duration == 0) {
                    waypointList.remove(waypoint);
                    continue;
                }
                if (waypoint.shouldClear() && waypoint.box.getCenter().distanceTo(mc.player.getPos()) <= 8.0) {
                    waypointList.remove(waypoint);
                    continue;
                }
                RenderColor color = waypoint.party ? partyColor.value() : allColor.value();
                event.drawFilledWithBeam(waypoint.box, 256, true, color);
                event.drawText(waypoint.box.getCenter().add(0, 1, 0), waypoint.name, 0.05f, true, RenderColor.white);
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (!waypointList.isEmpty()) {
            waypointList.clear();
        }
    }

    private static class PlayerWaypoint {
        public Text name;
        public Box box;
        public int duration;
        public boolean party;

        public PlayerWaypoint(String name, BlockPos pos, int duration, boolean party) {
            this.name = Text.literal(name);
            this.box = Box.enclosing(pos, pos);
            this.duration = duration;
            this.party = party;
        }

        public boolean shouldClear() {
            return this.party ? partyClear.value() : allClear.value();
        }
    }
}
