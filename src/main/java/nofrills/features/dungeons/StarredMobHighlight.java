package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.util.List;

public class StarredMobHighlight {
    public static final Feature instance = new Feature("starredMobHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xff00ffff), "color", instance.key());

    private static boolean isDungeonMob(Entity entity) {
        return !Rendering.Entities.isDrawingOutline(entity) && switch (entity) {
            case ZombieEntity ignored -> true;
            case SkeletonEntity ignored -> true;
            case EndermanEntity ignored -> true;
            case WitherSkeletonEntity ignored -> true;
            case PlayerEntity player -> !Utils.isPlayer(player);
            default -> false;
        };
    }

    private static boolean isStarred(String name) {
        int index = name.indexOf(Utils.Symbols.star);
        return index != -1 && index == name.lastIndexOf(Utils.Symbols.star);
    }

    private static void renderOutline(Entity entity, RenderColor color) {
        List<Entity> otherEntities = Utils.getOtherEntities(entity, 0.5, 2, 0.5, StarredMobHighlight::isDungeonMob);
        if (!otherEntities.isEmpty()) {
            Entity closest = Utils.findNametagOwner(entity, otherEntities);
            if (closest != null && !Rendering.Entities.isDrawingOutline(closest)) {
                Rendering.Entities.drawOutline(closest, true, color);
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.entity instanceof ArmorStandEntity && isStarred(event.namePlain)) {
            renderOutline(event.entity, color.value());
        }
    }
}
