package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;
import nofrills.events.*;
import nofrills.misc.*;

import java.util.List;

import static nofrills.Main.mc;

public class WitherDragons {
    public static final Feature instance = new Feature("witherDragons");

    public static final SettingBool alert = new SettingBool(false, "alert", instance);
    public static final SettingDouble power = new SettingDouble(0.0, "power", instance);
    public static final SettingDouble powerEasy = new SettingDouble(0.0, "powerEasy", instance);
    public static final SettingBool glow = new SettingBool(false, "glow", instance);
    public static final SettingBool boxes = new SettingBool(false, "boxes", instance);
    public static final SettingBool tracers = new SettingBool(false, "tracers", instance);
    public static final SettingBool stack = new SettingBool(false, "stack", instance);
    public static final SettingEnum<stackTypes> stackType = new SettingEnum<>(stackTypes.Simple, stackTypes.class, "stackType", instance);
    public static final SettingBool timer = new SettingBool(false, "timer", instance);
    public static final SettingBool health = new SettingBool(false, "health", instance);

    private static final List<Dragon> dragons = List.of(
            Dragon.RED,
            Dragon.ORANGE,
            Dragon.BLUE,
            Dragon.PURPLE,
            Dragon.GREEN
    );
    private static final EntityCache dragonCache = new EntityCache();
    private static boolean splitDone = false;

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

    private static boolean isDragonParticle(ParticleS2CPacket packet) {
        return packet.getParameters().getType().equals(ParticleTypes.FLAME) && packet.getCount() == 20
                && packet.getY() == 19 && packet.getOffsetX() == 2.0f && packet.getOffsetY() == 3.0f
                && packet.getOffsetZ() == 2.0f && packet.getSpeed() == 0.0f && packet.getX() % 1 == 0.0
                && packet.getZ() % 1 == 0.0;
    }

    private static boolean isPurpleInArea(Dragon dragon) {
        if (Dragon.PURPLE.hasEntity()) {
            Box box = Dragon.PURPLE.entity.getDimensions(EntityPose.STANDING).getBoxAt(Dragon.PURPLE.entity.getPos());
            return dragon.area.intersects(box);
        }
        return false;
    }

    private static boolean isEitherPurple(Dragon first, Dragon second) {
        return first == Dragon.PURPLE || second == Dragon.PURPLE;
    }

    private static Dragon getHigherPriority(Dragon first, Dragon second, boolean archerTeam) {
        if (archerTeam) {
            return first.archPriority > second.archPriority ? first : second;
        }
        return first.bersPriority > second.bersPriority ? first : second;
    }

    private static void announceSpawn(Dragon drag, boolean split) {
        Utils.showTitleCustom(Utils.toUpper(drag.name) + " IS SPAWNING!", 60, -20, 4.0f, drag.color);
        Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
        if (split) {
            Utils.infoRaw(Text.literal(drag.name + " is your priority dragon.").withColor(drag.color.hex));
        } else if (splitDone) {
            Utils.infoRaw(Text.literal(drag.name + " is spawning.").withColor(drag.color.hex));
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && isDragonPhase()) {
            for (Dragon drag : dragons) {
                if (boxes.value() && (drag.isSpawning() || drag.hasEntity())) {
                    event.drawOutline(drag.area, true, drag.color);
                }
                if (stack.value() && drag.isSpawning()) {
                    if (stackType.value().equals(stackTypes.Advanced)) {
                        for (Box part : drag.parts) {
                            event.drawOutline(part, true, drag.color);
                        }
                    } else {
                        event.drawFilled(drag.pos, true, RenderColor.fromHex(drag.color.hex, 0.67f));
                    }
                }
                if (timer.value() && drag.isSpawning()) {
                    MutableText timerText = Text.literal(Utils.formatDecimal(drag.spawnTicks / 20.0f, 3) + "s");
                    event.drawText(drag.pos.getCenter().add(0, 4, 0), timerText, 0.3f, true, drag.color);
                }
                if (health.value() && drag.hasEntity()) {
                    MutableText healthText = Text.literal(Utils.formatDecimal(drag.health * 0.000001) + "M");
                    Vec3d pos = drag.entity.getLerpedPos(event.tickCounter.getTickProgress(true)); // should make the text move smoothly with the dragons
                    event.drawText(pos, healthText, 0.2f, true, drag.color);
                }
            }
            if (tracers.value()) {
                List<Dragon> spawning = dragons.stream().filter(Dragon::isSpawning).toList();
                if (!spawning.isEmpty()) {
                    Dragon drag = spawning.size() == 2 ? getHigherPriority(spawning.getFirst(), spawning.get(1), isArcherTeam()) : spawning.getFirst();
                    event.drawTracer(drag.pos.getCenter(), drag.color);
                }
            }
        }
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && isDragonParticle(event.packet) && isDragonPhase()) {
            for (Dragon drag : dragons) {
                if (drag.spawnTicks == 0 && drag.area.contains(event.pos) && !isPurpleInArea(drag)) {
                    drag.startTicking();
                    List<Dragon> spawning = dragons.stream().filter(Dragon::isSpawning).toList();
                    if (!splitDone && spawning.size() == 2) {
                        if (alert.value()) {
                            double currentPower = getPowerLevel();
                            Dragon first = spawning.getFirst();
                            Dragon second = spawning.getLast();
                            if ((currentPower >= powerEasy.value() && isEitherPurple(first, second)) || currentPower >= power.value()) {
                                announceSpawn(getHigherPriority(first, second, isArcherTeam()), true);
                            } else { // no split
                                announceSpawn(getHigherPriority(first, second, true), true);
                            }
                            splitDone = true;
                        }
                    } else if (splitDone) {
                        if (alert.value()) {
                            announceSpawn(drag, false);
                        }
                    }
                }
            }
        }
    }

    // the dragons can be outside of render distance when spawned, so instead of trying to rely on the statue area
    // we can use their "collar" entities to accurately find each dragon regardless of where they are
    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (instance.isActive() && isDragonPhase()) {
            if (event.entity instanceof EnderDragonEntity dragon) {
                dragonCache.add(dragon);
                for (Dragon drag : dragons) {
                    if (!drag.hasEntity()) {
                        for (Entity collar : drag.cache.get()) {
                            if (dragon.distanceTo(collar) <= 8.0) {
                                drag.setEntity(dragon);
                                break;
                            }
                        }
                    } else if (drag.entity.equals(dragon)) {
                        drag.setEntity(dragon);
                        break;
                    }
                }
            }
            if (event.entity instanceof ArmorStandEntity stand) {
                for (Dragon drag : dragons) {
                    if (drag.isCollar(stand)) {
                        drag.cache.add(stand);
                        for (Entity dragon : dragonCache.get()) {
                            if (dragon.distanceTo(stand) <= 8.0) {
                                drag.setEntity((EnderDragonEntity) dragon);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && isDragonPhase()) {
            for (Dragon drag : dragons) {
                drag.tick();
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        splitDone = false;
        dragonCache.clear();
        for (Dragon drag : dragons) {
            drag.reset();
        }
    }

    public enum stackTypes {
        Simple,
        Advanced
    }

    private static class Dragon { // box coordinates taken from odin's WitherDragonEnum xqcL
        public static final Dragon RED = new Dragon(
                "Red",
                3,
                3,
                "c20ef06dd60499766ac8ce15d2bea41d2813fe55718864b52dc41cbaae1ea913",
                RenderColor.fromHex(0xff0000),
                Box.of(new Vec3d(27.0, 14.0, 59.0), 1, 1, 1),
                List.of(
                        new Box(26.5, 14.0, 52.0, 27.5, 15.0, 53.0),
                        new Box(25.5, 14.0, 52.0, 28.5, 17.0, 55.0),
                        new Box(24.5, 14.0, 56.0, 29.5, 17.0, 61.0),
                        new Box(26.0, 15.5, 61.5, 28.0, 17.5, 63.5),
                        new Box(26.0, 15.5, 63.5, 28.0, 17.5, 65.5),
                        new Box(26.0, 15.5, 65.5, 28.0, 17.5, 67.5),
                        new Box(29.5, 16.0, 57.0, 33.5, 18.0, 61.0),
                        new Box(20.5, 16.0, 57.0, 24.5, 18.0, 61.0)
                ),
                new Box(14.5, 5, 45.5, 39.5, 28, 70.5)
        );
        public static final Dragon ORANGE = new Dragon(
                "Orange",
                1,
                5,
                "aace6bb3aa4ccac031168202f6d4532597bcac6351059abd9d10b28610493aeb",
                RenderColor.fromHex(0xffaa00),
                Box.of(new Vec3d(85.0, 14.0, 56.0), 1, 1, 1),
                List.of(
                        new Box(84.5, 14.0, 49.0, 85.5, 15.0, 50.0),
                        new Box(83.5, 14.0, 49.0, 86.5, 17.0, 52.0),
                        new Box(82.5, 14.0, 53.0, 87.5, 17.0, 58.0),
                        new Box(84.0, 15.5, 58.5, 86.0, 17.5, 60.5),
                        new Box(84.0, 15.5, 60.5, 86.0, 17.5, 62.5),
                        new Box(84.0, 15.5, 62.5, 86.0, 17.5, 64.5),
                        new Box(87.5, 16.0, 54.0, 91.5, 18.0, 58.0),
                        new Box(78.5, 16.0, 54.0, 82.5, 18.0, 58.0)
                ),
                new Box(72, 5, 47, 102, 28, 77)
        );
        public static final Dragon BLUE = new Dragon(
                "Blue",
                4,
                2,
                "e4e71671db5f69d2c46a0d72766b249c1236d726782c00a0e22668df5772d4b9",
                RenderColor.fromHex(0x55ffff),
                Box.of(new Vec3d(84.0, 14.0, 94.0), 1, 1, 1),
                List.of(
                        new Box(83.5, 14.0, 87.0, 84.5, 15.0, 88.0),
                        new Box(82.5, 14.0, 87.0, 85.5, 17.0, 90.0),
                        new Box(81.5, 14.0, 91.0, 86.5, 17.0, 96.0),
                        new Box(83.0, 15.5, 96.5, 85.0, 17.5, 98.5),
                        new Box(83.0, 15.5, 98.5, 85.0, 17.5, 100.5),
                        new Box(83.0, 15.5, 100.5, 85.0, 17.5, 102.5),
                        new Box(86.5, 16.0, 92.0, 90.5, 18.0, 96.0),
                        new Box(77.5, 16.0, 92.0, 81.5, 18.0, 96.0)
                ),
                new Box(71.5, 5, 82.5, 96.5, 26, 107.5)
        );
        public static final Dragon PURPLE = new Dragon(
                "Purple",
                5,
                1,
                "cad8cc982786fb4d40b0b6e64a41f0d9736f9c26affb898f4a7faea88ccf8997",
                RenderColor.fromHex(0xaa00aa),
                Box.of(new Vec3d(56.0, 14.0, 125.0), 1, 1, 1),
                List.of(
                        new Box(55.5, 14.0, 118.0, 56.5, 15.0, 119.0),
                        new Box(54.5, 14.0, 118.0, 57.5, 17.0, 121.0),
                        new Box(53.5, 14.0, 122.0, 58.5, 17.0, 127.0),
                        new Box(55.0, 15.5, 127.5, 57.0, 17.5, 129.5),
                        new Box(55.0, 15.5, 129.5, 57.0, 17.5, 131.5),
                        new Box(55.0, 15.5, 131.5, 57.0, 17.5, 133.5),
                        new Box(58.5, 16.0, 123.0, 62.5, 18.0, 127.0),
                        new Box(49.5, 16.0, 123.0, 53.5, 18.0, 127.0)
                ),
                new Box(45.5, 6, 113.5, 68.5, 23, 136.5)
        );
        public static final Dragon GREEN = new Dragon(
                "Green",
                2,
                4,
                "816f0073c58703d8d41e55e0a3abb042b73f8c105bc41c2f02ffe33f0383cf0a",
                RenderColor.fromHex(0x00ff00),
                Box.of(new Vec3d(27.0, 14.0, 94.0), 1, 1, 1),
                List.of(
                        new Box(26.5, 14.0, 87.0, 27.5, 15.0, 88.0),
                        new Box(25.5, 14.0, 87.0, 28.5, 17.0, 90.0),
                        new Box(24.5, 14.0, 91.0, 29.5, 17.0, 96.0),
                        new Box(26.0, 15.5, 96.5, 28.0, 17.5, 98.5),
                        new Box(26.0, 15.5, 98.5, 28.0, 17.5, 100.5),
                        new Box(26.0, 15.5, 100.5, 28.0, 17.5, 102.5),
                        new Box(29.5, 16.0, 92.0, 33.5, 18.0, 96.0),
                        new Box(20.5, 16.0, 92.0, 24.5, 18.0, 96.0)
                ),
                new Box(7, 5, 80, 37, 28, 110)
        );

        public String name;
        public int archPriority;
        public int bersPriority;
        public String texture;
        public RenderColor color;
        public Box pos;
        public List<Box> parts;
        public Box area;
        public EnderDragonEntity entity = null;
        public float health = 0.0f;
        public int spawnTicks = 0;
        public EntityCache cache = new EntityCache();

        public Dragon(String name, int archPriority, int bersPriority, String texture, RenderColor color, Box pos, List<Box> parts, Box area) {
            this.name = name;
            this.archPriority = archPriority;
            this.bersPriority = bersPriority;
            this.texture = texture;
            this.color = color;
            this.pos = pos;
            this.parts = parts;
            this.area = area;
        }

        public boolean isSpawning() {
            return this.spawnTicks > 0;
        }

        public void startTicking() {
            this.spawnTicks = 100;
        }

        public void tick() {
            if (this.spawnTicks > 0) {
                this.spawnTicks--;
            }
        }

        public void reset() {
            this.entity = null;
            this.health = 0.0f;
            this.spawnTicks = 0;
            this.cache.clear();
        }

        public boolean hasEntity() {
            return EntityCache.exists(this.entity);
        }

        public void setEntity(EnderDragonEntity ent) {
            this.entity = ent;
            this.health = ent.getHealth();
            if (glow.value() && !Rendering.Entities.isDrawingGlow(ent)) {
                Rendering.Entities.drawGlow(ent, true, this.color);
            }
        }

        public boolean isCollar(ArmorStandEntity entity) {
            ItemStack helmet = Utils.getEntityArmor(entity).getFirst();
            return Utils.isTextureEqual(Utils.getTextures(helmet), this.texture);
        }
    }
}
