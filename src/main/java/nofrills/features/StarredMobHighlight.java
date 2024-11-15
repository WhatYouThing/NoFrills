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
import nofrills.misc.Utils;

import java.awt.*;
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

    private static Entity findClosestEntity(List<Entity> entityList, Entity target) {
        Entity closest = null;
        float lowestDist = 2.0f;
        for (Entity ent : entityList) {
            float dist = Utils.horizontalDistance(ent, target);
            if (dist < lowestDist) {
                closest = ent;
                lowestDist = dist;
            }
        }
        return closest;
    }

    @EventHandler
    public static void onNamed(EntityNamedEvent event) {
        if (Config.starredMobHighlight && Utils.isInDungeons()) {
            if (event.entity.getType() == ARMOR_STAND && event.namePlain.startsWith(star)) {
                if (event.namePlain.replace(star, "").equals(event.namePlain.replaceAll(star, ""))) {
                    List<Entity> otherEntities = event.entity.getWorld().getOtherEntities(
                            event.entity,
                            event.entity.getBoundingBox().expand(0.5, 1, 0.5),
                            StarredMobHighlight::isDungeonMob
                    );
                    if (!otherEntities.isEmpty()) {
                        Entity closest = findClosestEntity(otherEntities, event.entity);
                        if (closest != null) {
                            Color colors = Config.starredMobColor;
                            Utils.setRenderOutline(closest, true, (float) colors.getRed() / 255, (float) colors.getGreen() / 255, (float) colors.getBlue() / 255, (float) colors.getAlpha() / 255);
                        }
                    }
                }
            }
        }
    }
}
