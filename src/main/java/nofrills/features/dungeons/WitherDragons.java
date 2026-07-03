package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;
import nofrills.events.*;
import nofrills.misc.DungeonUtil;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static nofrills.Main.mc;

@EventListener
public class WitherDragons {
    public static final Feature instance = new Feature("witherDragons");

    public static final SettingBool alert = new SettingBool(false, "alert", instance);
    public static final SettingDouble power = new SettingDouble(0.0, "power", instance);
    public static final SettingDouble powerEasy = new SettingDouble(0.0, "powerEasy", instance);
    public static final SettingBool boxes = new SettingBool(false, "boxes", instance);
    public static final SettingBool hitboxes = new SettingBool(false, "hitboxes", instance);
    public static final SettingBool tracers = new SettingBool(false, "tracers", instance);
    public static final SettingEnum<WaypointTypes> waypoints = new SettingEnum<>(WaypointTypes.Disabled, WaypointTypes.class, "stackType", instance);
    public static final SettingBool timer = new SettingBool(false, "timer", instance);
    public static final SettingBool health = new SettingBool(false, "health", instance);
    public static final SettingBool trackIceSpray = new SettingBool(false, "trackIceSpray", instance);
    public static final SettingBool trackArrowHits = new SettingBool(false, "trackArrowHits", instance);

    private static final List<Dragon> dragons = List.of(
            Dragon.RED,
            Dragon.ORANGE,
            Dragon.BLUE,
            Dragon.PURPLE,
            Dragon.GREEN
    );
    private static final HashMap<String, EntityCache> teammateArrows = new HashMap<>();
    private static final CopyOnWriteArrayList<FireBowPoint> firePoints = new CopyOnWriteArrayList<>();
    private static boolean splitDone = false;
    private static int tickCounter = 0;

    private static boolean isArcherTeam() {
        return DungeonUtil.isClass("Archer") || DungeonUtil.isClass("Tank");
    }

    private static double getPowerLevel() {
        return DungeonUtil.getPower();
    }

    private static boolean isDragonParticle(ClientboundLevelParticlesPacket packet) {
        return packet.getParticle().getType().equals(ParticleTypes.FLAME) && packet.getCount() == 20
                && packet.getY() == 19 && packet.getXDist() == 2.0f && packet.getYDist() == 3.0f
                && packet.getZDist() == 2.0f && packet.getMaxSpeed() == 0.0f && packet.getX() % 1 == 0.0
                && packet.getZ() % 1 == 0.0;
    }

    private static boolean isEitherPurple(Dragon first, Dragon second) {
        return first == Dragon.PURPLE || second == Dragon.PURPLE;
    }

    private static boolean isIceSprayEntity(ArmorStand stand) {
        if (stand.isMarker()) {
            ItemStack item = stand.getItemBySlot(EquipmentSlot.MAINHAND);
            return item.getItem().equals(Items.PACKED_ICE) && item.count() == 1 && Utils.getCustomData(item) == null;
        }
        return false;
    }

    private static Dragon getHigherPriority(Dragon first, Dragon second, boolean archerTeam) {
        if (archerTeam) {
            return first.archPriority > second.archPriority ? first : second;
        }
        return first.bersPriority > second.bersPriority ? first : second;
    }

    private static void announceSpawn(Dragon drag, boolean split) {
        MutableComponent title = Component.literal(Utils.toUpper(drag.name) + " IS SPAWNING").setStyle(Style.EMPTY.withBold(true).withColor(drag.color.hex));
        Utils.showTitle(title, Component.empty(), 0, 30, 10);
        Utils.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 0);
        Utils.infoRaw(Component.literal(drag.name).withColor(drag.color.hex).append(
                Component.literal(split ? " is your priority dragon." : " is spawning.").withStyle(ChatFormatting.GRAY)
        ));
    }

    private static void updateDragonEntities(Entity entity) {
        for (Dragon drag : dragons) {
            if (entity instanceof EnderDragon dragon) {
                if (!drag.hasEntity()) {
                    for (Entity collar : drag.collarCache.get()) {
                        if (Utils.horizontalDistance(dragon, collar) <= 10.0) {
                            drag.setEntity(dragon);
                            break;
                        }
                    }
                } else if (drag.getEntity().equals(dragon)) {
                    drag.setEntity(dragon);
                }
            } else if (entity instanceof ArmorStand stand) {
                if (drag.isCollar(stand)) {
                    drag.collarCache.add(stand);
                    for (Entity dragon : drag.dragonCache.get()) {
                        if (Utils.horizontalDistance(dragon, stand) <= 10.0) {
                            drag.setEntity((EnderDragon) dragon);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void onArrowMotion(Arrow arrow, Vec3 motion) {
        Vec2 rot = motion.rotation();
        if (Float.isNaN(rot.x) || Float.isNaN(rot.y) || mc.level == null) {
            return;
        }
        float rotPitch = Mth.wrapDegrees(rot.x);
        float rotYaw = Mth.wrapDegrees(rot.y);
        double pitchDiff = Math.clamp(10.0 / (Math.abs(motion.x) + Math.abs(motion.z)), 2.5, 20.0);
        for (AbstractClientPlayer player : new ArrayList<>(mc.level.players())) {
            if (!Utils.isPlayer(player)) continue;
            String name = player.getName().getString();
            List<FireBowPoint> points = player.equals(mc.player) ? firePoints : List.of(new FireBowPoint(
                    player.position(),
                    Mth.wrapDegrees(player.getXRot()),
                    Mth.wrapDegrees(player.getYRot()),
                    0
            ));
            for (FireBowPoint point : points) {
                if (Utils.difference(point.pitch + 90.0f, rotPitch + 90.0f) > pitchDiff) continue;
                if (point.pitch < -85.0 || point.pitch > 85.0) {
                    if (Utils.horizontalDistance(arrow.position(), point.pos) > 1.0) continue;
                } else {
                    if (Utils.difference(point.yaw + 180.0f, rotYaw + 180.0f) > 15.0f) continue;
                    if (Utils.horizontalDistance(arrow.position(), point.pos) > 4.0) continue;
                }
                if (!teammateArrows.containsKey(name)) {
                    teammateArrows.put(name, new EntityCache());
                }
                teammateArrows.get(name).add(arrow);
                return;
            }
        }
    }

    @EventHandler
    private static void onEntityRemoved(EntityRemovedEvent event) {
        if (instance.isActive() && trackArrowHits.value() && event.entity instanceof Arrow arrow && !arrow.isRemoved() && DungeonUtil.isInDragonPhase()) {
            AABB arrowHitbox = arrow.getBoundingBox().inflate(0.25);
            for (Map.Entry<String, EntityCache> entry : teammateArrows.entrySet()) {
                if (!entry.getValue().has(arrow)) continue;
                for (Dragon dragon : dragons) {
                    if (!dragon.hasEntity()) continue;
                    for (EnderDragonPart part : dragon.getEntity().getSubEntities()) {
                        if (arrowHitbox.intersects(part.getBoundingBox())) {
                            String name = entry.getKey();
                            dragon.arrowHits.put(name, dragon.arrowHits.getOrDefault(name, 0) + 1);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && DungeonUtil.isInDragonPhase()) {
            for (Dragon drag : dragons) {
                if (timer.value() && drag.isSpawning()) {
                    float seconds = drag.spawnTicks / 20.0f;
                    String timerText = Utils.format("{}{}s",
                            Utils.getPercentageColor(seconds / 5.0, true),
                            Utils.formatDecimal(seconds, 3)
                    );
                    event.drawText(drag.pos.getCenter().add(0.0, 4.0, 0.0), Component.literal(timerText), 0.3f, true, RenderColor.white);
                }
                if (boxes.value() && (drag.isSpawning() || drag.hasEntity())) {
                    event.drawOutline(drag.area, true, drag.color);
                }
                if (hitboxes.value() && drag.hasEntity()) {
                    for (EnderDragonPart part : drag.getEntity().getSubEntities()) {
                        event.drawOutline(Utils.getLerpedBox(part, event.delta()), false, drag.color);
                    }
                }
                WaypointTypes waypointType = waypoints.value();
                if (!waypointType.equals(WaypointTypes.Disabled) && drag.isSpawning()) {
                    switch (waypointType) {
                        case Simple -> event.drawFilled(drag.pos, false, drag.color.withAlpha(0.5f));
                        case Advanced -> {
                            for (AABB part : drag.parts) {
                                event.drawFilled(part, false, drag.color.withAlpha(0.33f));
                            }
                        }
                    }
                }
                if (health.value() && drag.hasEntity()) {
                    double maxHealth = drag.maxHealth > 0.0 ? drag.maxHealth : 200.0;
                    String healthText = Utils.format("{}{}M",
                            Utils.getPercentageColor(drag.health / maxHealth, true),
                            Utils.formatDecimal(drag.health * 0.000001)
                    );
                    Vec3 pos = drag.getEntity().getPosition(event.delta());
                    event.drawText(pos, Component.literal(healthText), 0.2f, true, RenderColor.white);
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
        if (instance.isActive() && isDragonParticle(event.packet) && DungeonUtil.isInDragonPhase()) {
            for (Dragon drag : dragons) {
                if (drag.spawnTicks == 0 && drag.area.contains(event.pos)) {
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

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (instance.isActive() && DungeonUtil.isInDragonPhase()) {
            if (event.entity instanceof ArmorStand stand && isIceSprayEntity(stand)) {
                if (!trackIceSpray.value()) return;
                for (Dragon dragon : dragons) {
                    if (!dragon.hasEntity()) continue;
                    EnderDragon entity = dragon.getEntity();
                    if (!dragon.iceSprayed && Utils.horizontalDistance(entity, stand) < 2.0 && stand.getY() > entity.getY()) {
                        Utils.infoRaw(Component.literal(dragon.name).withColor(dragon.color.hex).append(
                                Component.literal(Utils.format(" Ice Sprayed in {} ticks.", tickCounter - dragon.spawnedAt)).withStyle(ChatFormatting.GRAY)
                        ));
                        dragon.iceSprayed = true;
                        break;
                    }
                }
            } else {
                updateDragonEntities(event.entity);
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && DungeonUtil.isInDragonPhase()) {
            for (Dragon drag : dragons) {
                drag.tick();
            }
            firePoints.removeIf(point -> point.tick + 60 < tickCounter);
            tickCounter++;
        }
    }

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (instance.isActive() && (mc.options.keyAttack.isDown() || mc.options.keyUse.isDown()) && mc.player.isHolding(Items.BOW)) {
            Vec3 pos = mc.player.position();
            firePoints.add(new FireBowPoint(
                    new Vec3(pos.x, pos.y, pos.z),
                    Mth.wrapDegrees(mc.player.getXRot()),
                    Mth.wrapDegrees(mc.player.getYRot()),
                    tickCounter
            ));
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        teammateArrows.clear();
        firePoints.clear();
        splitDone = false;
        tickCounter = 0;
        for (Dragon drag : dragons) {
            drag.reset();
        }
    }

    public enum WaypointTypes {
        Disabled,
        Simple,
        Advanced
    }

    private record FireBowPoint(Vec3 pos, float pitch, float yaw, int tick) {
    }

    private static class Dragon { // box coordinates taken from odin's WitherDragonEnum xqcL
        public static final Dragon RED = new Dragon(
                "Red",
                3,
                3,
                "RED_KING_RELIC",
                RenderColor.fromHex(0xff0000),
                AABB.ofSize(new Vec3(27.0, 14.0, 59.0), 1, 1, 1),
                List.of(
                        new AABB(25.5, 14.0, 52.0, 28.5, 17.0, 55.0),
                        new AABB(24.5, 14.0, 56.0, 29.5, 17.0, 61.0),
                        new AABB(26.0, 15.5, 61.5, 28.0, 17.5, 67.5),
                        new AABB(29.5, 16.0, 57.0, 33.5, 18.0, 61.0),
                        new AABB(20.5, 16.0, 57.0, 24.5, 18.0, 61.0)
                ),
                new AABB(14.5, 5, 45.5, 39.5, 28, 70.5)
        );
        public static final Dragon ORANGE = new Dragon(
                "Orange",
                1,
                5,
                "ORANGE_KING_RELIC",
                RenderColor.fromHex(0xffaa00),
                AABB.ofSize(new Vec3(85.0, 14.0, 56.0), 1, 1, 1),
                List.of(
                        new AABB(83.5, 14.0, 49.0, 86.5, 17.0, 52.0),
                        new AABB(82.5, 14.0, 53.0, 87.5, 17.0, 58.0),
                        new AABB(84.0, 15.5, 58.5, 86.0, 17.5, 64.5),
                        new AABB(87.5, 16.0, 54.0, 91.5, 18.0, 58.0),
                        new AABB(78.5, 16.0, 54.0, 82.5, 18.0, 58.0)
                ),
                new AABB(72, 5, 47, 102, 28, 77)
        );
        public static final Dragon BLUE = new Dragon(
                "Blue",
                4,
                2,
                "BLUE_KING_RELIC",
                RenderColor.fromHex(0x55ffff),
                AABB.ofSize(new Vec3(84.0, 14.0, 94.0), 1, 1, 1),
                List.of(
                        new AABB(82.5, 14.0, 87.0, 85.5, 17.0, 90.0),
                        new AABB(81.5, 14.0, 91.0, 86.5, 17.0, 96.0),
                        new AABB(83.0, 15.5, 96.5, 85.0, 17.5, 102.5),
                        new AABB(86.5, 16.0, 92.0, 90.5, 18.0, 96.0),
                        new AABB(77.5, 16.0, 92.0, 81.5, 18.0, 96.0)
                ),
                new AABB(71.5, 5, 82.5, 96.5, 26, 107.5)
        );
        public static final Dragon PURPLE = new Dragon(
                "Purple",
                5,
                1,
                "PURPLE_KING_RELIC",
                RenderColor.fromHex(0xaa00aa),
                AABB.ofSize(new Vec3(56.0, 14.0, 125.0), 1, 1, 1),
                List.of(
                        new AABB(54.5, 14.0, 118.0, 57.5, 17.0, 121.0),
                        new AABB(53.5, 14.0, 122.0, 58.5, 17.0, 127.0),
                        new AABB(55.0, 15.5, 127.5, 57.0, 17.5, 133.5),
                        new AABB(58.5, 16.0, 123.0, 62.5, 18.0, 127.0),
                        new AABB(49.5, 16.0, 123.0, 53.5, 18.0, 127.0)
                ),
                new AABB(45.5, 6, 113.5, 68.5, 23, 136.5)
        );
        public static final Dragon GREEN = new Dragon(
                "Green",
                2,
                4,
                "GREEN_KING_RELIC",
                RenderColor.fromHex(0x00ff00),
                AABB.ofSize(new Vec3(27.0, 14.0, 94.0), 1, 1, 1),
                List.of(
                        new AABB(25.5, 14.0, 87.0, 28.5, 17.0, 90.0),
                        new AABB(24.5, 14.0, 91.0, 29.5, 17.0, 96.0),
                        new AABB(26.0, 15.5, 96.5, 28.0, 17.5, 102.5),
                        new AABB(29.5, 16.0, 92.0, 33.5, 18.0, 96.0),
                        new AABB(20.5, 16.0, 92.0, 24.5, 18.0, 96.0)
                ),
                new AABB(7, 5, 80, 37, 28, 110)
        );

        public final EntityCache dragonCache = EntityCache.create();
        public final EntityCache collarCache = EntityCache.create();
        public final ConcurrentHashMap<String, Integer> arrowHits = new ConcurrentHashMap<>();
        public final String name;
        public final int archPriority;
        public final int bersPriority;
        public final String relicID;
        public final RenderColor color;
        public final AABB pos;
        public final List<AABB> parts;
        public final AABB area;
        public float health = 0.0f;
        public double maxHealth = 200.0f;
        public int spawnTicks = 0;
        public int spawnedAt = 0;
        public boolean iceSprayed = false;

        public Dragon(String name, int archPriority, int bersPriority, String relicID, RenderColor color, AABB pos, List<AABB> parts, AABB area) {
            this.name = name;
            this.archPriority = archPriority;
            this.bersPriority = bersPriority;
            this.relicID = relicID;
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
            if (!this.hasEntity()) {
                if (!this.arrowHits.isEmpty()) {
                    String hitsText = this.arrowHits.entrySet().stream().map(e -> e.getKey() + " - " + e.getValue()).collect(Collectors.joining(", "));
                    Utils.infoRaw(Component.literal("Arrows hit on ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(this.name).withColor(this.color.hex))
                            .append(Component.literal(": " + hitsText).withStyle(ChatFormatting.GRAY))
                    );
                    this.arrowHits.clear();
                }
                if (this.iceSprayed) this.iceSprayed = false;
                if (this.spawnedAt != 0) this.spawnedAt = 0;
            }
        }

        public void reset() {
            this.iceSprayed = false;
            this.arrowHits.clear();
            this.health = 0.0f;
            this.maxHealth = 200.0f;
            this.spawnTicks = 0;
            this.spawnedAt = 0;
        }

        public boolean hasEntity() {
            return !this.dragonCache.empty() && this.dragonCache.getFirst().isAlive();
        }

        public EnderDragon getEntity() {
            return (EnderDragon) this.dragonCache.getFirst();
        }

        public void setEntity(EnderDragon ent) {
            this.dragonCache.add(ent);
            this.health = ent.getHealth(); // store the health value on update, required as the client appears to reset it on the next tick
            this.maxHealth = ent.getAttributeBaseValue(Attributes.MAX_HEALTH);
            if (this.spawnedAt == 0) {
                this.spawnedAt = tickCounter;
            }
        }

        public boolean isCollar(ArmorStand entity) {
            ItemStack helmet = Utils.getEntityHelmet(entity);
            return !helmet.isEmpty() && Utils.getSkyblockId(helmet).equals(this.relicID);
        }
    }
}
