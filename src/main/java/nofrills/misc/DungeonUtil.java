package nofrills.misc;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.type.MapIdComponent;
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

    public static String getPlayerClass(String name) {
        return classCache.getOrDefault(name, "");
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (Utils.isInDungeons() && (partyCount == 0 || classCache.size() != partyCount) && isDungeonStarted()) {
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

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        classCache.clear();
        partyCount = 0;
    }
}
