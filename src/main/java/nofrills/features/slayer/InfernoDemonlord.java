package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class InfernoDemonlord {
    public static final Feature instance = new Feature("infernoDemonlord");

    public static final SettingBool pillarAlert = new SettingBool(false, "pillarAlert", instance.key());
    public static final SettingBool noSpam = new SettingBool(false, "noSpam", instance.key());

    private static final Pattern firePillarRegex = Pattern.compile("[0-9]s [0-9] hits");
    private static final List<Vec3d> pillarData = new ArrayList<>();
    private static int pillarClearTicks = 0;

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && pillarAlert.value() && !pillarData.isEmpty() && firePillarRegex.matcher(event.namePlain).matches()) {
            double dist = Utils.horizontalDistance(event.entity.getPos(), pillarData.getLast());
            if (dist <= 3) {
                Utils.showTitleCustom("Pillar: " + event.namePlain, 30, 25, 4.0f, RenderColor.fromHex(0xffff00));
                pillarClearTicks = 60;
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && !SlayerUtil.bossAlive && pillarClearTicks > 0) {
            pillarData.clear();
            pillarClearTicks = 0;
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.blaze)) {
            if (pillarClearTicks > 0) {
                pillarClearTicks--;
                if (pillarClearTicks == 0) {
                    pillarData.clear();
                }
            }
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && noSpam.value()) {
            String msg = event.getPlainMessage();
            if (msg.equals("Your hit was reduced by Hellion Shield!")) {
                event.cancel();
            }
            if (msg.startsWith("Strike using the") && msg.endsWith("attunement on your dagger!")) {
                event.cancel();
            }
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && pillarAlert.value() && SlayerUtil.isFightingBoss(SlayerUtil.blaze)) {
            if (event.isSound(SoundEvents.ENTITY_CHICKEN_EGG)) {
                Vec3d pos = new Vec3d(event.packet.getX(), event.packet.getY(), event.packet.getZ());
                if (pillarData.isEmpty()) {
                    for (Entity ent : Utils.getEntities()) {
                        if (ent instanceof ArmorStandEntity && ent.getCustomName() != null) {
                            if (SlayerUtil.isSpawner(Utils.toPlainString(ent.getCustomName()))) {
                                double dist = Utils.horizontalDistance(pos, ent.getPos());
                                if (dist <= 1.5) {
                                    pillarData.add(pos);
                                    pillarClearTicks = 60;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    double dist = Utils.horizontalDistance(pos, pillarData.getLast());
                    if (dist <= 4) {
                        pillarData.add(pos);
                        pillarClearTicks = 60;
                    }
                }
            }
        }
    }
}
