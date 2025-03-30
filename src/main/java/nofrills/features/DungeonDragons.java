package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Config;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class DungeonDragons {
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private static final List<Dragon> dragons = List.of( // box coordinates taken from odin's WitherDragonEnum xqcL
            new Dragon("Red", 3, 3, RenderColor.fromHex(0xff0000), new BlockPos(27, 14, 59), new Box(14.5, 12, 45.5, 39.5, 28, 70.5)),
            new Dragon("Orange", 1, 5, RenderColor.fromHex(0xffaa00), new BlockPos(85, 14, 56), new Box(72, 8, 47, 102, 28, 77)),
            new Dragon("Blue", 4, 2, RenderColor.fromHex(0x55ffff), new BlockPos(84, 14, 94), new Box(71.5, 11, 82.5, 96.5, 26, 107.5)),
            new Dragon("Purple", 5, 1, RenderColor.fromHex(0xaa00aa), new BlockPos(56, 14, 125), new Box(45.5, 12, 113.5, 68.5, 23, 136.5)),
            new Dragon("Green", 2, 4, RenderColor.fromHex(0x00ff00), new BlockPos(27, 14, 94), new Box(7, 8, 80, 37, 28, 110))
    );
    private static final List<SpawnedDragon> spawnedDragons = new ArrayList<>();
    private static boolean dragonSplitDone = false;

    private static boolean isDragonPhase() {
        if (mc.player != null) {
            Vec3d pos = mc.player.getPos();
            return Utils.isOnDungeonFloor("M7") && pos.getX() > 0 && pos.getY() < 50 && pos.getZ() > 0;
        }
        return false;
    }

    private static boolean isArcherTeam() {
        return switch (Config.dungeonClass) {
            case "Archer", "Tank" -> true;
            default -> false;
        };
    }

    private static double getPowerLevel() {
        double total = 0;
        for (String line : Utils.getFooterLines()) {
            if (line.startsWith("Blessing of Power")) {
                total += Utils.parseRoman(line.replace("Blessing of Power", "").trim());
            }
            if (line.startsWith("Blessing of Time")) {
                total += 0.5 * Utils.parseRoman(line.replace("Blessing of Time", "").trim());
            }
        }
        return total;
    }

    private static List<SpawnedDragon> getSpawnedDragons() {
        return new ArrayList<>(spawnedDragons);
    }

    private static boolean isDragonParticle(ParticleS2CPacket packet) {
        return packet.getParameters().getType().equals(ParticleTypes.FLAME) && packet.getCount() == 20
                && packet.getY() == 19 && packet.getOffsetX() == 2.0f && packet.getOffsetY() == 3.0f
                && packet.getOffsetZ() == 2.0f && packet.getSpeed() == 0.0f && packet.getX() % 1 == 0.0
                && packet.getZ() % 1 == 0.0;
    }

    private static boolean isDragonSpawned(Dragon dragon) {
        for (SpawnedDragon drag : getSpawnedDragons()) {
            if (dragon.name.equals(drag.data.name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPurpleInArea(Dragon dragon) {
        for (SpawnedDragon drag : getSpawnedDragons()) {
            if (drag.data.name.equals("Purple") && drag.entity != null && dragon.area.contains(drag.entity.getPos())) {
                return true;
            }
        }
        return false;
    }

    private static boolean doesDragonExist(Entity dragon) {
        for (SpawnedDragon drag : getSpawnedDragons()) {
            if (drag.entity != null && drag.entity.getUuidAsString().equals(dragon.getUuidAsString())) {
                return true;
            }
        }
        return false;
    }

    private static SpawnedDragon getHigherPriority(SpawnedDragon first, SpawnedDragon second, boolean archerTeam) {
        if (archerTeam) {
            return first.data.archPriority > second.data.archPriority ? first : second;
        } else {
            return first.data.bersPriority > second.data.bersPriority ? first : second;
        }
    }

    private static void announceDragonSpawn(SpawnedDragon drag, boolean split) {
        Utils.showTitleCustom(drag.data.name.toUpperCase() + " IS SPAWNING!", 60, -20, 4.0f, drag.data.color.hex);
        Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
        if (split) {
            Utils.infoRaw(Text.literal(drag.data.name + " is your priority dragon.").setStyle(Style.EMPTY.withColor(drag.data.color.hex)));
        } else if (dragonSplitDone) {
            Utils.infoRaw(Text.literal(drag.data.name + " is spawning.").setStyle(Style.EMPTY.withColor(drag.data.color.hex)));
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (!spawnedDragons.isEmpty()) {
            for (SpawnedDragon drag : getSpawnedDragons()) {
                if (Config.dragBoxes) {
                    event.drawOutline(drag.data.area, false, drag.data.color);
                }
                if (Config.dragStack && !drag.spawned) {
                    event.drawFilled(Box.enclosing(drag.data.spawnPos, drag.data.spawnPos), true, RenderColor.fromHex(drag.data.color.hex, 0.67f));
                }
                if (Config.dragTimer && !drag.spawned) {
                    event.drawText(drag.data.area.getCenter(), Text.of(decimalFormat.format(drag.spawnTicks / 20.0f) + "s"), 0.2f, true, drag.data.color);
                }
                if (Config.dragHealth && drag.entity != null) {
                    Vec3d pos = drag.entity.getLerpedPos(event.tickCounter.getTickDelta(true)); // should make the text move smoothly with the dragons
                    event.drawText(pos, Text.of(decimalFormat.format(drag.health * 0.000001) + "M"), 0.2f, true, drag.data.color);
                }
            }
        }
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (isDragonPhase() && isDragonParticle(event.packet)) {
            Vec3d pos = new Vec3d(event.packet.getX(), event.packet.getY(), event.packet.getZ());
            for (Dragon drag : dragons) {
                if (!isDragonSpawned(drag) && drag.area.contains(pos) && !isPurpleInArea(drag)) {
                    SpawnedDragon spawnedDragon = new SpawnedDragon(drag);
                    spawnedDragons.add(spawnedDragon);
                    List<SpawnedDragon> dragons = getSpawnedDragons();
                    if (!dragonSplitDone && dragons.size() == 2) {
                        if (Config.dragAlert) {
                            double power = getPowerLevel();
                            SpawnedDragon first = dragons.getFirst();
                            SpawnedDragon second = dragons.getLast();
                            boolean purple = first.data.name.equals("Purple") || second.data.name.equals("Purple");
                            if ((power >= Config.dragSkipEasy && purple) || power >= Config.dragSkip) {
                                announceDragonSpawn(getHigherPriority(first, second, isArcherTeam()), true);
                            } else { // no split
                                announceDragonSpawn(getHigherPriority(first, second, true), true);
                            }
                            dragonSplitDone = true;
                        }
                    } else if (dragonSplitDone) {
                        if (Config.dragAlert) {
                            announceDragonSpawn(spawnedDragon, false);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (event.entity instanceof EnderDragonEntity dragonEntity && isDragonPhase()) {
            float health = dragonEntity.getHealth();
            for (SpawnedDragon drag : getSpawnedDragons()) {
                if (drag.spawning && !doesDragonExist(event.entity) && drag.data.area.contains(event.entity.getPos())) {
                    drag.entity = event.entity;
                    drag.health = health;
                    drag.spawning = false;
                    drag.spawned = true;
                    if (Config.dragGlow) {
                        Rendering.Entities.drawGlow(event.entity, true, drag.data.color);
                    }
                } else if (drag.spawned && event.entity.getUuidAsString().equals(drag.entity.getUuidAsString())) {
                    if (health > 0.0f && !event.entity.isRemoved()) {
                        drag.health = health;
                    } else {
                        spawnedDragons.removeIf(dragon -> dragon.data.name.equals(drag.data.name));
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        for (SpawnedDragon drag : getSpawnedDragons()) {
            if (!drag.spawned && drag.spawnTicks > 0) {
                drag.spawnTicks--;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        dragonSplitDone = false;
        spawnedDragons.clear();
    }

    private static class Dragon {
        public String name;
        public int archPriority;
        public int bersPriority;
        public RenderColor color;
        public BlockPos spawnPos;
        public Box area;

        public Dragon(String name, int archPriority, int bersPriority, RenderColor color, BlockPos spawnPos, Box area) {
            this.name = name;
            this.archPriority = archPriority;
            this.bersPriority = bersPriority;
            this.color = color;
            this.spawnPos = spawnPos;
            this.area = area;
        }
    }

    private static class SpawnedDragon {
        public Dragon data;
        public Entity entity = null;
        public int spawnTicks = 100;
        public boolean spawned = false;
        public boolean spawning = true;
        public float health = 0.0f;

        public SpawnedDragon(Dragon data) {
            this.data = data;
        }
    }
}
