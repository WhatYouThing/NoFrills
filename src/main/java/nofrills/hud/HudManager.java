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
    public static final FPS fpsElement = new FPS("FPS: §f0");
    public static final TPS tpsElement = new TPS("TPS: §f20.00");
    public static final Ping pingElement = new Ping("Ping: §f0ms");
    public static final Day dayElement = new Day("Day: §f0");
    public static final SeaCreatures seaCreaturesElement = new SeaCreatures("Sea Creatures: §70");
    public static final FishingBobber bobberElement = new FishingBobber("Bobber: §7Inactive");
    public static final LagMeter lagMeterElement = new LagMeter("Last server tick was 0.00s ago");
    public static final Power powerElement = new Power("Power: §f0");
    public static final ShardTrackerDisplay shardTrackerElement = new ShardTrackerDisplay();
    public static final TickTimer tickTimerElement = new TickTimer("Tick Timers");
    public static final Armor armorElement = new Armor();

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
        pingElement.reset();
        tpsElement.reset();
        fpsElement.reset();
        lagMeterElement.setTickTime(0); // temporarily disables the element, as the server doesn't send tick packets for a few seconds after joining
        bobberElement.hologram = null;
    }

    @EventHandler
    private static void onPing(ReceivePacketEvent event) {
        if (event.packet instanceof PingResultS2CPacket pingPacket) {
            if (pingElement.isActive()) {
                pingElement.setPing(Util.getMeasuringTimeMs() - pingPacket.startTime());
                pingElement.ticks = 20;
            }
        }
    }

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (powerElement.isActive()) {
            powerElement.setPower(SkyblockData.dungeonPower);
        }
        if (dayElement.isActive() && mc.world != null) {
            dayElement.setDay(mc.world.getLevelProperties().getTimeOfDay() / 24000L);
        }
        if (pingElement.isActive()) { // pings every second when element is enabled, waits until ping result is received
            if (pingElement.ticks > 0) {
                pingElement.ticks -= 1;
                if (pingElement.ticks == 0) {
                    Utils.sendPingPacket();
                }
            }
        }
        if (tpsElement.isActive()) {
            if (tpsElement.clientTicks > 0) {
                tpsElement.clientTicks -= 1;
                if (tpsElement.clientTicks == 0) {
                    tpsElement.setTps(tpsElement.serverTicks);
                    tpsElement.clientTicks = 20;
                    tpsElement.serverTicks = 0;
                }
            }
        }
        if (seaCreaturesElement.isActive()) {
            seaCreaturesElement.setCount(CapTracker.seaCreatures.size());
        }
        if (bobberElement.isActive() && mc.player != null) {
            if (mc.player.fishHook != null && (bobberElement.hologram == null || !bobberElement.hologram.isAlive())) {
                bobberElement.setActive();
            } else if (mc.player.fishHook == null) {
                bobberElement.setInactive();
            }
        }
        if (fpsElement.isActive()) {
            if (fpsElement.ticks > 0) {
                fpsElement.ticks -= 1;
                if (fpsElement.ticks == 0) {
                    fpsElement.setFps(mc.getCurrentFps());
                    fpsElement.ticks = 20;
                }
            }
        }
        if (armorElement.isActive()) {
            armorElement.updateArmor();
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (lagMeterElement.isActive()) {
            lagMeterElement.setTickTime(Util.getMeasuringTimeMs());
        }
        if (tpsElement.isActive()) {
            tpsElement.serverTicks += 1;
        }
        if (bobberElement.isActive() && bobberElement.timer.value() && bobberElement.active) {
            bobberElement.timerTicks += 1;
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (bobberElement.isActive() && event.namePlain.length() == 3) {
            if (event.namePlain.equals("!!!") || event.namePlain.indexOf(".") == 1) {
                bobberElement.hologram = event.entity;
                bobberElement.setTimer(event.namePlain);
            }
        }
    }
}
