package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Config;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;
import nofrills.mixin.BossBarHudAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static nofrills.Main.mc;

public class KuudraFeatures {
    private static final PickupSpot[] supplyPickupSpots = {
            // data borrowed from odin
            // originally i tried my own solution, but it was such voodoo that terry davis would resurrect himself to witness it
            new PickupSpot("X", new Vec3d(-142.5, 77.0, -151.0), 18.0f, 30.0f,
                    new PickupSpot("X Cannon", new Vec3d(-143.0, 76.0, -125.0), 16.0f, 0.0f, null)),
            new PickupSpot("Triangle", new Vec3d(-67.5, 77.0, -122.5), 18.0f, 15.0f,
                    new PickupSpot("Shop", new Vec3d(-81.0, 76.0, -143.0), 18.0f, 0.0f, null)),
            new PickupSpot("Slash", new Vec3d(-113.5, 77.0, -68.5), 18.0f, 15.0f,
                    new PickupSpot("Square", new Vec3d(-143.0, 76.0, -80.0), 20.0f, 0.0f, null)),
            new PickupSpot("Equals", new Vec3d(-65.5, 76.0, -87.5), 18.0f, 15.0f, null),
    };
    private static final List<Float> dpsData = new ArrayList<>();
    private static final Box stunBox = Box.enclosing(new BlockPos(-169, 26, -167), new BlockPos(-169, 26, -167));
    private static final List<Entity> supplies = new ArrayList<>();
    private static final List<Entity> dropOffs = new ArrayList<>();
    private static final List<Entity> buildPiles = new ArrayList<>();
    private static int freshTicks = 0;
    private static int missingTicks = 20;
    private static float previousHealth = 0.0f;
    private static MagmaCubeEntity kuudraEntity = null;
    private static boolean isStunning = false;

    private static kuudraPhases getCurrentPhase() {
        if (Utils.isInZone(-133, 59, -75, -73, 1, -138)) {
            return kuudraPhases.Lair;
        }
        for (String line : SkyblockData.getLines()) {
            if (line.startsWith("Rescue supplies")) {
                return kuudraPhases.Collect;
            }
            if (line.startsWith("Protect Elle")) {
                return kuudraPhases.Build;
            }
            for (String phaseLine : new String[]{"Figure out how to", "Charge Ballista or", "Shoot Ballista at", "Mine Kuudra's pods"}) {
                if (line.startsWith(phaseLine)) {
                    return kuudraPhases.DPS;
                }
            }
        }
        return kuudraPhases.Starting;
    }

    private static void updateKuudraEntity() {
        Entity kuudra = null;
        double maxY = 0;
        int cubesFound = 0;
        for (Entity ent : mc.world.getEntities()) {
            if (ent.getType() == EntityType.MAGMA_CUBE && ((MagmaCubeEntity) ent).getSize() == 30) {
                double y = ent.getPos().getY();
                cubesFound++;
                if (y > maxY) {
                    kuudra = ent;
                    maxY = y;
                }
            }
        }
        if (kuudra != null) {
            if (cubesFound == 2 || getCurrentPhase() == kuudraPhases.Lair) {
                // scuffed, but needed, because the average tps in kuudra is too low even for dr. disrespect
                kuudraEntity = (MagmaCubeEntity) kuudra;
            }
        }
    }

    private static float calculateDPS() {
        float total = 0.0f;
        for (float damage : dpsData) {
            total += damage;
        }
        return total / dpsData.size();
    }

    private static Vec3d getGround(Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos.getX(), Math.max(pos.getY(), 75), pos.getZ());
        BlockPos ground = Utils.findGround(blockPos, 4);
        return new Vec3d(pos.getX(), ground.toCenterPos().add(0, 0.5, 0).getY(), pos.getZ());
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (Utils.isInKuudra()) {
            RenderColor color = RenderColor.fromColor(Config.kuudraColor);
            kuudraPhases phase = getCurrentPhase();
            if (phase == kuudraPhases.Starting) {
                return;
            }
            if (Config.kuudraMissing && phase == kuudraPhases.Collect) {
                if (missingTicks == 0) {
                    PickupSpot preSpot = null;
                    Vec3d selfPos = mc.player.getPos();
                    for (PickupSpot pickupSpot : supplyPickupSpots) {
                        if (pickupSpot.spot.distanceTo(selfPos) < pickupSpot.maxPlayerDist) {
                            preSpot = pickupSpot;
                            Utils.info("§eYour Pre: " + pickupSpot.name);
                        }
                    }
                    if (preSpot != null) {
                        boolean preFound = false, secondaryFound = false;
                        for (Entity ent : mc.world.getEntities()) {
                            if (ent.getType() == EntityType.GIANT) {
                                Vec3d entPos = ent.getPos();
                                Vec3d supplyPos = new Vec3d(entPos.getX(), 76, entPos.getZ());
                                if (preSpot.spot.distanceTo(supplyPos) < preSpot.maxSupplyDist) {
                                    preFound = true;
                                }
                                if (preSpot.secondary != null) {
                                    if (preSpot.secondary.spot.distanceTo(supplyPos) < preSpot.secondary.maxSupplyDist) {
                                        secondaryFound = true;
                                    }
                                }
                            }
                        }
                        if (!preFound) {
                            Utils.sendMessage("/pc No " + preSpot.name + "!");
                        } else if (!secondaryFound && preSpot.secondary != null) {
                            Utils.sendMessage("/pc No " + preSpot.secondary.name + "!");
                        }
                    } else {
                        Utils.info("§eCouldn't find your Pre spot, meaning that you're probably AFK (or even worse, getting carried, very gross).");
                    }
                    missingTicks = -1;
                } else if (missingTicks > 0) {
                    missingTicks--;
                }
            }
            if (freshTicks > 0 && phase == kuudraPhases.Build) {
                Utils.showTitleCustom(Utils.format("FRESH: {}s", Utils.formatDecimal(freshTicks / 20f)), 1, 25, 2.5f, 0x55ff55);
            }
            if (Config.kuudraStunWaypoint) {
                isStunning = phase == kuudraPhases.DPS && mc.player.getPos().getY() <= 60;
            }
            if (kuudraEntity == null || !kuudraEntity.isAlive()) {
                // alive check is needed in case kuudra goes out of render distance, and we need to find him again.
                if (Config.kuudraHealth && phase == kuudraPhases.DPS) {
                    Collection<ClientBossBar> bossBars = ((BossBarHudAccessor) mc.inGameHud.getBossBarHud()).getBossBars().values();
                    if (!bossBars.isEmpty()) {
                        float health = ((ClientBossBar) bossBars.toArray()[0]).getPercent();
                        Utils.showTitleCustom(Utils.format("KUUDRA: {}% HP", Utils.formatDecimal(health * 100)), 1, 25, 2.5f, color.hex);
                    }
                }
                updateKuudraEntity();
            } else {
                if (Config.kuudraHitbox && !Rendering.Entities.isDrawingOutline(kuudraEntity)) {
                    Rendering.Entities.drawOutline(kuudraEntity, true, color);
                }
                if (Config.kuudraHealth && phase == kuudraPhases.DPS) {
                    float health = kuudraEntity.getHealth() / kuudraEntity.getMaxHealth();
                    Utils.showTitleCustom(Utils.format("KUUDRA: {}% HP", Utils.formatDecimal(health)), 1, 25, 2.5f, color.hex);
                }
                if (Config.kuudraDPS && phase == kuudraPhases.Lair && !Utils.isInstanceOver()) {
                    Utils.showTitleCustom(Utils.format("DPS: {}M", Utils.formatDecimal(calculateDPS() * 20 * 0.000001)), 1, 25, 2.5f, color.hex);
                }
            }
        }
    }

    @EventHandler
    private static void onChatMsg(ChatMsgEvent event) {
        if (Utils.isInKuudra()) {
            String msg = event.getPlainMessage();
            if (msg.equals("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!")) {
                if (Config.kuudraFresh && !Config.kuudraFreshMsg.isEmpty()) {
                    Utils.sendMessage(Config.kuudraFreshMsg);
                }
                if (Config.kuudraFreshTimer) {
                    freshTicks = 200;
                }
            }
            if (Config.kuudraDrain) {
                if (msg.startsWith("Used Extreme Focus!")) {
                    String mana = msg.replace("Used Extreme Focus! (", "").replace(" Mana)", "");
                    int players = 0;
                    for (Entity ent : mc.world.getEntities()) {
                        if (ent instanceof PlayerEntity player && player != mc.player) {
                            if (Utils.isPlayer(player) && !player.isInvisible() && player.distanceTo(mc.player) <= 5) {
                                players++;
                            }
                        }
                    }
                    if (!Config.kuudraDrainMsg.isEmpty()) {
                        String drainMsg = Config.kuudraDrainMsg.replace("{mana}", mana).replace("{players}", "" + players);
                        Utils.sendMessage(drainMsg);
                    }
                    event.cancel();
                }
                if (msg.startsWith("You now have") && msg.contains("Damage Resistance for 5 seconds")) {
                    event.cancel();
                }
                if (msg.equals("Your Extreme Focus has worn off.")) {
                    event.cancel();
                }
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (Utils.isInKuudra()) {
            if (freshTicks > 0) {
                freshTicks--;
            }
            if (Config.kuudraDPS && getCurrentPhase() == kuudraPhases.Lair && !Utils.isInstanceOver()) {
                float health = Utils.getTrueHealth(kuudraEntity.getHealth());
                float damage = Math.clamp(previousHealth - health, 0, 240_000_000);
                dpsData.add(damage);
                if (dpsData.size() > 20) {
                    dpsData.removeFirst();
                }
                previousHealth = health;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        freshTicks = 0;
        kuudraEntity = null;
        missingTicks = 20;
        previousHealth = 0.0f;
        dpsData.clear();
        supplies.clear();
        dropOffs.clear();
        buildPiles.clear();
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (Utils.isInKuudra()) {
            if (Config.kuudraSupplyHighlight && event.namePlain.equals("SUPPLIES")) {
                if (supplies.stream().noneMatch(ent -> ent.getUuidAsString().equals(event.entity.getUuidAsString()))) {
                    supplies.add(event.entity);
                }
            }
            if (Config.kuudraDropHighlight && event.namePlain.equals("BRING SUPPLY CHEST HERE")) {
                if (dropOffs.stream().noneMatch(ent -> ent.getUuidAsString().equals(event.entity.getUuidAsString()))) {
                    dropOffs.add(event.entity);
                }
            }
            if (Config.kuudraBuildHighlight && event.namePlain.startsWith("PROGRESS: ") && event.namePlain.endsWith("%")) {
                if (buildPiles.stream().noneMatch(ent -> ent.getUuidAsString().equals(event.entity.getUuidAsString()))) {
                    buildPiles.add(event.entity);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (isStunning) {
            event.drawFilled(stunBox, true, RenderColor.fromColor(Config.kuudraStunColor));
            event.drawText(stunBox.getCenter().add(0, 2, 0), Text.of("Stun"), 0.1f, true, RenderColor.fromHex(0xffffff));
        }
        if (!supplies.isEmpty()) {
            for (Entity supply : new ArrayList<>(supplies)) {
                if (supply.isAlive()) {
                    event.drawBeam(getGround(supply.getPos()), 256, true, RenderColor.fromColor(Config.kuudraSupplyColor));
                } else {
                    supplies.removeIf(ent -> ent.getUuidAsString().equals(supply.getUuidAsString()));
                }
            }
        }
        if (!dropOffs.isEmpty()) {
            for (Entity drop : new ArrayList<>(dropOffs)) {
                if (drop.isAlive() && drop.isCustomNameVisible() && drop.getCustomName() != null && Formatting.strip(drop.getCustomName().getString()).equals("BRING SUPPLY CHEST HERE")) {
                    event.drawBeam(getGround(drop.getPos()), 256, true, RenderColor.fromColor(Config.kuudraDropColor));
                } else {
                    dropOffs.removeIf(ent -> ent.getUuidAsString().equals(drop.getUuidAsString()));
                }
            }
        }
        if (!buildPiles.isEmpty()) {
            for (Entity pile : new ArrayList<>(buildPiles)) {
                if (pile.isAlive() && pile.isCustomNameVisible() && pile.getCustomName() != null && Formatting.strip(pile.getCustomName().getString()).endsWith("%")) {
                    event.drawBeam(getGround(pile.getLerpedPos(event.tickCounter.getTickProgress(true))), 256, true, RenderColor.fromColor(Config.kuudraBuildColor));
                } else {
                    buildPiles.removeIf(ent -> ent.getUuidAsString().equals(pile.getUuidAsString()));
                }
            }
        }
    }

    private enum kuudraPhases {
        Starting,
        Collect,
        Build,
        DPS,
        Lair
    }

    private static class PickupSpot {
        public String name;
        public Vec3d spot;
        public float maxSupplyDist;
        public float maxPlayerDist;
        public PickupSpot secondary;

        PickupSpot(String name, Vec3d spot, float maxSupplyDist, float maxPlayerDist, PickupSpot secondary) {
            this.name = name;
            this.spot = spot;
            this.maxSupplyDist = maxSupplyDist;
            this.maxPlayerDist = maxPlayerDist;
            this.secondary = secondary;
        }
    }
}
