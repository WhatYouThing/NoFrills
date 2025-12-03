package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.KuudraUtil;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class PreMessage {
    public static final Feature instance = new Feature("preMessage");

    private static int missingTicks = 20;

    @EventHandler
    private static void onTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.phase.Collect)) {
            if (missingTicks > 0) {
                missingTicks--;
                if (missingTicks == 0) {
                    KuudraUtil.PickupSpot preSpot = null;
                    Vec3d selfPos = mc.player.getEntityPos();
                    for (KuudraUtil.PickupSpot pickupSpot : KuudraUtil.pickupSpots) {
                        if (pickupSpot.spot.distanceTo(selfPos) < pickupSpot.playerDist) {
                            preSpot = pickupSpot;
                            Utils.info("§eYour Pre: " + pickupSpot.name);
                        }
                    }
                    if (preSpot != null) {
                        boolean preFound = false, secondaryFound = false;
                        for (Entity ent : Utils.getEntities()) {
                            if (ent instanceof GiantEntity) {
                                Vec3d entPos = ent.getEntityPos();
                                Vec3d supplyPos = new Vec3d(entPos.getX(), 76, entPos.getZ());
                                if (preSpot.spot.distanceTo(supplyPos) < preSpot.supplyDist) {
                                    preFound = true;
                                }
                                if (preSpot.secondary != null) {
                                    if (preSpot.secondary.spot.distanceTo(supplyPos) < preSpot.secondary.supplyDist) {
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
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        missingTicks = 20;
    }
}
