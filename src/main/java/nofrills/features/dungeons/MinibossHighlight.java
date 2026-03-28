package nofrills.features.dungeons;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import nofrills.misc.Utils;

import java.util.HashSet;

public class MinibossHighlight {
    public static final Feature instance = new Feature("minibossHighlight");

    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Outline, RenderStyle.class, "style", instance);
    public static final SettingColor outlineColor = new SettingColor(RenderColor.fromArgb(0xffffff00), "color", instance);
    public static final SettingColor fillColor = new SettingColor(RenderColor.fromHex(0xffff00, 0.5f), "fillColor", instance);

    public static final EntityCache cache = new EntityCache();

    private static final HashSet<String> minibossList = Sets.newHashSet(
            "Lost Adventurer",
            "Diamond Guy",
            "Shadow Assassin",
            "King Midas",
            "Spirit Bear"
    );

    private static boolean isMiniboss(Entity ent) {
        if (ent instanceof Player player && !Utils.isPlayer(player)) {
            String name = player.getName().getString();
            if (!minibossList.contains(name)) {
                return false;
            }
            if (Utils.isInDungeonBoss("4")) {
                return player.position().y() < 76.0; // prevents the Floor 4 "crowd" NPC's from getting highlighted
            } else {
                return !name.equals("Spirit Bear"); // prevents the Spirit Bear spawned by the Watcher from getting highlighted
            }
        }
        return false;
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && isMiniboss(event.entity)) {
            cache.add(event.entity);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            for (Entity ent : cache.get()) {
                if (!ent.isAlive()) continue;
                AABB box = Utils.getLerpedBox(ent, event.tickCounter.getGameTimeDeltaPartialTick(true));
                event.drawStyled(box, style.value(), false, outlineColor.value(), fillColor.value());
            }
        }
    }
}
