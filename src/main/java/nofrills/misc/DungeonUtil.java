package nofrills.misc;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static nofrills.Main.mc;

@EventListener
public class DungeonUtil {
    private static final HashMap<String, String> classCache = new HashMap<>();
    private static final MapId mapId = new MapId(1024);
    private static final HashSet<String> dungeonClasses = Sets.newHashSet(
            "Healer",
            "Mage",
            "Berserk",
            "Archer",
            "Tank"
    );
    private static final HashSet<String> chestNames = Sets.newHashSet(
            "Wood",
            "Gold",
            "Diamond",
            "Emerald",
            "Obsidian",
            "Bedrock"
    );
    private static List<AbstractClientPlayer> teammateEntities = List.of();
    private static String currentFloor = "";
    private static int partyCount = 0;
    private static double powerLevel = 0;

    public static HashMap<String, String> getClassCache() {
        return classCache;
    }

    public static List<Teammate> getAliveTeammates(boolean excludeSelf) {
        List<Teammate> list = new ArrayList<>();
        String playerName = mc.player.getName().getString();
        for (String line : Utils.getTabListLines()) {
            if (!line.endsWith(")")) {
                continue;
            }
            for (String dungeonClass : dungeonClasses) {
                if (line.contains("(" + dungeonClass)) {
                    int start = line.lastIndexOf("]") + 2;
                    String name = line.substring(start, line.indexOf(" ", start));
                    if (excludeSelf && name.equalsIgnoreCase(playerName)) {
                        break;
                    }
                    list.add(new Teammate(name, dungeonClass));
                }
            }
        }
        return list;
    }

    public static List<Teammate> getAliveTeammates() {
        return getAliveTeammates(false);
    }

    public static boolean isDungeonStarted() {
        return getMap() != null;
    }

    public static boolean isInDragonPhase() {
        return mc.player != null && mc.player.position().y() < 50 && isOnFloor("7") && isInBossRoom();
    }

    public static boolean isInBossRoom(String floor) {
        if (mc.player == null) return false;
        Vec3 pos = mc.player.position();
        return isOnFloor(floor) && switch (floor.length() == 2 ? floor.substring(1, 2) : floor) {
            case "1" -> new AABB(-72, 146, -40, -14, 55, 49).contains(pos);
            case "2" -> new AABB(-40, 99, -40, 24, 54, 54).contains(pos);
            case "3" -> new AABB(-40, 118, -40, 42, 64, 73).contains(pos);
            case "4" -> new AABB(50, 112, 81, -40, 53, -40).contains(pos);
            case "5" -> new AABB(50, 112, 118, -40, 53, -8).contains(pos);
            case "6" -> new AABB(22, 110, 134, -40, 51, -8).contains(pos);
            case "7" -> new AABB(134, 254, 147, -8, 0, -8).contains(pos);
            default -> false;
        };
    }

    public static boolean isInBossRoom() {
        return isInBossRoom(currentFloor);
    }

    public static String getCurrentFloor() {
        return currentFloor;
    }

    public static HashSet<String> getChestNames() {
        return chestNames;
    }

    public static boolean isOnFloor(String floor) {
        return getCurrentFloor().endsWith(floor);
    }

    public static boolean isClass(String dungeonClass) {
        return mc.player != null && getPlayerClass(mc.player.getName().getString()).equalsIgnoreCase(dungeonClass);
    }

    public static double getPower() {
        return powerLevel;
    }

    public static boolean isSecretBat(Entity entity) {
        if (entity instanceof Bat bat) {
            return Utils.isBaseHealth(bat, 100.0f) && !isInBossRoom("4");
        }
        return false;
    }

    public static String getPlayerClass(String name) {
        return classCache.getOrDefault(name, "");
    }

    public static String getPlayerClass() {
        return mc.player != null ? getPlayerClass(mc.player.getName().getString()) : "";
    }

    public static MapItemSavedData getMap() {
        return mc.level != null ? mc.level.getMapData(mapId) : null;
    }

    public static MapId getMapId() {
        return mapId;
    }

    public static List<AbstractClientPlayer> getTeammateEntities() {
        return teammateEntities;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onTick(WorldTickEvent event) {
        if (mc.level != null && Utils.isInDungeons()) {
            if (currentFloor.isEmpty()) {
                String location = SkyblockData.getLocation();
                if (location.contains("The Catacombs (")) {
                    currentFloor = location.substring(location.indexOf("(") + 1, location.indexOf(")"));
                }
            }
            if ((partyCount == 0 || classCache.size() != partyCount) && isDungeonStarted()) {
                for (String line : Utils.getTabListLines()) {
                    if (line.startsWith("Party (") && line.endsWith(")")) {
                        String count = line.substring(line.indexOf("(") + 1).replace(")", "");
                        partyCount = Utils.parseInt(count).orElse(0);
                        break;
                    }
                }
                for (Teammate teammate : getAliveTeammates()) {
                    classCache.put(teammate.name, teammate.selectedClass);
                }
            }
            double power = 0;
            for (String line : Utils.getFooterLines()) {
                if (line.startsWith("Blessing of Power")) {
                    power += Utils.parseRoman(line.replace("Blessing of Power", "").trim());
                }
                if (line.startsWith("Blessing of Time")) {
                    power += 0.5 * Utils.parseRoman(line.replace("Blessing of Time", "").trim());
                }
            }
            powerLevel = power;
            List<AbstractClientPlayer> teammates = new ArrayList<>();
            for (AbstractClientPlayer player : mc.level.players()) {
                if (Utils.isPlayer(player) && !getPlayerClass(player.getName().getString()).isEmpty()) {
                    teammates.add(player);
                }
            }
            teammateEntities = teammates;
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        classCache.clear();
        teammateEntities = new ArrayList<>();
        currentFloor = "";
        partyCount = 0;
        powerLevel = 0.0;
    }

    public record Teammate(String name, String selectedClass) {
    }
}
