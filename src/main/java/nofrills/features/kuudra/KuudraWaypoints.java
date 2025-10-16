package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.KuudraUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

public class KuudraWaypoints {
    public static final Feature instance = new Feature("kuudraWaypoints");

    public static final SettingBool supply = new SettingBool(false, "supply", instance.key());
    public static final SettingColor supplyColor = new SettingColor(new RenderColor(0, 255, 255, 170), "supplyColor", instance.key());
    public static final SettingBool drop = new SettingBool(false, "drop", instance.key());
    public static final SettingColor dropColor = new SettingColor(new RenderColor(255, 255, 0, 170), "dropColor", instance.key());
    public static final SettingBool build = new SettingBool(false, "build", instance.key());
    public static final SettingColor buildColor = new SettingColor(new RenderColor(255, 0, 0, 170), "buildColor", instance.key());

    private static final EntityCache supplies = new EntityCache();
    private static final EntityCache dropOffs = new EntityCache();
    private static final EntityCache buildPiles = new EntityCache();

    private static Vec3d getGround(Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos.getX(), Math.max(pos.getY(), 75), pos.getZ());
        BlockPos ground = Utils.findGround(blockPos, 4);
        return new Vec3d(pos.getX(), ground.toCenterPos().add(0, 0.5, 0).getY(), pos.getZ());
    }

    private static boolean hasName(Entity ent, String name) {
        return ent.isCustomNameVisible() && ent.getCustomName() != null && Utils.toPlain(ent.getCustomName()).endsWith(name);
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInKuudra()) {
            if (drop.value() && event.namePlain.equals("BRING SUPPLY CHEST HERE")) {
                dropOffs.add(event.entity);
            }
            if (build.value() && event.namePlain.startsWith("PROGRESS: ") && event.namePlain.endsWith("%")) {
                buildPiles.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && supply.value() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.phase.Collect)) {
            for (Entity ent : Utils.getEntities()) {
                if (ent instanceof GiantEntity) {
                    supplies.add(ent);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInKuudra()) {
            if (!supplies.empty() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.phase.Collect)) {
                for (Entity supply : supplies.get()) {
                    float delta = event.tickCounter.getTickProgress(true);
                    Vec3d pos = supply.getLerpedPos(delta);
                    float yaw = supply.getYaw(delta);
                    Vec3d supplyPos = new Vec3d(
                            pos.getX() + (3.7 * Math.cos((yaw + 130) * (Math.PI / 180))),
                            75,
                            pos.getZ() + (3.7 * Math.sin((yaw + 130) * (Math.PI / 180)))
                    );
                    event.drawBeam(supplyPos, 256, true, supplyColor.value());
                }
            }
            if (!dropOffs.empty()) {
                for (Entity drop : dropOffs.get()) {
                    if (hasName(drop, "BRING SUPPLY CHEST HERE")) {
                        event.drawBeam(getGround(drop.getPos()), 256, true, dropColor.value());
                    } else {
                        dropOffs.remove(drop);
                    }
                }
            }
            if (!buildPiles.empty()) {
                for (Entity pile : buildPiles.get()) {
                    if (hasName(pile, "%")) {
                        event.drawBeam(getGround(pile.getLerpedPos(event.tickCounter.getTickProgress(true))), 256, true, buildColor.value());
                    } else {
                        buildPiles.remove(pile);
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        supplies.clear();
        dropOffs.clear();
        buildPiles.clear();
    }
}
