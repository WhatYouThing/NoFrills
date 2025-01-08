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

public class DungeonHighlight {
    private static final String[] minibossList = {
            "Lost Adventurer",
            "Angry Archaeologist",
            "Shadow Assassin",
            "King Midas",
            "Frozen Adventurer",
            "Spirit Bear"
    };

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

    private static void renderOutline(Entity entity, RenderColor color) {
        List<Entity> otherEntities = Utils.getNearbyEntities(entity, 0.6, 2, 0.6, DungeonHighlight::isDungeonMob);
        if (!otherEntities.isEmpty()) {
            Entity closest = Utils.findNametagOwner(entity, otherEntities);
            if (closest != null) {
                Rendering.Entities.drawOutline(closest, true, color);
            }
        }
    }

    @EventHandler
    public static void onNamed(EntityNamedEvent event) {
        if (Utils.isInDungeons() && event.entity.getType() == ARMOR_STAND) {
            if (Config.miniHighlight && event.namePlain.contains(Utils.Symbols.heart)) {
                for (String mini : minibossList) {
                    if (event.namePlain.contains(mini)) {
                        renderOutline(event.entity, RenderColor.fromColor(Config.miniColor));
                        return;
                    }
                }
            }
        }
        if (Config.starredMobHighlight && event.namePlain.startsWith(Utils.Symbols.star)) {
            if (event.namePlain.indexOf(Utils.Symbols.star) == event.namePlain.lastIndexOf(Utils.Symbols.star)) {
                /*
                    ensures that the armor stand only has a single star in its name,
                    so that we don't replicate the spaghetti code from badlion where
                    overload damage ticks are considered as starred mobs
                */
                renderOutline(event.entity, RenderColor.fromColor(Config.starredMobColor));
            }
        }
    }
}
