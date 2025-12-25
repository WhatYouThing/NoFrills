package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class SlayerUtil {
    public static final SlayerBoss revenant = new SlayerBoss("Revenant Horror", List.of("Revenant Horror", "Atoned Horror"), ent -> ent instanceof ZombieEntity);
    public static final SlayerBoss tarantula = new SlayerBoss("Tarantula Broodfather", List.of("Tarantula Broodfather", "Conjoined Brood"), ent -> ent instanceof SpiderEntity && !(ent instanceof CaveSpiderEntity));
    public static final SlayerBoss sven = new SlayerBoss("Sven Packmaster", List.of("Sven Packmaster"), ent -> ent instanceof WolfEntity);
    public static final SlayerBoss voidgloom = new SlayerBoss("Voidgloom Seraph", List.of("Voidgloom Seraph"), ent -> ent instanceof EndermanEntity);
    public static final SlayerBoss vampire = new SlayerBoss("Riftstalker Bloodfiend", List.of("Bloodfiend"), ent -> ent instanceof PlayerEntity player && !Utils.isPlayer(player));
    public static final SlayerBoss blaze = new SlayerBoss("Inferno Demonlord", List.of("Inferno Demonlord", "ⓉⓎⓅⒽⓄⒺⓊⓈ", "ⓆⓊⒶⓏⒾⒾ"), ent -> ent instanceof BlazeEntity || ent instanceof ZombifiedPiglinEntity || ent instanceof WitherSkeletonEntity);
    public static final List<SlayerBoss> bossList = List.of(revenant, tarantula, sven, voidgloom, vampire, blaze);

    private static final Pattern bossTimerRegex = Pattern.compile(".*[0-9][0-9]:[0-9][0-9].*");
    private static final Predicate<Entity> predicate = entity -> entity.isAlive() && Utils.isMob(entity);
    private static final HashMap<String, Entity> entities = new HashMap<>();
    public static boolean bossAlive = false;
    public static SlayerBoss currentBoss = null;

    public static boolean isSpawner(String name) {
        return name.equals(Utils.format("Spawned by: {}", mc.player.getName().getString()));
    }

    public static boolean isTimer(String name) {
        return bossTimerRegex.matcher(name).matches();
    }

    public static boolean isName(String name) {
        return currentBoss.entityNames.stream().anyMatch(name::contains);
    }

    public static boolean isFightingBoss(SlayerBoss boss) {
        return bossAlive && currentBoss != null && currentBoss.equals(boss);
    }

    public static ArmorStandEntity getSpawnerEntity() {
        return entities.containsKey("spawner") ? (ArmorStandEntity) entities.get("spawner") : null;
    }

    public static ArmorStandEntity getTimerEntity() {
        return entities.containsKey("timer") ? (ArmorStandEntity) entities.get("timer") : null;
    }

    public static ArmorStandEntity getNameEntity() {
        return entities.containsKey("name") ? (ArmorStandEntity) entities.get("name") : null;
    }

    public static LivingEntity getBossEntity() {
        return entities.containsKey("boss") ? (LivingEntity) entities.get("boss") : null;
    }

    public static void updateQuestState(List<String> lines) {
        bossAlive = lines.contains("Slay the boss!");
        for (String line : lines) {
            for (SlayerBoss boss : bossList) {
                if (line.startsWith(boss.bossName)) {
                    currentBoss = boss;
                    return;
                }
            }
        }
        currentBoss = null;
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (currentBoss != null && isSpawner(event.namePlain)) {
            entities.put("spawner", event.entity);
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (!entities.isEmpty()) {
            entities.entrySet().removeIf(entry -> !EntityCache.exists(entry.getValue()));
        }
        if (currentBoss != null) {
            Entity spawner = getSpawnerEntity();
            if (spawner == null) return;
            for (Entity entity : Utils.getOtherEntities(spawner, 0.5, 2.0, 0.5, predicate)) {
                if (entity instanceof ArmorStandEntity stand) {
                    String name = Utils.toPlain(stand.getName());
                    if (isTimer(name)) {
                        entities.put("timer", entity);
                    }
                    if (isName(name)) {
                        entities.put("name", entity);
                    }
                    continue;
                }
                if (currentBoss.predicate.test(entity)) {
                    entities.put("boss", entity);
                }
            }
        }
    }

    public static class SlayerBoss {
        public String bossName;
        public List<String> entityNames;
        public Predicate<Entity> predicate;

        public SlayerBoss(String bossName, List<String> entityNames, Predicate<Entity> predicate) {
            this.bossName = bossName;
            this.entityNames = entityNames;
            this.predicate = predicate;
        }
    }
}
