package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import nofrills.misc.Utils;

import java.util.List;

public class StarredMobHighlight {
    public static final Feature instance = new Feature("starredMobHighlight");

    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Outline, RenderStyle.class, "style", instance);
    public static final SettingColor outlineColor = new SettingColor(RenderColor.fromArgb(0xff00ffff), "color", instance);
    public static final SettingColor fillColor = new SettingColor(RenderColor.fromHex(0x00ffff, 0.5f), "fillColor", instance);

    private static final EntityCache cache = new EntityCache();

    private static boolean isDungeonMob(Entity entity) {
        if (entity instanceof ArmorStand) {
            return false;
        }
        return Utils.isMob(entity) && !cache.has(entity);
    }

    private static boolean isStarred(String name) {
        int index = name.indexOf(Utils.Symbols.star);
        return index != -1 && index == name.lastIndexOf(Utils.Symbols.star);
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && isStarred(event.namePlain)) {
            List<Entity> otherEntities = Utils.getOtherEntities(event.entity, 0.5, 2, 0.5, StarredMobHighlight::isDungeonMob);
            Entity closest = Utils.findNametagOwner(event.entity, otherEntities);
            if (closest != null && !MinibossHighlight.cache.has(closest)) {
                cache.add(closest);
            }
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
