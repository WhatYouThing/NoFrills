package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.phys.Vec3;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;

import java.util.List;
import java.util.Optional;

import static nofrills.Main.mc;

@EventListener
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
    private static final EntityCache kuudraCache = new EntityCache();
    private static PickupSpot preSpot = null;
    private static Phase currentPhase = Phase.Starting;

    public static Phase getCurrentPhase() {
        return currentPhase;
    }

    public static MagmaCube getKuudraEntity() {
        return (MagmaCube) kuudraCache.getFirst();
    }

    public static Optional<Float> getKuudraHealth() {
        MagmaCube kuudra = getKuudraEntity();
        if (kuudra == null) return Optional.empty();
        float health = kuudra.getHealth();
        if (getCurrentPhase().equals(Phase.Lair)) {
            return Optional.of(240000000.0f * (health / 25000.f));
        }
        if (health == 1024.0f) {
            return Optional.of(100000.0f);
        }
        return Optional.of(health);
    }

    public static PickupSpot getPreSpot() {
        return preSpot;
    }

    public static boolean isSupplyCrateEntity(Entity entity) {
        if (entity instanceof Giant giant) {
            return Utils.hasTexturePayload(giant.getItemBySlot(EquipmentSlot.MAINHAND), -292152149);
        }
        return false;
    }

    private static void updateKuudraEntity() {
        if (!kuudraCache.empty()) return;
        for (Entity ent : Utils.getEntities()) {
            if (ent instanceof MagmaCube cube && cube.getSize() == 30 && cube.getAttributeBaseValue(Attributes.MAX_HEALTH) == 100000.0f) {
                kuudraCache.add(cube);
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
            Vec3 pos = mc.player.position();
            for (PickupSpot pickupSpot : KuudraUtil.pickupSpots) {
                if (pickupSpot.spot.distanceTo(pos) < pickupSpot.playerDist) {
                    preSpot = pickupSpot;
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
        currentPhase = Phase.Starting;
    }

    public enum Phase {
        Starting,
        Collect,
        Build,
        DPS,
        Lair
    }

    public enum SpotType {
        X,
        XCannon,
        Square,
        Slash,
        Equals,
        Triangle,
        Shop,
        None
    }

    public static class PickupSpot {
        public static final PickupSpot X = new PickupSpot("X", -142.5, 77.0, -151.0, 18, 30)
                .withSecondary(SpotType.XCannon);
        public static final PickupSpot XCannon = new PickupSpot("X Cannon", -143.0, 76.0, -125.0, 16, 0);
        public static final PickupSpot Square = new PickupSpot("Square", -143.0, 76.0, -80.0, 20, 0);
        public static final PickupSpot Slash = new PickupSpot("Slash", -113.5, 77.0, -68.5, 18, 15)
                .withSecondary(SpotType.Square);
        public static final PickupSpot Equals = new PickupSpot("Equals", -65.5, 76.0, -87.5, 18, 15);
        public static final PickupSpot Triangle = new PickupSpot("Triangle", -67.5, 77.0, -122.5, 18, 15)
                .withSecondary(SpotType.Shop);
        public static final PickupSpot Shop = new PickupSpot("Shop", -81.0, 76.0, -143.0, 18, 0);

        public String name;
        public Vec3 spot;
        public double supplyDist;
        public double playerDist;
        public SpotType secondary;

        public PickupSpot(String name, double x, double y, double z, double supplyDist, double playerDist) {
            this.name = name;
            this.spot = new Vec3(x, y, z);
            this.supplyDist = supplyDist;
            this.playerDist = playerDist;
            this.secondary = SpotType.None;
        }

        public static PickupSpot fromType(SpotType type) {
            return switch (type) {
                case X -> PickupSpot.X;
                case XCannon -> PickupSpot.XCannon;
                case Square -> PickupSpot.Square;
                case Slash -> PickupSpot.Slash;
                case Equals -> PickupSpot.Equals;
                case Triangle -> PickupSpot.Triangle;
                case Shop -> PickupSpot.Shop;
                case None -> null;
            };
        }

        public PickupSpot withSecondary(SpotType type) {
            this.secondary = type;
            return this;
        }

        public boolean matches(String msg) {
            String name = Utils.toLower(this.name);
            return msg.equals("no " + name + "!") || msg.equals("no " + name.replaceAll(" ", "") + "!");
        }
    }
}
