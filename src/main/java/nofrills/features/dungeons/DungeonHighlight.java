package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.util.List;

public class DungeonHighlight {
    public static final Feature instance = new Feature("dungeonHighlight");

    public static final SettingBool starred = new SettingBool(false, "starred", instance.key());
    public static final SettingColor starredColor = new SettingColor(RenderColor.fromArgb(0xff00ffff), "starredColor", instance.key());
    public static final SettingBool miniboss = new SettingBool(false, "miniboss", instance.key());
    public static final SettingColor minibossColor = new SettingColor(RenderColor.fromArgb(0xffffff00), "minibossColor", instance.key());
    public static final SettingBool witherKey = new SettingBool(false, "witherKey", instance.key());
    public static final SettingColor witherKeyColor = new SettingColor(RenderColor.fromArgb(0x8000ff00), "witherKeyColor", instance.key());
    public static final SettingBool spiritBow = new SettingBool(false, "spiritBow", instance.key());
    public static final SettingColor spiritBowColor = new SettingColor(RenderColor.fromArgb(0xaaaf00ff), "spiritBowColor", instance.key());

    private static final List<String> minibossList = List.of(
            "Lost Adventurer",
            "Diamond Guy",
            "Shadow Assassin",
            "King Midas",
            "Spirit Bear"
    );
    private static final EntityCache dungeonKeys = new EntityCache();
    private static final EntityCache spiritBows = new EntityCache();

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
        List<Entity> otherEntities = Utils.getOtherEntities(entity, 0.5, 2, 0.5, DungeonHighlight::isDungeonMob);
        if (!otherEntities.isEmpty()) {
            Entity closest = Utils.findNametagOwner(entity, otherEntities);
            if (closest != null && !Rendering.Entities.isDrawingOutline(closest)) {
                Rendering.Entities.drawOutline(closest, true, color);
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.entity instanceof ArmorStandEntity) {
            if (starred.value() && isStarred(event.namePlain)) {
                renderOutline(event.entity, starredColor.value());
            }
            if (witherKey.value() && (event.namePlain.equals("Wither Key") || event.namePlain.equals("Blood Key"))) {
                dungeonKeys.add(event.entity);
            }
            if (spiritBow.value() && event.namePlain.equals("Spirit Bow")) {
                spiritBows.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && miniboss.value() && Utils.isInDungeons() && event.entity instanceof PlayerEntity player && !Utils.isPlayer(player)) {
            String name = player.getName().getString();
            for (String mini : minibossList) {
                if (name.equals(mini)) {
                    Rendering.Entities.drawOutline(event.entity, true, minibossColor.value());
                    return;
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (!dungeonKeys.empty()) {
                for (Entity ent : dungeonKeys.get()) {
                    event.drawFilledWithBeam(Box.of(ent.getPos().add(0, 1.5, 0), 1, 1, 1), 256, true, witherKeyColor.value());
                }
            }
            if (!spiritBows.empty()) {
                for (Entity ent : spiritBows.get()) {
                    BlockPos ground = Utils.findGround(ent.getBlockPos(), 4);
                    Vec3d pos = ent.getPos();
                    Vec3d posAdjust = new Vec3d(pos.x, ground.up(1).getY() + 1, pos.z);
                    event.drawFilled(Box.of(posAdjust, 0.8, 1.75, 0.8), true, spiritBowColor.value());
                }
            }
        }
    }
}
