package nofrills.hud;

import io.wispforest.owo.ui.hud.Hud;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import nofrills.events.*;
import nofrills.features.fishing.CapTracker;
import nofrills.hud.elements.*;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public class HudManager {
    public static FishingBobber bobberElement = new FishingBobber(Text.of("§cBobber: §7Inactive"));
    public static SeaCreatures seaCreaturesElement = new SeaCreatures(Text.of("§3Sea Creatures: §70"));
    public static TPS tpsElement = new TPS(Text.of("§bTPS: §f20.00"));
    public static LagMeter lagMeterElement = new LagMeter(Text.of("§cLast server tick was 0.00s ago"));
    public static Power powerElement = new Power(Text.of("§bPower: §f0"));
    public static Day dayElement = new Day(Text.of("§bDay: §f0"));
    public static Ping pingElement = new Ping(Text.of("§bPing: §f0ms"));

    public static List<HudElement> elements = List.of(
            bobberElement,
            seaCreaturesElement,
            tpsElement,
            lagMeterElement,
            powerElement,
            dayElement,
            pingElement
    );

    private static int pingTicks = 0;
    private static int serverTicks = 0;
    private static int tpsTimer = 0;
    private static Entity bobberHologram = null;

    public static boolean isEditingHud() {
        return mc.currentScreen instanceof HudEditorScreen;
    }

    public static void registerAll() {
        for (HudElement element : elements) {
            Identifier identifier = element.getIdentifier();
            if (identifier != null && !Hud.hasComponent(identifier)) {
                Hud.add(identifier, () -> element);
            }
        }
    }

    public static void unregisterAll() {
        for (HudElement element : elements) {
            Identifier identifier = element.getIdentifier();
            if (identifier != null && Hud.hasComponent(identifier)) {
                Hud.remove(identifier);
            }
        }
    }

    @EventHandler
    private static void onRenderHud(HudRenderEvent event) {
        for (HudElement element : HudManager.elements) {
            element.updatePosition();
        }
    }

    @EventHandler
    private static void onJoinServer(ServerJoinEvent event) {
        registerAll();
        pingTicks = 0;
        lagMeterElement.setTickTime(0); // temporarily disables the element, as the server doesn't send tick packets for a few seconds after joining
        serverTicks = 0;
        tpsTimer = 0;
        tpsElement.setTps(0);
    }

    @EventHandler
    private static void onPing(ReceivePacketEvent event) {
        if (event.packet instanceof PingResultS2CPacket pingPacket) {
            if (pingElement.instance.isActive()) {
                pingElement.setPing(Util.getMeasuringTimeMs() - pingPacket.startTime());
            }
            pingTicks = 0;
        }
    }

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (powerElement.instance.isActive()) {
            powerElement.setPower(SkyblockData.dungeonPower);
        }
        if (dayElement.instance.isActive()) {
            dayElement.setDay(mc.world.getTimeOfDay() / 24000L);
        }
        if (pingElement.instance.isActive() && pingTicks <= 20) { // pings every second when element is enabled, waits until ping result is received
            pingTicks++;
            if (pingTicks == 20) {
                Utils.sendPingPacket();
            }
        }
        if (tpsElement.instance.isActive()) {
            tpsTimer++;
            if (tpsTimer == 20) {
                tpsElement.setTps(serverTicks);
                serverTicks = 0;
                tpsTimer = 0;
            }
        }
        if (seaCreaturesElement.instance.isActive()) {
            seaCreaturesElement.setCount(CapTracker.seaCreatures.size());
        }
        if (bobberElement.instance.isActive() && mc.player != null) {
            if (mc.player.fishHook != null && (bobberHologram == null || !bobberHologram.isAlive())) {
                bobberElement.setActive();
            } else if (mc.player.fishHook == null) {
                bobberElement.setInactive();
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (lagMeterElement.instance.isActive()) {
            lagMeterElement.setTickTime(Util.getMeasuringTimeMs());
        }
        if (tpsElement.instance.isActive()) {
            serverTicks++;
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (bobberElement.instance.isActive() && event.namePlain.length() == 3) {
            if (event.namePlain.equals("!!!") || event.namePlain.indexOf(".") == 1) {
                bobberHologram = event.entity;
                bobberElement.setTimer(event.namePlain);
            }
        }
    }
}
