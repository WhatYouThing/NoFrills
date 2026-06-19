package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.KuudraUtil;
import nofrills.misc.Utils;

public class PreMessage {
    public static final Feature instance = new Feature("preMessage");

    private static int missingTicks = 20;

    @EventHandler
    private static void onTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.Phase.Collect)) {
            if (missingTicks > 0) {
                missingTicks--;
                if (missingTicks == 0) {
                    KuudraUtil.PickupSpot preSpot = KuudraUtil.getPreSpot();
                    if (preSpot != null) {
                        KuudraUtil.PickupSpot secondary = KuudraUtil.PickupSpot.fromType(preSpot.secondary);
                        Utils.infoFormat("§eYour Pre: {}", preSpot.name);
                        boolean preFound = false, secondaryFound = false;
                        for (Entity ent : Utils.getEntities()) {
                            if (ent instanceof Giant) {
                                Vec3 entPos = ent.position();
                                Vec3 supplyPos = new Vec3(entPos.x(), 76, entPos.z());
                                if (preSpot.spot.distanceTo(supplyPos) < preSpot.supplyDist) {
                                    preFound = true;
                                }
                                if (secondary != null && secondary.spot.distanceTo(supplyPos) < secondary.supplyDist) {
                                    secondaryFound = true;
                                }
                            }
                        }
                        if (!preFound) {
                            Utils.sendMessage("/pc No " + preSpot.name + "!");
                        } else if (!secondaryFound && secondary != null) {
                            Utils.sendMessage("/pc No " + secondary.name + "!");
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
