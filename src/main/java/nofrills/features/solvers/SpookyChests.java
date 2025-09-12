package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

public class SpookyChests {
    public static final Feature instance = new Feature("spookyChests");

    public static final SettingColor color = new SettingColor(new RenderColor(1.0f, 0.67f, 0.0f, 0.67f), "color", instance.key());

    private static final EntityCache chestList = new EntityCache();

    private static boolean isSpooky() {
        for (String line : SkyblockData.getLines()) {
            if (line.startsWith("Spooky Festival") && line.contains(":")) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && isSpooky() && event.entity.isCustomNameVisible()) {
            String name = Utils.toLower(event.namePlain);
            if (name.equals("trick or treat?") || name.equals("party chest")) {
                if (!chestList.has(event.entity)) {
                    Utils.showTitle("§6§lCHEST SPAWNED!", "", 5, 20, 5);
                    Utils.playSound(SoundEvents.BLOCK_VAULT_ACTIVATE, SoundCategory.MASTER, 1.0f, 1.0f);
                    chestList.add(event.entity);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !chestList.empty()) {
            for (Entity chest : chestList.get()) {
                BlockPos pos = Utils.findGround(chest.getBlockPos(), 4).up(1);
                event.drawFilledWithBeam(Box.enclosing(pos, pos), 256, true, color.value());
            }
        }
    }
}
