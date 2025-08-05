package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.PlaySoundEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.regex.Pattern;

public class RiftstalkerBloodfiend {
    public static final Feature instance = new Feature("riftstalkerBloodfiend");

    public static final SettingBool ice = new SettingBool(false, "ice", instance.key());
    public static final SettingBool steak = new SettingBool(false, "steak", instance.key());
    public static final SettingBool ichor = new SettingBool(false, "ichor", instance.key());
    public static final SettingColor ichorColor = new SettingColor(RenderColor.fromArgb(0xaaaf00ff), "ichorColor", instance.key());
    public static final SettingBool mania = new SettingBool(false, "mania", instance.key());
    public static final SettingBool springs = new SettingBool(false, "springs", instance.key());

    private static final Pattern chaliceRegex = Pattern.compile("[0-9]*\\.[0-9]*s");
    private static final EntityCache chaliceData = new EntityCache();
    private static String iceText = "";
    private static boolean shouldSteak = false;

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.vampire) && Utils.isInChateau()) {
            if (ice.value() && SlayerUtil.isTimer(event.namePlain) && SlayerUtil.isNearSpawner(event.entity)) {
                if (event.namePlain.contains("TWINCLAWS")) {
                    iceText = Utils.format("Ice: {}", event.namePlain.split("TWINCLAWS")[1].trim().split(" ")[0]);
                } else {
                    iceText = "";
                }
            }
            if (steak.value() && SlayerUtil.isName(event.namePlain) && SlayerUtil.isNearSpawner(event.entity)) {
                shouldSteak = event.namePlain.contains(Utils.Symbols.vampLow);
            }
            if (ichor.value() && chaliceRegex.matcher(event.namePlain).matches()) {
                chaliceData.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInChateau()) {
            if (SlayerUtil.isFightingBoss(SlayerUtil.vampire)) {
                if (ice.value() && !iceText.isEmpty()) {
                    Utils.showTitleCustom(iceText, 1, 25, 4.0f, 0x00ffff);
                }
                if (steak.value() && shouldSteak && iceText.isEmpty()) {
                    Utils.showTitleCustom("Steak!", 1, 25, 4.0f, 0xff0000);
                }
            } else {
                iceText = "";
                shouldSteak = false;
                chaliceData.clear();
            }
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && Utils.isInChateau()) {
            if ((mania.value() && event.isSound(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE)) || (springs.value() && event.isSound(SoundEvents.ENTITY_WITHER_SPAWN))) {
                event.cancel();
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && ichor.value() && SlayerUtil.isFightingBoss(SlayerUtil.vampire)) {
            for (Entity ent : chaliceData.get()) {
                BlockPos blockPos = Utils.findGround(ent.getBlockPos(), 4);
                Vec3d pos = ent.getPos();
                Vec3d posAdjust = new Vec3d(pos.x, blockPos.up(1).getY() + 0.5, pos.z);
                event.drawFilledWithBeam(Box.of(posAdjust, 1, 1.25, 1), 256, true, ichorColor.value());
            }
        }
    }
}
