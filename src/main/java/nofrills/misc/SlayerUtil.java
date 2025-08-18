package nofrills.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class SlayerUtil {
    public static final SlayerBoss revenant = new SlayerBoss("Revenant Horror", List.of("Revenant Horror", "Atoned Horror"), ent -> ent instanceof ZombieEntity);
    public static final SlayerBoss tarantula = new SlayerBoss("Tarantula Broodfather", List.of("Tarantula Broodfather"), ent -> ent instanceof SpiderEntity && !(ent instanceof CaveSpiderEntity));
    public static final SlayerBoss sven = new SlayerBoss("Sven Packmaster", List.of("Sven Packmaster"), ent -> ent instanceof WolfEntity);
    public static final SlayerBoss voidgloom = new SlayerBoss("Voidgloom Seraph", List.of("Voidgloom Seraph"), ent -> ent instanceof EndermanEntity);
    public static final SlayerBoss vampire = new SlayerBoss("Riftstalker Bloodfiend", List.of("Bloodfiend"), ent -> ent instanceof PlayerEntity player && !Utils.isPlayer(player));
    public static final SlayerBoss blaze = new SlayerBoss("Inferno Demonlord", List.of("Inferno Demonlord", "ⓉⓎⓅⒽⓄⒺⓊⓈ", "ⓆⓊⒶⓏⒾⒾ"), ent -> ent instanceof BlazeEntity || ent instanceof ZombifiedPiglinEntity || ent instanceof WitherSkeletonEntity);
    public static final List<SlayerBoss> bossList = List.of(
            revenant, tarantula, sven, voidgloom, vampire, blaze
    );
    private static final Pattern bossTimerRegex = Pattern.compile(".*[0-9][0-9]:[0-9][0-9].*");
    public static boolean bossAlive = false;
    public static SlayerBoss currentBoss = null;

    public static boolean isSpawner(String name) {
        return currentBoss != null && name.equals(Utils.format("Spawned by: {}", mc.player.getName().getString()));
    }

    public static boolean isTimer(String name) {
        return currentBoss != null && bossTimerRegex.matcher(name).matches();
    }

    public static boolean isName(String name) {
        return currentBoss != null && currentBoss.entityNames.stream().anyMatch(name::contains);
    }

    public static boolean isFightingBoss(SlayerBoss boss) {
        return bossAlive && currentBoss != null && currentBoss.equals(boss);
    }

    public static boolean isNearSpawner(Entity entity) {
        for (Entity ent : Utils.getOtherEntities(entity, 1, 3, 1, Utils::isMob)) {
            if (ent instanceof ArmorStandEntity && ent.getCustomName() != null && isSpawner(Formatting.strip(ent.getCustomName().getString()))) {
                return true;
            }
        }
        return false;
    }

    public static void updateQuestState(List<String> lines) {
        bossAlive = lines.contains("Slay the boss!");
        for (SlayerBoss boss : bossList) {
            for (String line : lines) {
                if (line.startsWith(boss.bossName)) {
                    currentBoss = boss;
                    return;
                }
            }
        }
        currentBoss = null;
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
