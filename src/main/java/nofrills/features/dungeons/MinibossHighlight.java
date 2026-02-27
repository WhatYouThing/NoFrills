package nofrills.features.dungeons;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.HashSet;

public class MinibossHighlight {
    public static final Feature instance = new Feature("minibossHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xffffff00), "color", instance.key());

    public static final EntityCache cache = new EntityCache();

    private static final HashSet<String> minibossList = Sets.newHashSet(
            "Lost Adventurer",
            "Diamond Guy",
            "Shadow Assassin",
            "King Midas",
            "Spirit Bear"
    );

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.entity instanceof PlayerEntity player && !Utils.isPlayer(player)) {
            if (minibossList.contains(player.getName().getString()) && !cache.has(event.entity)) {
                if (Utils.isInDungeonBoss("4") && event.entity.getEntityPos().getY() >= 76.0) {
                    return; // prevents the Floor 4 "crowd" NPC's from getting highlighted
                }
                cache.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            for (Entity ent : cache.get()) {
                if (!ent.isAlive()) continue;
                event.drawOutline(Utils.getLerpedBox(ent, event.tickCounter.getTickProgress(true)), false, color.value());
            }
        }
    }
}
