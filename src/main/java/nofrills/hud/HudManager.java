package nofrills.hud;

import io.wispforest.owo.ui.hud.Hud;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import nofrills.events.*;
import nofrills.features.fishing.CapTracker;
import nofrills.hud.elements.*;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class HudManager {
    public static final List<HudElement> elements = new ArrayList<>();
    public static final FPS fps = new FPS("FPS: §f0");
    public static final TPS tps = new TPS("TPS: §f20.00");
    public static final Ping ping = new Ping("Ping: §f0ms");
    public static final Day day = new Day("Day: §f0");
    public static final Armor armor = new Armor();
    public static final Inventory inventory = new Inventory();
    public static final Quiver quiver = new Quiver("Quiver: §fN/A");
    public static final LagMeter lagMeter = new LagMeter("Last server tick was 0.00s ago");
    public static final BossHealth bossHealth = new BossHealth();
    public static final Power power = new Power("Power: §f0");
    public static final TickTimer tickTimer = new TickTimer("Tick Timers");
    public static final SeaCreatures seaCreatures = new SeaCreatures("Sea Creatures: §70");
    public static final FishingBobber bobber = new FishingBobber("Bobber: §7Inactive");
    public static final ShardTrackerDisplay shardTracker = new ShardTrackerDisplay();
    public static final SkillTrackerDisplay skillTrackerElement = new SkillTrackerDisplay();

    public static boolean isEditingHud() {
        return mc.currentScreen instanceof HudEditorScreen;
    }

    public static List<HudElement> getElements() {
        return elements;
    }

    public static void addNew(HudElement element) {
        elements.add(element);
    }

    public static void registerElements() {
        for (HudElement element : elements) {
            Identifier identifier = element.getIdentifier();
            if (!Hud.hasComponent(identifier)) {
                Hud.add(identifier, () -> element);
            }
        }
    }

    @EventHandler
    private static void onRenderHud(HudRenderEvent event) {
        if (!isEditingHud()) {
            for (HudElement element : HudManager.elements) {
                if (element.isAdded()) element.updatePosition();
            }
        }
    }

    @EventHandler
    private static void onJoinServer(ServerJoinEvent event) {
        ping.reset();
        tps.reset();
        fps.reset();
        lagMeter.setTickTime(0);
        bobber.hologram = null;
        bossHealth.reset();
    }

    @EventHandler
    private static void onPing(ReceivePacketEvent event) {
        if (event.packet instanceof PingResultS2CPacket pingPacket) {
            if (ping.isActive()) {
                ping.setPing(Util.getMeasuringTimeMs() - pingPacket.startTime());
                ping.ticks = 20;
            }
        }
    }

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (power.isActive()) {
            power.setPower(SkyblockData.dungeonPower);
        }
        if (day.isActive() && mc.world != null) {
            day.setDay(mc.world.getLevelProperties().getTimeOfDay() / 24000L);
        }
        if (ping.isActive()) { // pings every second when element is enabled, waits until ping result is received
            if (ping.ticks > 0) {
                ping.ticks -= 1;
                if (ping.ticks == 0) {
                    Utils.sendPingPacket();
                }
            }
        }
        if (tps.isActive()) {
            if (tps.clientTicks > 0) {
                tps.clientTicks -= 1;
                if (tps.clientTicks == 0) {
                    tps.setTps(tps.serverTicks);
                    tps.clientTicks = 20;
                    tps.serverTicks = 0;
                }
            }
        }
        if (seaCreatures.isActive()) {
            seaCreatures.setCount(CapTracker.seaCreatures.size());
        }
        if (bobber.isActive() && mc.player != null) {
            if (mc.player.fishHook != null && (bobber.hologram == null || !bobber.hologram.isAlive())) {
                bobber.setActive();
            } else if (mc.player.fishHook == null) {
                bobber.setInactive();
            }
        }
        if (fps.isActive()) {
            if (fps.ticks > 0) {
                fps.ticks -= 1;
                if (fps.ticks == 0) {
                    fps.setFps(mc.getCurrentFps());
                    fps.ticks = 20;
                }
            }
        }
        if (armor.isActive()) {
            armor.updateArmor();
        }
        if (inventory.isActive()) {
            inventory.updateInventory();
        }
        if (bossHealth.isActive()) {
            bossHealth.update();
        }
        if (quiver.isActive()) {
            quiver.update();
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (lagMeter.isActive()) {
            lagMeter.setTickTime(Util.getMeasuringTimeMs());
        }
        if (tps.isActive()) {
            tps.serverTicks += 1;
        }
        if (bobber.isActive() && bobber.timer.value() && bobber.active) {
            bobber.timerTicks += 1;
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (bobber.isActive() && event.namePlain.length() == 3) {
            if (event.namePlain.equals("!!!") || event.namePlain.indexOf(".") == 1) {
                bobber.hologram = event.entity;
                bobber.setTimer(event.namePlain);
            }
        }
    }
}
