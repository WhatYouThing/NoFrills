package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EventListener;
import nofrills.events.WorldTickEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

@EventListener
public class SlayerUtil {
    public static final SlayerBoss REVENANT = new SlayerBoss("Revenant Horror", List.of("Revenant Horror", "Atoned Horror"), ent -> ent instanceof Zombie);
    public static final SlayerBoss TARANTULA = new SlayerBoss("Tarantula Broodfather", List.of("Tarantula Broodfather", "Conjoined Brood"), ent -> ent instanceof Spider && !(ent instanceof CaveSpider));
    public static final SlayerBoss SVEN = new SlayerBoss("Sven Packmaster", List.of("Sven Packmaster"), ent -> ent instanceof Wolf);
    public static final SlayerBoss VOIDGLOOM = new SlayerBoss("Voidgloom Seraph", List.of("Voidgloom Seraph"), ent -> ent instanceof EnderMan);
    public static final SlayerBoss VAMPIRE = new SlayerBoss("Riftstalker Bloodfiend", List.of("Bloodfiend"), ent -> ent instanceof Player player && !Utils.isPlayer(player));
    public static final SlayerBoss BLAZE = new SlayerBoss("Inferno Demonlord", List.of("Inferno Demonlord", "ⓉⓎⓅⒽⓄⒺⓊⓈ", "ⓆⓊⒶⓏⒾⒾ"), ent -> ent instanceof Blaze || ent instanceof ZombifiedPiglin || ent instanceof WitherSkeleton);
    public static final List<SlayerBoss> bossList = List.of(REVENANT, TARANTULA, SVEN, VOIDGLOOM, VAMPIRE, BLAZE);

    private static final Pattern bossTimerRegex = Pattern.compile(".*[0-9][0-9]:[0-9][0-9].*");
    private static final MappedEntityCache<CurrentBoss> bossCache = new MappedEntityCache<>();
    public static boolean bossAlive = false;
    public static SlayerBoss currentBoss = null;

    public static boolean isSpawner(String name) {
        return name.equals("SLAYER BOSS");
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
        return (ArmorStand) getCurrentBoss().map(Map.Entry::getKey).orElse(null);
    }

    public static ArmorStand getTimerEntity() {
        return getCurrentBoss().map(b -> b.getValue().timer().get()).orElse(null);
    }

    public static ArmorStand getNameEntity() {
        return getCurrentBoss().map(b -> b.getValue().name().get()).orElse(null);
    }

    public static LivingEntity getBossEntity() {
        return getCurrentBoss().map(b -> b.getValue().boss().get()).orElse(null);
    }

    public static Optional<Map.Entry<Entity, CurrentBoss>> getCurrentBoss() {
        if (mc.player == null) return Optional.empty();
        return bossCache.get().stream()
                .min(Comparator.comparingDouble(b -> b.getKey().distanceTo(mc.player)));
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
            bossCache.add(event.entity, new CurrentBoss());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onTick(WorldTickEvent event) {
        if (currentBoss != null) {
            bossCache.get().forEach(b -> {
                for (Entity entity : Utils.getOtherEntities(b.getKey(), 1.0, 3.0, 1.0, e -> e.isAlive() && Utils.isMob(e))) {
                    if (entity instanceof ArmorStand stand) {
                        String name = Utils.toPlain(stand.getName());
                        if (isTimer(name)) {
                            b.getValue().setTimer(stand);
                        } else if (isName(name)) {
                            b.getValue().setName(stand);
                        }
                    } else if (currentBoss.predicate.test(entity)) {
                        b.getValue().setBoss(entity);
                    }
                }
            });
        }
    }

    public record CurrentBoss(AtomicReference<ArmorStand> timer, AtomicReference<ArmorStand> name,
                              AtomicReference<LivingEntity> boss) {

        public CurrentBoss() {
            this(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>());
        }

        public void setTimer(Entity timer) {
            this.timer().set((ArmorStand) timer);
        }

        public void setName(Entity name) {
            this.name().set((ArmorStand) name);
        }

        public void setBoss(Entity boss) {
            this.boss().set((LivingEntity) boss);
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
