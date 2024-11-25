package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import nofrills.config.Config;
import nofrills.events.EntityNamedEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.util.List;

import static net.minecraft.entity.EntityType.ARMOR_STAND;

public class StarredMobHighlight {
    private static final String star = "✯";

    private static boolean isDungeonMob(Entity entity) {
        return switch (entity) {
            case ZombieEntity ignored -> true;
            case SkeletonEntity ignored -> true;
            case EndermanEntity ignored -> true;
            case WitherSkeletonEntity ignored -> true;
            case PlayerEntity ignored -> true;
            default -> false;
        };
    }

    @EventHandler
    public static void onNamed(EntityNamedEvent event) {
        if (Config.starredMobHighlight && Utils.isInDungeons()) {
            if (event.entity.getType() == ARMOR_STAND && event.namePlain.startsWith(star)) {
                if (event.namePlain.replace(star, "").equals(event.namePlain.replaceAll(star, ""))) {
                    List<Entity> otherEntities = Utils.getNearbyEntities(event.entity, 0.6, 2, 0.6, StarredMobHighlight::isDungeonMob);
                    if (!otherEntities.isEmpty()) {
                        Entity closest = Utils.findNametagOwner(event.entity, otherEntities);
                        if (closest != null) {
                            Rendering.Entities.drawOutline(closest, true, RenderColor.fromColor(Config.starredMobColor));
                        }
                    }
                }
            }
        }
    }
}
