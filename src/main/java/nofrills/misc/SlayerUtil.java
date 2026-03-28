package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldTickEvent;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class SlayerUtil {
    public static final SlayerBoss revenant = new SlayerBoss("Revenant Horror", List.of("Revenant Horror", "Atoned Horror"), ent -> ent instanceof Zombie);
    public static final SlayerBoss tarantula = new SlayerBoss("Tarantula Broodfather", List.of("Tarantula Broodfather", "Conjoined Brood"), ent -> ent instanceof Spider && !(ent instanceof CaveSpider));
    public static final SlayerBoss sven = new SlayerBoss("Sven Packmaster", List.of("Sven Packmaster"), ent -> ent instanceof Wolf);
    public static final SlayerBoss voidgloom = new SlayerBoss("Voidgloom Seraph", List.of("Voidgloom Seraph"), ent -> ent instanceof EnderMan);
    public static final SlayerBoss vampire = new SlayerBoss("Riftstalker Bloodfiend", List.of("Bloodfiend"), ent -> ent instanceof Player player && !Utils.isPlayer(player));
    public static final SlayerBoss blaze = new SlayerBoss("Inferno Demonlord", List.of("Inferno Demonlord", "ⓉⓎⓅⒽⓄⒺⓊⓈ", "ⓆⓊⒶⓏⒾⒾ"), ent -> ent instanceof Blaze || ent instanceof ZombifiedPiglin || ent instanceof WitherSkeleton);
    public static final List<SlayerBoss> bossList = List.of(revenant, tarantula, sven, voidgloom, vampire, blaze);

    private static final Pattern bossTimerRegex = Pattern.compile(".*[0-9][0-9]:[0-9][0-9].*");
    private static final Predicate<Entity> predicate = entity -> entity.isAlive() && Utils.isMob(entity);
    private static final EntityCache spawnerCache = new EntityCache();
    private static final EntityCache timerCache = new EntityCache();
    private static final EntityCache nameCache = new EntityCache();
    private static final EntityCache bossCache = new EntityCache();
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

    public static ArmorStand getSpawnerEntity() {
        return (ArmorStand) spawnerCache.getFirst();
    }

    public static ArmorStand getTimerEntity() {
        return (ArmorStand) timerCache.getFirst();
    }

    public static ArmorStand getNameEntity() {
        return (ArmorStand) nameCache.getFirst();
    }

    public static LivingEntity getBossEntity() {
        return (LivingEntity) bossCache.getFirst();
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
            spawnerCache.add(event.entity);
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (currentBoss != null) {
            Entity spawner = getSpawnerEntity();
            if (spawner == null) return;
            for (Entity entity : Utils.getOtherEntities(spawner, 0.5, 2.0, 0.5, predicate)) {
                if (entity instanceof ArmorStand stand) {
                    String name = Utils.toPlain(stand.getName());
                    if (isTimer(name)) {
                        timerCache.add(entity);
                    }
                    if (isName(name)) {
                        nameCache.add(entity);
                    }
                    continue;
                }
                if (currentBoss.predicate.test(entity)) {
                    bossCache.add(entity);
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
