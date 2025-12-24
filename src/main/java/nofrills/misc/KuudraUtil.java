package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.util.math.Vec3d;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;

import java.util.List;

public class KuudraUtil {
    public static final List<PickupSpot> pickupSpots = List.of( // data borrowed from odin
            new PickupSpot("X", new Vec3d(-142.5, 77.0, -151.0), 18, 30,
                    new PickupSpot("X Cannon", new Vec3d(-143.0, 76.0, -125.0), 16, 0, null)),
            new PickupSpot("Triangle", new Vec3d(-67.5, 77.0, -122.5), 18, 15,
                    new PickupSpot("Shop", new Vec3d(-81.0, 76.0, -143.0), 18, 0, null)),
            new PickupSpot("Slash", new Vec3d(-113.5, 77.0, -68.5), 18, 15,
                    new PickupSpot("Square", new Vec3d(-143.0, 76.0, -80.0), 20, 0, null)),
            new PickupSpot("Equals", new Vec3d(-65.5, 76.0, -87.5), 18, 15, null)
    );
    private static final List<String> dpsLines = List.of(
            "Figure out how to",
            "Charge Ballista or",
            "Shoot Ballista at",
            "Mine Kuudra's pods"
    );
    private static MagmaCubeEntity kuudraEntity = null;

    public static phase getCurrentPhase() {
        if (Utils.isInZone(-133, 59, -75, -73, 1, -138)) {
            return phase.Lair;
        }
        for (String line : SkyblockData.getLines()) {
            if (line.startsWith("Rescue supplies")) {
                return phase.Collect;
            }
            if (line.startsWith("Protect Elle")) {
                return phase.Build;
            }
            for (String phaseLine : dpsLines) {
                if (line.startsWith(phaseLine)) {
                    return phase.DPS;
                }
            }
        }
        return phase.Starting;
    }

    public static MagmaCubeEntity getKuudraEntity() {
        return kuudraEntity;
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (Utils.isInKuudra() && !EntityCache.exists(kuudraEntity)) {
            MagmaCubeEntity kuudra = null;
            double maxY = 0;
            int cubesFound = 0;
            for (Entity ent : Utils.getEntities()) {
                if (ent instanceof MagmaCubeEntity cube && cube.getSize() == 30) {
                    double y = ent.getPos().getY();
                    cubesFound++;
                    if (y > maxY) {
                        kuudra = cube;
                        maxY = y;
                    }
                }
            }
            if (kuudra == null) {
                kuudraEntity = null;
                return;
            }
            if (cubesFound == 2 || getCurrentPhase().equals(phase.Lair)) {
                kuudraEntity = kuudra;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        kuudraEntity = null;
    }

    public enum phase {
        Starting,
        Collect,
        Build,
        DPS,
        Lair
    }

    public static class PickupSpot {
        public String name;
        public Vec3d spot;
        public double supplyDist;
        public double playerDist;
        public PickupSpot secondary;

        PickupSpot(String name, Vec3d spot, double supplyDist, double playerDist, PickupSpot secondary) {
            this.name = name;
            this.spot = spot;
            this.supplyDist = supplyDist;
            this.playerDist = playerDist;
            this.secondary = secondary;
        }
    }
}
