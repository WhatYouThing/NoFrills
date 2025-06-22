package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EntityUpdatedEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.Config;

public class DungeonHighlight {
    private static final String[] minibossList = {
            "Lost Adventurer",
            "Diamond Guy",
            "Shadow Assassin",
            "King Midas",
            "Spirit Bear"
    };

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
        return name.startsWith(Utils.Symbols.star) && name.indexOf(Utils.Symbols.star) == name.lastIndexOf(Utils.Symbols.star);
    }

    private static void renderOutline(Entity entity, RenderColor color) {
        List<Entity> otherEntities = Utils.getNearbyEntities(entity, 0.5, 2, 0.5, DungeonHighlight::isDungeonMob);
        if (!otherEntities.isEmpty()) {
            Entity closest = Utils.findNametagOwner(entity, otherEntities);
            if (closest != null && !Rendering.Entities.isDrawingOutline(closest)) {
                Rendering.Entities.drawOutline(closest, true, color);
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (Config.starredMobHighlight() && Utils.isInDungeons() && event.entity instanceof ArmorStandEntity && isStarred(event.namePlain)) {
            renderOutline(event.entity, RenderColor.fromColor(Config.starredMobColor()));
        }
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (Config.miniHighlight() && Utils.isInDungeons() && event.entity instanceof PlayerEntity player && !Utils.isPlayer(player)) {
            String name = player.getName().getString();
            for (String mini : minibossList) {
                if (name.equals(mini)) {
                    Rendering.Entities.drawOutline(event.entity, true, RenderColor.fromColor(Config.miniColor()));
                    break;
                }
            }
        }
    }
}
