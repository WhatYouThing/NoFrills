package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Formatting;
import nofrills.events.EntityNamedEvent;
import nofrills.misc.Utils;

import java.util.List;
import java.util.regex.Pattern;

public class SlayerFeatures {
    private static final Pattern firePillarRegex = Pattern.compile("[0-9]s [0-9] hits");
    private static final Pattern bossTimerRegex = Pattern.compile(".*[0-9][0-9]:[0-9][0-9].*");
    private static final SlayerBoss[] slayerBosses = {
            new SlayerBoss("Revenant Horror", new String[]{"Revenant Horror", "Atoned Horror"}, EntityType.ZOMBIE),
            new SlayerBoss("Tarantula Broodfather", new String[]{"Tarantula Broodfather"}, EntityType.SPIDER),
            new SlayerBoss("Sven Packmaster", new String[]{"Sven Packmaster"}, EntityType.WOLF),
            new SlayerBoss("Voidgloom Seraph", new String[]{"Voidgloom Seraph"}, EntityType.ENDERMAN),
            new SlayerBoss("Riftstalker Bloodfiend", new String[]{"Bloodfiend"}, EntityType.PLAYER),
            new SlayerBoss("Inferno Demonlord", new String[]{"Inferno Demonlord"}, EntityType.BLAZE),
    };

    private static boolean isEntityValid(Entity entity, EntityType<?>[] validTypes) {
        if (entity.getType() == EntityType.ARMOR_STAND) {
            return entity.hasCustomName();
        } else {
            for (EntityType<?> type : validTypes) {
                if (entity.getType() == type) {
                    return true;
                }
            }
            return false;
        }
    }

    @EventHandler
    public static void onNamed(EntityNamedEvent event) {
        if (Utils.scoreboardLines.contains("Slay the boss!")) {
            if (bossTimerRegex.matcher(event.namePlain).matches()) {
                for (String line : Utils.scoreboardLines) {
                    if (line.startsWith("Inferno Demonlord")) {
                        String[] names = {"Inferno Demonlord", "ⓉⓎⓅⒽⓄⒺⓊⓈ", "ⓆⓊⒶⓏⒾⒾ"};
                        EntityType<?>[] types = {EntityType.BLAZE, EntityType.ZOMBIFIED_PIGLIN, EntityType.WITHER_SKELETON};
                        List<Entity> otherEntities = Utils.getNearbyEntities(event.entity, 0.7, 2, 0.7, ent -> isEntityValid(ent, types));
                        for (Entity ent : otherEntities) {
                            if (ent.getType() == EntityType.ARMOR_STAND) {
                                String name = Formatting.strip(ent.getCustomName().getString());
                                for (String bossName : names) {
                                    if (name.startsWith("☠ " + bossName)) {
                                        Entity nearest = Utils.findNametagOwner(event.entity, otherEntities);
                                        if (nearest != null) {
                                            if (event.namePlain.startsWith("IMMUNE")) {
                                                Utils.setRenderOutline(nearest, false, 0.0f, 0.0f, 0.0f, 0.0f);
                                            } else if (event.namePlain.startsWith("ASHEN")) {
                                                Utils.setRenderOutline(nearest, true, 0.55f, 0.55f, 0.55f, 1.0f);
                                            } else if (event.namePlain.startsWith("SPIRIT")) {
                                                Utils.setRenderOutline(nearest, true, 1.0f, 1.0f, 1.0f, 1.0f);
                                            } else if (event.namePlain.startsWith("AURIC")) {
                                                Utils.setRenderOutline(nearest, true, 1.0f, 1.0f, 0.0f, 1.0f);
                                            } else if (event.namePlain.startsWith("CRYSTAL")) {
                                                Utils.setRenderOutline(nearest, true, 0.0f, 1.0f, 1.0f, 1.0f);
                                            } else {
                                                Utils.setRenderOutline(nearest, true, 1.0f, 1.0f, 0.0f, 1.0f);
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static class SlayerBoss {
        public String scoreboardName;
        public String[] entityNames;
        public EntityType<?> entityType;

        public SlayerBoss(String scoreboardName, String[] entityNames, EntityType<?> entityType) {
            this.scoreboardName = scoreboardName;
            this.entityNames = entityNames;
            this.entityType = entityType;
        }
    }

    private static class CurrentBoss {
        public Entity bossEntity;
        public Entity healthDisplay;
        public Entity spawnerDisplay;
        public Entity statusDisplay;

        public CurrentBoss(Entity bossEntity, Entity healthDisplay, Entity spawnerDisplay, Entity statusDisplay) {
            this.bossEntity = bossEntity;
            this.healthDisplay = healthDisplay;
            this.spawnerDisplay = spawnerDisplay;
            this.statusDisplay = statusDisplay;
        }
    }
}
