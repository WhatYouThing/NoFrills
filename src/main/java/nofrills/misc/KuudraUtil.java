package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.util.math.Vec3d;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;

import java.util.List;

import static nofrills.Main.mc;

public class KuudraUtil {
    public static final List<PickupSpot> pickupSpots = List.of( // data borrowed from odin
            PickupSpot.X,
            PickupSpot.Triangle,
            PickupSpot.Slash,
            PickupSpot.Equals
    );
    private static final List<String> dpsLines = List.of(
            "Figure out how to",
            "Charge Ballista or",
            "Shoot Ballista at",
            "Mine Kuudra's pods"
    );
    private static PickupSpot preSpot = null;
    private static MagmaCubeEntity kuudraEntity = null;
    private static Phase currentPhase = Phase.Starting;

    public static Phase getCurrentPhase() {
        return currentPhase;
    }

    public static MagmaCubeEntity getKuudraEntity() {
        return kuudraEntity;
    }

    public static PickupSpot getPreSpot() {
        return preSpot;
    }

    private static void updateKuudraEntity() {
        if (!EntityCache.exists(kuudraEntity)) {
            MagmaCubeEntity kuudra = null;
            double maxY = 0;
            int cubesFound = 0;
            for (Entity ent : Utils.getEntities()) {
                if (ent instanceof MagmaCubeEntity cube && cube.getSize() == 30) {
                    double y = ent.getEntityPos().getY();
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
            if (cubesFound == 2 || currentPhase.equals(Phase.Lair)) {
                kuudraEntity = kuudra;
            }
        }
    }

    private static void updateCurrentPhase() {
        if (Utils.isInZone(-133, 59, -75, -73, 1, -138)) {
            currentPhase = Phase.Lair;
            return;
        }
        for (String line : SkyblockData.getLines()) {
            if (line.startsWith("Rescue supplies")) {
                currentPhase = Phase.Collect;
                return;
            }
            if (line.startsWith("Protect Elle")) {
                currentPhase = Phase.Build;
                return;
            }
            for (String phaseLine : dpsLines) {
                if (line.startsWith(phaseLine)) {
                    currentPhase = Phase.DPS;
                    return;
                }
            }
        }
        currentPhase = Phase.Starting;
    }

    private static void updatePreSpot() {
        if (mc.player != null && preSpot == null && currentPhase.equals(Phase.Collect)) {
            Vec3d pos = mc.player.getEntityPos();
            for (KuudraUtil.PickupSpot pickupSpot : KuudraUtil.pickupSpots) {
                if (pickupSpot.spot.distanceTo(pos) < pickupSpot.playerDist) {
                    preSpot = pickupSpot;
                    break;
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (Utils.isInKuudra()) {
            updateCurrentPhase();
            updateKuudraEntity();
            updatePreSpot();
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        preSpot = null;
        kuudraEntity = null;
        currentPhase = Phase.Starting;
    }

    public enum Phase {
        Starting,
        Collect,
        Build,
        DPS,
        Lair
    }

    public static class PickupSpot {
        public static final PickupSpot XCannon = new PickupSpot("X Cannon", new Vec3d(-143.0, 76.0, -125.0), 16, 0, null);
        public static final PickupSpot Shop = new PickupSpot("Shop", new Vec3d(-81.0, 76.0, -143.0), 18, 0, null);
        public static final PickupSpot Square = new PickupSpot("Square", new Vec3d(-143.0, 76.0, -80.0), 20, 0, null);
        public static final PickupSpot Equals = new PickupSpot("Equals", new Vec3d(-65.5, 76.0, -87.5), 18, 15, null);
        public static final PickupSpot X = new PickupSpot("X", new Vec3d(-142.5, 77.0, -151.0), 18, 30, PickupSpot.XCannon);
        public static final PickupSpot Triangle = new PickupSpot("Triangle", new Vec3d(-67.5, 77.0, -122.5), 18, 15, PickupSpot.Shop);
        public static final PickupSpot Slash = new PickupSpot("Slash", new Vec3d(-113.5, 77.0, -68.5), 18, 15, PickupSpot.Square);

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
