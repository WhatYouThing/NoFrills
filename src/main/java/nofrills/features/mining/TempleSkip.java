package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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

    private static final Text text = Text.literal("Stand Below, Double Pearl, Jump");
    private static BlockPos spot = null;

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && spot == null && Utils.isInArea("Crystal Hollows") && event.namePlain.equals("Kalhuiki Door Guardian")) {
            BlockPos ground = Utils.findGround(event.entity.getBlockPos(), 4);
            if (mc.world.getBlockState(ground).getBlock().equals(Blocks.STONE_BRICKS)) {
                spot = ground.add(20, -45, -35);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && spot != null && Utils.isInArea("Crystal Hollows")) {
            Box box = Box.enclosing(spot, spot);
            event.drawOutline(box, true, color.value());
            event.drawText(box.getCenter().add(0, -1, 0), text, 0.035f, true, RenderColor.white);
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        spot = null;
    }
}
