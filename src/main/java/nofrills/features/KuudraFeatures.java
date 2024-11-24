package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.text.DecimalFormat;

import static nofrills.Main.mc;

public class KuudraFeatures {
    private static final DecimalFormat kuudraHealthFormat = new DecimalFormat("#.00");
    private static final Vec3d[] supplyDropSpots = {
            new Vec3d(-98.00, 79.00, -112.94),
            new Vec3d(-106.00, 79.00, -112.94),
            new Vec3d(-110.00, 79.00, -106.00),
            new Vec3d(-106.00, 79.00, -99.06),
            new Vec3d(-98.00, 79.00, -99.06),
            new Vec3d(-94.00, 79.00, -106.00)
    };
    private static final PickupSpot[] supplyPickupSpots = {
            // data borrowed from odin
            // originally i tried my own solution, but it was such voodoo that terry davis would resurrect himself to witness it
            new PickupSpot("X", new Vec3d(-142.5, 77.0, -151.0), 18.0f, 30.0f,
                    new PickupSpot("X Cannon", new Vec3d(-143.0, 76.0, -125.0), 16.0f, 0.0f, null)),
            new PickupSpot("Triangle", new Vec3d(-67.5, 77.0, -122.5), 18.0f, 15.0f,
                    new PickupSpot("Shop", new Vec3d(-81.0, 76.0, -143.0), 18.0f, 0.0f, null)),
            new PickupSpot("Slash", new Vec3d(-113.5, 77.0, -68.5), 18.0f, 15.0f,
                    new PickupSpot("Square", new Vec3d(-143.0, 76.0, -80.0), 20.0f, 0.0f, null)),
            new PickupSpot("Equals", new Vec3d(-65.5, 76.0, -87.5), 18.0f, 15.0f, null),
    };
    private static int freshTicks = 0;
    private static int missingTicks = 20;
    private static MagmaCubeEntity kuudraEntity;

    private static kuudraPhases getCurrentPhase() {
        if (Utils.isInZone(-133, 59, -75, -73, 1, -138)) {
            return kuudraPhases.Lair;
        }
        for (String line : Utils.scoreboardLines) {
            if (line.startsWith("Rescue supplies")) {
                return kuudraPhases.Collect;
            }
            if (line.startsWith("Protect Elle")) {
                return kuudraPhases.Build;
            }
            for (String phaseLine : new String[]{"Figure out how to", "Charge Ballista or", "Shoot Ballista at", "Mine Kuudra's pods"}) {
                if (line.startsWith(phaseLine)) {
                    return kuudraPhases.DPS;
                }
            }
        }
        return kuudraPhases.Starting;
    }

    public static boolean isNearPile() {
        Vec3d playerPos = mc.player.getPos();
        for (Vec3d drop : supplyDropSpots) {
            if (drop.distanceTo(playerPos) <= 6) {
                return true;
            }
        }
        return false;
    }

    private static void updateKuudraEntity() {
        Entity kuudra = null;
        double maxY = 0;
        int cubesFound = 0;
        for (Entity ent : mc.world.getEntities()) {
            if (ent.getType() == EntityType.MAGMA_CUBE && ((MagmaCubeEntity) ent).getSize() == 30) {
                double y = ent.getPos().getY();
                cubesFound++;
                if (y > maxY) {
                    kuudra = ent;
                    maxY = y;
                }
            }
            if (cubesFound == 2) { // scuffed, but needed, because the average tps in kuudra is too low even for dr. disrespect
                kuudraEntity = (MagmaCubeEntity) kuudra;
                break;
            }
        }
    }

    @EventHandler
    public static void onTick(WorldTickEvent event) {
        kuudraPhases phase = getCurrentPhase();
        if (Utils.isInKuudra() && phase != kuudraPhases.Starting) {
            if (Config.kuudraMissing && phase == kuudraPhases.Collect) {
                if (missingTicks == 0) {
                    PickupSpot preSpot = null;
                    Vec3d selfPos = mc.player.getPos();
                    for (PickupSpot pickupSpot : supplyPickupSpots) {
                        if (pickupSpot.spot.distanceTo(selfPos) < pickupSpot.maxPlayerDist) {
                            preSpot = pickupSpot;
                            Utils.info("§eYour Pre: " + pickupSpot.name);
                        }
                    }
                    if (preSpot != null) {
                        boolean preFound = false, secondaryFound = false;
                        for (Entity ent : mc.world.getEntities()) {
                            if (ent.getType() == EntityType.GIANT) {
                                Vec3d entPos = ent.getPos();
                                Vec3d supplyPos = new Vec3d(entPos.getX(), 76, entPos.getZ());
                                if (preSpot.spot.distanceTo(supplyPos) < preSpot.maxSupplyDist) {
                                    preFound = true;
                                }
                                if (preSpot.secondary != null) {
                                    if (preSpot.secondary.spot.distanceTo(supplyPos) < preSpot.secondary.maxSupplyDist) {
                                        secondaryFound = true;
                                    }
                                }
                            }
                        }
                        if (!preFound) {
                            Utils.sendMessage("/pc No " + preSpot.name + "!");
                        } else if (!secondaryFound && preSpot.secondary != null) {
                            Utils.sendMessage("/pc No " + preSpot.secondary.name + "!");
                        }
                    } else {
                        Utils.info("§eCouldn't find your Pre, which means that you're probably AFK, or getting carried (gross, git gud scrub).");
                    }
                    missingTicks = -1;
                } else if (missingTicks > 0) {
                    missingTicks--;
                }
            }
            if (freshTicks > 0 && phase == kuudraPhases.Build) {
                int seconds = freshTicks / 20;
                Utils.showTitleCustom("FRESH: " + seconds + "s", 1, 25, 2.5f, 0x55ff55);
                freshTicks--;
            }
            if (kuudraEntity == null || !kuudraEntity.isAlive()) {
                // alive check is needed in case kuudra goes out of render distance, and we need to find him again.
                if (phase != kuudraPhases.Lair) {
                    updateKuudraEntity();
                }
            } else {
                if (Config.kuudraHitbox && !Rendering.Entities.isDrawingOutline(kuudraEntity)) {
                    Rendering.Entities.drawOutline(kuudraEntity, true, new RenderColor(255, 255, 0, 255));
                }
                if (Config.kuudraHealth && getCurrentPhase() == kuudraPhases.DPS) {
                    float health = kuudraEntity.getHealth() / kuudraEntity.getMaxHealth();
                    Utils.showTitleCustom("KUUDRA: " + kuudraHealthFormat.format(health) + "% HP", 1, 25, 2.5f, 0xffff00);
                }
            }
        } else {
            freshTicks = 0;
            kuudraEntity = null;
            missingTicks = 20;
        }
    }

    @EventHandler
    public static void onChatMsg(ChatMsgEvent event) {
        if (Utils.isInKuudra()) {
            String msg = event.getPlainMessage();
            if (msg.equals("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!")) {
                if (Config.kuudraFresh && !Config.kuudraFreshMsg.isEmpty()) {
                    Utils.sendMessage(Config.kuudraFreshMsg);
                }
                if (Config.kuudraFreshTimer) {
                    freshTicks = 200;
                }
            }
            if (Config.kuudraDrain) {
                if (msg.startsWith("Used Extreme Focus!")) {
                    String mana = msg.replace("Used Extreme Focus! (", "").replace(" Mana)", "");
                    int players = 0;
                    for (Entity ent : mc.world.getEntities()) {
                        if (ent.getType() == EntityType.PLAYER && ent != mc.player && !ent.isInvisible()) {
                            if (ent.distanceTo(mc.player) <= 5) {
                                players++;
                            }
                        }
                    }
                    if (!Config.kuudraDrainMsg.isEmpty()) {
                        String drainMsg = Config.kuudraDrainMsg.replace("{mana}", mana).replace("{players}", "" + players);
                        Utils.sendMessage(drainMsg);
                    }
                    event.cancel();
                }
                if (msg.startsWith("You now have") && msg.contains("Damage Resistance for 5 seconds")) {
                    event.cancel();
                }
                if (msg.equals("Your Extreme Focus has worn off.")) {
                    event.cancel();
                }
            }
        }
    }

    private enum kuudraPhases {
        Starting,
        Collect,
        Build,
        DPS,
        Lair
    }

    private static class PickupSpot {
        public String name;
        public Vec3d spot;
        public float maxSupplyDist;
        public float maxPlayerDist;
        public PickupSpot secondary;

        PickupSpot(String name, Vec3d spot, float maxSupplyDist, float maxPlayerDist, PickupSpot secondary) {
            this.name = name;
            this.spot = spot;
            this.maxSupplyDist = maxSupplyDist;
            this.maxPlayerDist = maxPlayerDist;
            this.secondary = secondary;
        }
    }
}
