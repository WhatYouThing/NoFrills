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
    private static final List<HudElement> elements = new ArrayList<>();
    public static FPS fpsElement = new FPS("§bFPS: §f0");
    public static TPS tpsElement = new TPS("§bTPS: §f20.00");
    public static Ping pingElement = new Ping("§bPing: §f0ms");
    public static Day dayElement = new Day("§bDay: §f0");
    public static SeaCreatures seaCreaturesElement = new SeaCreatures("§3Sea Creatures: §70");
    public static FishingBobber bobberElement = new FishingBobber("§cBobber: §7Inactive");
    public static LagMeter lagMeterElement = new LagMeter("§cLast server tick was 0.00s ago");
    public static Power powerElement = new Power("§bPower: §f0");
    public static ShardTrackerDisplay shardTrackerElement = new ShardTrackerDisplay();
    public static TickTimer tickTimerElement = new TickTimer("§bTick Timers");

    public static boolean isEditingHud() {
        return mc.currentScreen instanceof HudEditorScreen;
    }

    public static List<HudElement> getElements() {
        return elements;
    }

    public static void addNew(HudElement element) {
        elements.add(element);
    }

    public static double getDefaultX() {
        return 0.01;
    }

    public static double getDefaultY() {
        return 0.01 + 0.05 * elements.size();
    }

    public static void registerElements() {
        for (HudElement element : elements) {
            Identifier identifier = element.getIdentifier();
            if (identifier != null && !Hud.hasComponent(identifier)) {
                Hud.add(identifier, () -> element);
            }
        }
    }

    @EventHandler
    private static void onRenderHud(HudRenderEvent event) {
        if (!isEditingHud()) {
            for (HudElement element : HudManager.elements) {
                element.updatePosition();
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
            if (pingElement.instance.isActive()) {
                pingElement.setPing(Util.getMeasuringTimeMs() - pingPacket.startTime());
                pingElement.ticks = 20;
            }
        }
    }

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (powerElement.instance.isActive()) {
            powerElement.setPower(SkyblockData.dungeonPower);
        }
        if (dayElement.instance.isActive() && mc.world != null) {
            dayElement.setDay(mc.world.getLevelProperties().getTimeOfDay() / 24000L);
        }
        if (pingElement.instance.isActive()) { // pings every second when element is enabled, waits until ping result is received
            if (pingElement.ticks > 0) {
                pingElement.ticks -= 1;
                if (pingElement.ticks == 0) {
                    Utils.sendPingPacket();
                }
            }
        }
        if (tpsElement.instance.isActive()) {
            if (tpsElement.clientTicks > 0) {
                tpsElement.clientTicks -= 1;
                if (tpsElement.clientTicks == 0) {
                    tpsElement.setTps(tpsElement.serverTicks);
                    tpsElement.clientTicks = 20;
                    tpsElement.serverTicks = 0;
                }
            }
        }
        if (seaCreaturesElement.instance.isActive()) {
            seaCreaturesElement.setCount(CapTracker.seaCreatures.size());
        }
        if (bobberElement.instance.isActive() && mc.player != null) {
            if (mc.player.fishHook != null && (bobberElement.hologram == null || !bobberElement.hologram.isAlive())) {
                bobberElement.setActive();
            } else if (mc.player.fishHook == null) {
                bobberElement.setInactive();
            }
        }
        if (fpsElement.instance.isActive()) {
            if (fpsElement.ticks > 0) {
                fpsElement.ticks -= 1;
                if (fpsElement.ticks == 0) {
                    fpsElement.setFps(mc.getCurrentFps());
                    fpsElement.ticks = 20;
                }
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (lagMeterElement.instance.isActive()) {
            lagMeterElement.setTickTime(Util.getMeasuringTimeMs());
        }
        if (tpsElement.instance.isActive()) {
            tpsElement.serverTicks += 1;
        }
        if (bobberElement.instance.isActive() && bobberElement.timer.value() && bobberElement.active) {
            bobberElement.timerTicks += 1;
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (bobberElement.instance.isActive() && event.namePlain.length() == 3) {
            if (event.namePlain.equals("!!!") || event.namePlain.indexOf(".") == 1) {
                bobberElement.hologram = event.entity;
                bobberElement.setTimer(event.namePlain);
            }
        }
    }
}
