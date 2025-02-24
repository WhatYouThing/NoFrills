package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Config;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class PartyFeatures {
    private static final Pattern partyFinderPattern = Pattern.compile("Party Finder > .* joined .*");
    private static final Config.partyBehaviorList auto = Config.partyBehaviorList.Automatic;
    private static final Config.partyBehaviorList ignore = Config.partyBehaviorList.Ignore;
    private static final Config.partyBehaviorList disabled = Config.partyBehaviorList.Disabled;
    private static final List<PlayerWaypoint> waypointList = new ArrayList<>();
    private static boolean downtimeNeeded = false;

    private static void setDowntimeReminder(String sender) {
        if (!Utils.isInstanceOver() && (Utils.isInDungeons() || Utils.isInKuudra())) {
            Utils.info("§aScheduled downtime reminder for §6" + sender + "§a.");
            downtimeNeeded = true;
        } else {
            Utils.showTitle("§6§lDOWNTIME", "", 5, 40, 5);
        }
    }

    private static boolean isCharValid(String character) {
        return Character.isDigit(character.charAt(0)) || character.equals("-") || character.equals(".");
    }

    private static void highlightCoords(String message, String sender) {
        StringBuilder lastCoord = new StringBuilder();
        int lastCoordEnd = -1;
        List<Double> coords = new ArrayList<>();
        for (int i = 0; i < message.length(); i++) {
            if (lastCoordEnd != -1 && i > lastCoordEnd + 6) {
                coords.clear();
                lastCoordEnd = -1;
                lastCoord = new StringBuilder();
            }
            char character = message.charAt(i);
            String charString = Character.toString(character);
            if (isCharValid(charString)) {
                lastCoord.append(charString);
                lastCoordEnd = -1;
            }
            if (i < message.length() - 1) {
                if (isCharValid(Character.toString(message.charAt(i + 1)))) {
                    continue;
                }
            }
            if (!lastCoord.isEmpty()) {
                try {
                    double coord = Double.parseDouble(lastCoord.toString());
                    coords.add(coord);
                } catch (NumberFormatException e) {
                    coords.clear();
                }
                lastCoordEnd = i;
                lastCoord = new StringBuilder();
            }
        }
        if (coords.size() == 3) {
            int x = (int) Math.floor(coords.getFirst()), y = (int) Math.floor(coords.get(1)), z = (int) Math.floor(coords.get(2));
            waypointList.removeIf(waypoint -> waypoint.name.equals(sender));
            waypointList.add(new PlayerWaypoint(sender, new BlockPos(x, y, z), Config.partyWaypointTime * 20));
        }
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        String msg = event.getPlainMessage();
        if (Config.partyFinderOptions && msg.startsWith("Party Finder >") && partyFinderPattern.matcher(msg).matches()) {
            String name = msg.replace("Party Finder >", "").trim().split(" ", 2)[0].toLowerCase();
            if (name.equalsIgnoreCase(mc.getSession().getUsername())) {
                return;
            }
            ClickEvent copyName = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, name);
            ClickEvent kick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party kick " + name);
            ClickEvent ignoreAdd = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ignore add " + name);
            Text message = Text.literal("§aOptions for §6" + name + "§a: ")
                    .append(Text.literal("§b§l[COPY NAME]").setStyle(Style.EMPTY.withClickEvent(copyName)))
                    .append(Text.literal(" "))
                    .append(Text.literal("§c§l[KICK]").setStyle(Style.EMPTY.withClickEvent(kick)))
                    .append(Text.literal(" "))
                    .append(Text.literal("§e§l[IGNORE ADD]").setStyle(Style.EMPTY.withClickEvent(ignoreAdd)));
            Utils.infoRaw(message);
        }
    }

    @EventHandler
    private static void onPartyMessage(PartyChatMsgEvent event) {
        if (Config.partyWaypoints && !event.self) {
            highlightCoords(event.message, event.sender);
        }
        if (!Config.partyPrefixes.isEmpty() && !event.self) {
            String msg = event.message.toLowerCase();
            String author = event.sender.toLowerCase();
            for (String prefix : Config.partyPrefixes.split(" ")) {
                if (msg.startsWith(prefix.toLowerCase())) {
                    if (Config.partyBlacklist.contains(author)) {
                        return;
                    }
                    boolean whitelisted = Config.partyWhitelist.contains(author);
                    String command = msg.replace(prefix, "");
                    if (Config.partyCmdWarp != disabled && command.startsWith("warp")) {
                        if (whitelisted || Config.partyCmdWarp == auto) {
                            Utils.sendMessage("/party warp");
                        } else if (Config.partyCmdWarp != ignore) {
                            Utils.infoButton("§aClick here to warp your party.", "/party warp");
                        }
                    }
                    if (Config.partyCmdTransfer != disabled && command.startsWith("ptme")) {
                        if (whitelisted || Config.partyCmdTransfer == auto) {
                            Utils.sendMessage("/party transfer " + author);
                        } else if (Config.partyCmdTransfer != ignore) {
                            Utils.infoButton("§aClick here to promote §6" + event.sender + " §aas leader.", "/party transfer " + author);
                        }
                    }
                    if (Config.partyCmdAllInvite != disabled && command.startsWith("allinv")) {
                        if (whitelisted || Config.partyCmdAllInvite == auto) {
                            Utils.sendMessage("/party settings allinvite");
                        } else if (Config.partyCmdAllInvite != ignore) {
                            Utils.infoButton("§aClick here to toggle all invite.", "/party settings allinvite");
                        }
                    }
                    if (Config.partyCmdDowntime != disabled && command.startsWith("dt")) {
                        if (whitelisted || Config.partyCmdDowntime == auto) {
                            setDowntimeReminder(event.sender);
                        }
                    }
                    if (Config.partyCmdQueue != disabled) {
                        for (SkyblockData.InstanceType instance : SkyblockData.instances) {
                            if (command.equals(instance.name)) {
                                if (whitelisted || Config.partyCmdQueue == auto) {
                                    Utils.sendMessage("/joininstance " + instance.type);
                                } else if (Config.partyCmdQueue != ignore) {
                                    Utils.infoButton("§aClick to queue for §6" + Utils.uppercaseFirst(instance.type.toLowerCase(), true) + "§a.", "/joininstance " + instance.type);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (downtimeNeeded) {
            if (Utils.isInDungeons() || Utils.isInKuudra()) {
                if (Utils.isInstanceOver()) {
                    Utils.showTitle("§6§lDOWNTIME", "", 5, 80, 5);
                    downtimeNeeded = false;
                }
            } else {
                downtimeNeeded = false;
            }
        }
        if (!waypointList.isEmpty()) {
            if (!Config.partyWaypoints) {
                waypointList.clear();
            } else {
                for (PlayerWaypoint waypoint : waypointList) {
                    if (waypoint.duration > 0) {
                        waypoint.duration--;
                    }
                }
                waypointList.removeIf(waypoint -> waypoint.duration == 0);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (!waypointList.isEmpty()) {
            List<PlayerWaypoint> waypoints = new ArrayList<>(waypointList);
            for (PlayerWaypoint waypoint : waypoints) {
                event.drawFilled(waypoint.box, true, RenderColor.fromColor(Config.partyWaypointColor));
                event.drawText(waypoint.box.getCenter().add(0, 1, 0), Text.of(waypoint.name), 0.05f, true, RenderColor.fromHex(0xffffff));
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
        public String name;
        public Box box;
        public int duration;

        public PlayerWaypoint(String name, BlockPos pos, int duration) {
            this.name = name;
            this.box = Box.enclosing(pos, pos);
            this.duration = duration;
        }
    }
}
