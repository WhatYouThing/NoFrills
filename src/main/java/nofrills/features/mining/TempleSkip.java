package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class TempleSkip {
    public static final Feature instance = new Feature("templeSkip");

    public static final SettingColor color = new SettingColor(new RenderColor(127, 0, 255, 255), "color", instance.key());

    private static BlockPos spot = null;

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && spot == null && Utils.isInArea("Crystal Hollows") && event.namePlain.equals("Kalhuiki Door Guardian")) {
            BlockPos ground = Utils.findGround(event.entity.blockPosition(), 4);
            if (mc.level.getBlockState(ground).getBlock().equals(Blocks.STONE_BRICKS)) {
                spot = ground.offset(20, -45, -35);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && spot != null && Utils.isInArea("Crystal Hollows")) {
            BlockPos standPos = spot.below(8);
            AABB box = AABB.encapsulatingFullBlocks(spot, spot);
            AABB standBox = AABB.encapsulatingFullBlocks(standPos, standPos);
            event.drawOutline(box, true, color.value());
            event.drawOutline(standBox, true, color.value());
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        spot = null;
    }
}
