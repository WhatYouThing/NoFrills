package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import nofrills.config.Config;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;

import static nofrills.Main.mc;

public class AutoSprint {
    @EventHandler
    public static void tick(WorldTickEvent event) {
        if (Config.autoSprint) {
            mc.options.sprintKey.setPressed(true);
        }

        for (Entity ent : mc.world.getEntities()) {
            if (ent instanceof LivingEntity) {
                Rendering.Entities.drawOutline(ent, true, RenderColor.fromHex(0xffffff));
            }
        }
    }
}
