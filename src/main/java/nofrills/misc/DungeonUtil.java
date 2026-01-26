package nofrills.misc;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BatEntity;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;

import java.util.HashMap;
import java.util.HashSet;

import static nofrills.Main.mc;

public class DungeonUtil {
    private static final HashMap<String, String> classCache = new HashMap<>();
    private static final MapIdComponent mapId = new MapIdComponent(1024);
    private static final HashSet<String> dungeonClasses = Sets.newHashSet(
            "Healer",
            "Mage",
            "Berserk",
            "Archer",
            "Tank"
    );
    private static String currentFloor = "";
    private static int partyCount = 0;

    public static HashMap<String, String> getClassCache() {
        return classCache;
    }

    public static HashSet<String> getDungeonClasses() {
        return dungeonClasses;
    }

    public static boolean isDungeonStarted() {
        return mc.world != null && mc.world.getMapState(mapId) != null;
    }

    public static boolean isInDragonPhase() {
        return mc.player != null && mc.player.getEntityPos().getY() < 50 && Utils.isInDungeonBoss("7");
    }

    public static boolean isInBossRoom() {
        return Utils.isInDungeonBoss(currentFloor);
    }

    public static String getCurrentFloor() {
        return currentFloor;
    }

    public static boolean isSecretBat(Entity entity) {
        if (entity instanceof BatEntity bat) {
            return Utils.isBaseHealth(bat, 100.0f) && !Utils.isInDungeonBoss("4");
        }
        return false;
    }

    public static String getPlayerClass(String name) {
        return classCache.getOrDefault(name, "");
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (Utils.isInDungeons()) {
            if (currentFloor.isEmpty() && SkyblockData.getLocation().contains("The Catacombs (")) {
                String location = SkyblockData.getLocation();
                currentFloor = location.substring(location.indexOf("(") + 1, location.indexOf(")"));
            }
            if ((partyCount == 0 || classCache.size() != partyCount) && isDungeonStarted()) {
                for (String line : Utils.getTabListLines()) {
                    if (line.startsWith("Party (") && line.endsWith(")")) {
                        String count = line.substring(line.indexOf("(") + 1).replace(")", "");
                        partyCount = Utils.parseInt(count).orElse(0);
                    } else {
                        for (String dungeonClass : dungeonClasses) {
                            if (line.contains("(" + dungeonClass) && line.endsWith(")")) {
                                int start = line.lastIndexOf("]") + 2;
                                String name = line.substring(start, line.indexOf(" ", start));
                                classCache.put(name, dungeonClass);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        classCache.clear();
        currentFloor = "";
        partyCount = 0;
    }
}
