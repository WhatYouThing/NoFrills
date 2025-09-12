package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class WitherDragons {
    public static final Feature instance = new Feature("witherDragons");

    public static final SettingBool alert = new SettingBool(false, "alert", instance.key());
    public static final SettingDouble power = new SettingDouble(0.0, "power", instance.key());
    public static final SettingDouble powerEasy = new SettingDouble(0.0, "powerEasy", instance.key());
    public static final SettingBool glow = new SettingBool(false, "glow", instance.key());
    public static final SettingBool boxes = new SettingBool(false, "boxes", instance.key());
    public static final SettingBool tracers = new SettingBool(false, "tracers", instance.key());
    public static final SettingBool stack = new SettingBool(false, "stack", instance.key());
    public static final SettingEnum<stackTypes> stackType = new SettingEnum<>(stackTypes.Simple, stackTypes.class, "stackType", instance.key());
    public static final SettingBool timer = new SettingBool(false, "timer", instance.key());
    public static final SettingBool health = new SettingBool(false, "health", instance.key());

    private static final List<Dragon> dragons = List.of( // box coordinates taken from odin's WitherDragonEnum xqcL
            new Dragon("Red", 3, 3, RenderColor.fromHex(0xff0000), SpawnBoxes.red, PartBoxes.red, new Box(14.5, 5, 45.5, 39.5, 28, 70.5)),
            new Dragon("Orange", 1, 5, RenderColor.fromHex(0xffaa00), SpawnBoxes.orange, PartBoxes.orange, new Box(72, 5, 47, 102, 28, 77)),
            new Dragon("Blue", 4, 2, RenderColor.fromHex(0x55ffff), SpawnBoxes.blue, PartBoxes.blue, new Box(71.5, 5, 82.5, 96.5, 26, 107.5)),
            new Dragon("Purple", 5, 1, RenderColor.fromHex(0xaa00aa), SpawnBoxes.purple, PartBoxes.purple, new Box(45.5, 6, 113.5, 68.5, 23, 136.5)),
            new Dragon("Green", 2, 4, RenderColor.fromHex(0x00ff00), SpawnBoxes.green, PartBoxes.green, new Box(7, 5, 80, 37, 28, 110))
    );
    private static final List<SpawnedDragon> spawnedDragons = new ArrayList<>();
    private static boolean dragonSplitDone = false;

    private static boolean isDragonPhase() {
        return mc.player != null && mc.player.getPos().getY() < 50 && Utils.isInDungeonBoss("7");
    }

    private static boolean isArcherTeam() {
        return switch (SkyblockData.dungeonClass.value()) {
            case "Archer", "Tank" -> true;
            default -> false;
        };
    }

    private static double getPowerLevel() {
        return SkyblockData.dungeonPower;
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
            if (drag.data.name.equals("Purple") && drag.entity != null) {
                Box box = drag.entity.getDimensions(EntityPose.STANDING).getBoxAt(drag.entity.getPos());
                if (dragon.area.intersects(box)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isEitherPurple(SpawnedDragon first, SpawnedDragon second) {
        return first.data.name.equals("Purple") || second.data.name.equals("Purple");
    }

    private static boolean doesDragonExist(Entity dragon) {
        for (SpawnedDragon drag : getSpawnedDragons()) {
            if (drag.entity != null && drag.uuid.equals(dragon.getUuidAsString())) {
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
        Utils.showTitleCustom(Utils.toUpper(drag.data.name) + " IS SPAWNING!", 60, -20, 4.0f, drag.data.color.hex);
        Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
        if (split) {
            Utils.infoRaw(Text.literal(drag.data.name + " is your priority dragon.").setStyle(Style.EMPTY.withColor(drag.data.color.hex)));
        } else if (dragonSplitDone) {
            Utils.infoRaw(Text.literal(drag.data.name + " is spawning.").setStyle(Style.EMPTY.withColor(drag.data.color.hex)));
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !spawnedDragons.isEmpty()) {
            List<SpawnedDragon> spawnedDrags = getSpawnedDragons();
            for (SpawnedDragon drag : spawnedDrags) {
                if (boxes.value()) {
                    event.drawOutline(drag.data.area, true, drag.data.color);
                }
                if (stack.value() && !drag.spawned) {
                    if (stackType.value().equals(stackTypes.Advanced)) {
                        for (Box part : drag.data.parts) {
                            event.drawOutline(part, true, drag.data.color);
                        }
                    } else {
                        event.drawFilled(drag.data.pos, true, RenderColor.fromHex(drag.data.color.hex, 0.67f));
                    }
                }
                if (timer.value() && !drag.spawned) {
                    event.drawText(drag.data.pos.getCenter().add(0, 4, 0), Text.of(Utils.formatDecimal(drag.spawnTicks / 20.0f, 3) + "s"), 0.3f, true, drag.data.color);
                }
                if (health.value() && drag.entity != null) {
                    Vec3d pos = drag.entity.getLerpedPos(event.tickCounter.getTickProgress(true)); // should make the text move smoothly with the dragons
                    event.drawText(pos, Text.of(Utils.formatDecimal(drag.health * 0.000001) + "M"), 0.2f, true, drag.data.color);
                }
            }
            if (tracers.value()) {
                SpawnedDragon drag = spawnedDrags.size() == 2 ? getHigherPriority(spawnedDrags.getFirst(), spawnedDrags.get(1), isArcherTeam()) : spawnedDrags.getFirst();
                if (!drag.spawned) {
                    event.drawTracer(drag.data.pos.getCenter(), drag.data.color);
                }
            }
        }
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && isDragonParticle(event.packet) && isDragonPhase()) {
            for (Dragon drag : dragons) {
                if (!isDragonSpawned(drag) && drag.area.contains(event.pos) && !isPurpleInArea(drag)) {
                    SpawnedDragon spawnedDragon = new SpawnedDragon(drag);
                    spawnedDragons.add(spawnedDragon);
                    List<SpawnedDragon> dragons = getSpawnedDragons();
                    if (!dragonSplitDone && dragons.size() == 2) {
                        if (alert.value()) {
                            double currentPower = getPowerLevel();
                            SpawnedDragon first = dragons.getFirst();
                            SpawnedDragon second = dragons.getLast();
                            if ((currentPower >= powerEasy.value() && isEitherPurple(first, second)) || currentPower >= power.value()) {
                                announceDragonSpawn(getHigherPriority(first, second, isArcherTeam()), true);
                            } else { // no split
                                announceDragonSpawn(getHigherPriority(first, second, true), true);
                            }
                            dragonSplitDone = true;
                        }
                    } else if (dragonSplitDone) {
                        if (alert.value()) {
                            announceDragonSpawn(spawnedDragon, false);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (instance.isActive() && event.entity instanceof EnderDragonEntity dragonEntity && isDragonPhase()) {
            float health = dragonEntity.getHealth();
            String uuid = dragonEntity.getUuidAsString();
            for (SpawnedDragon drag : getSpawnedDragons()) {
                if (drag.spawning && !doesDragonExist(event.entity) && drag.data.area.contains(event.entity.getPos())) {
                    drag.entity = event.entity;
                    drag.uuid = uuid;
                    drag.health = health;
                    drag.spawning = false;
                    drag.spawned = true;
                    if (glow.value()) {
                        Rendering.Entities.drawGlow(event.entity, true, drag.data.color);
                    }
                } else if (drag.spawned && uuid.equals(drag.uuid)) {
                    if (dragonEntity.isAlive() && dragonEntity.ticksSinceDeath == 0) {
                        drag.health = health;
                        drag.entity = event.entity;
                        if (glow.value()) {
                            Rendering.Entities.drawGlow(event.entity, true, drag.data.color);
                        }
                    } else {
                        spawnedDragons.removeIf(dragon -> dragon.data.name.equals(drag.data.name));
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && isDragonPhase()) {
            for (SpawnedDragon drag : getSpawnedDragons()) {
                if (!drag.spawned && drag.spawnTicks > 0) {
                    drag.spawnTicks--;
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        dragonSplitDone = false;
        spawnedDragons.clear();
    }

    public enum stackTypes {
        Simple,
        Advanced
    }

    private static class Dragon {
        public String name;
        public int archPriority;
        public int bersPriority;
        public RenderColor color;
        public Box pos;
        public List<Box> parts;
        public Box area;

        public Dragon(String name, int archPriority, int bersPriority, RenderColor color, Box pos, List<Box> parts, Box area) {
            this.name = name;
            this.archPriority = archPriority;
            this.bersPriority = bersPriority;
            this.color = color;
            this.pos = pos;
            this.parts = parts;
            this.area = area;
        }
    }

    private static class SpawnedDragon {
        public Dragon data;
        public Entity entity = null;
        public String uuid = "";
        public int spawnTicks = 100;
        public boolean spawned = false;
        public boolean spawning = true;
        public float health = 0.0f;

        public SpawnedDragon(Dragon data) {
            this.data = data;
        }
    }

    private static class SpawnBoxes {
        public static Box red = Box.of(new Vec3d(27.0, 14.0, 59.0), 1, 1, 1);
        public static Box orange = Box.of(new Vec3d(85.0, 14.0, 56.0), 1, 1, 1);
        public static Box blue = Box.of(new Vec3d(84.0, 14.0, 94.0), 1, 1, 1);
        public static Box purple = Box.of(new Vec3d(56.0, 14.0, 125.0), 1, 1, 1);
        public static Box green = Box.of(new Vec3d(27.0, 14.0, 94.0), 1, 1, 1);
    }

    private static class PartBoxes {
        public static List<Box> red = List.of(
                new Box(26.5, 14.0, 52.0, 27.5, 15.0, 53.0),
                new Box(25.5, 14.0, 52.0, 28.5, 17.0, 55.0),
                new Box(24.5, 14.0, 56.0, 29.5, 17.0, 61.0),
                new Box(26.0, 15.5, 61.5, 28.0, 17.5, 63.5),
                new Box(26.0, 15.5, 63.5, 28.0, 17.5, 65.5),
                new Box(26.0, 15.5, 65.5, 28.0, 17.5, 67.5),
                new Box(29.5, 16.0, 57.0, 33.5, 18.0, 61.0),
                new Box(20.5, 16.0, 57.0, 24.5, 18.0, 61.0)
        );
        public static List<Box> orange = List.of(
                new Box(84.5, 14.0, 49.0, 85.5, 15.0, 50.0),
                new Box(83.5, 14.0, 49.0, 86.5, 17.0, 52.0),
                new Box(82.5, 14.0, 53.0, 87.5, 17.0, 58.0),
                new Box(84.0, 15.5, 58.5, 86.0, 17.5, 60.5),
                new Box(84.0, 15.5, 60.5, 86.0, 17.5, 62.5),
                new Box(84.0, 15.5, 62.5, 86.0, 17.5, 64.5),
                new Box(87.5, 16.0, 54.0, 91.5, 18.0, 58.0),
                new Box(78.5, 16.0, 54.0, 82.5, 18.0, 58.0)
        );
        public static List<Box> blue = List.of(
                new Box(83.5, 14.0, 87.0, 84.5, 15.0, 88.0),
                new Box(82.5, 14.0, 87.0, 85.5, 17.0, 90.0),
                new Box(81.5, 14.0, 91.0, 86.5, 17.0, 96.0),
                new Box(83.0, 15.5, 96.5, 85.0, 17.5, 98.5),
                new Box(83.0, 15.5, 98.5, 85.0, 17.5, 100.5),
                new Box(83.0, 15.5, 100.5, 85.0, 17.5, 102.5),
                new Box(86.5, 16.0, 92.0, 90.5, 18.0, 96.0),
                new Box(77.5, 16.0, 92.0, 81.5, 18.0, 96.0));
        public static List<Box> purple = List.of(
                new Box(55.5, 14.0, 118.0, 56.5, 15.0, 119.0),
                new Box(54.5, 14.0, 118.0, 57.5, 17.0, 121.0),
                new Box(53.5, 14.0, 122.0, 58.5, 17.0, 127.0),
                new Box(55.0, 15.5, 127.5, 57.0, 17.5, 129.5),
                new Box(55.0, 15.5, 129.5, 57.0, 17.5, 131.5),
                new Box(55.0, 15.5, 131.5, 57.0, 17.5, 133.5),
                new Box(58.5, 16.0, 123.0, 62.5, 18.0, 127.0),
                new Box(49.5, 16.0, 123.0, 53.5, 18.0, 127.0)
        );
        public static List<Box> green = List.of(
                new Box(26.5, 14.0, 87.0, 27.5, 15.0, 88.0),
                new Box(25.5, 14.0, 87.0, 28.5, 17.0, 90.0),
                new Box(24.5, 14.0, 91.0, 29.5, 17.0, 96.0),
                new Box(26.0, 15.5, 96.5, 28.0, 17.5, 98.5),
                new Box(26.0, 15.5, 98.5, 28.0, 17.5, 100.5),
                new Box(26.0, 15.5, 100.5, 28.0, 17.5, 102.5),
                new Box(29.5, 16.0, 92.0, 33.5, 18.0, 96.0),
                new Box(20.5, 16.0, 92.0, 24.5, 18.0, 96.0)
        );
    }
}
