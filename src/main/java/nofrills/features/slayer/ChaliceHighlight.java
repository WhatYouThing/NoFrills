package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.regex.Pattern;

public class ChaliceHighlight {
    public static final Feature instance = new Feature("chaliceHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xaaaf00ff), "color", instance.key());

    private static final Pattern chaliceRegex = Pattern.compile("[0-9]*\\.[0-9]*s");
    private static final EntityCache chaliceData = new EntityCache();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.vampire) && Utils.isInChateau()) {
            if (!chaliceData.has(event.entity) && chaliceRegex.matcher(event.namePlain).matches()) {
                chaliceData.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.vampire)) {
            for (Entity ent : chaliceData.get()) {
                BlockPos blockPos = Utils.findGround(ent.getBlockPos(), 4);
                Vec3d pos = ent.getPos();
                Vec3d posAdjust = new Vec3d(pos.x, blockPos.up(1).getY() + 0.5, pos.z);
                event.drawFilledWithBeam(Box.of(posAdjust, 1, 1.25, 1), 256, true, color.value());
            }
        }
    }
}
