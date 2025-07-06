package nofrills.hud;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import nofrills.config.Config;
import nofrills.events.*;
import nofrills.features.FishingFeatures;
import nofrills.hud.elements.*;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public class HudManager {
    private static final RenderColor defaultColor = RenderColor.fromHex(0xffffff);

    public static FishingBobber bobberElement = new FishingBobber(Text.of("§cBobber: §7Inactive"), defaultColor);
    public static SeaCreatures seaCreaturesElement = new SeaCreatures(Text.of("§3Sea Creatures: §70"), defaultColor);
    public static TPS tpsElement = new TPS(Text.of("§bTPS: §f20.00"), defaultColor);
    public static LagMeter lagMeterElement = new LagMeter(Text.of("§cLast server tick was 0.00s ago"), defaultColor);
    public static Power powerElement = new Power(Text.of("§bPower: §f0"), defaultColor);
    public static Day dayElement = new Day(Text.of("§bDay: §f0"), defaultColor);
    public static Ping pingElement = new Ping(Text.of("§bPing: §f0§7ms"), defaultColor);

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

    @EventHandler
    private static void onRenderHud(HudRenderEvent event) {
        if (!isEditingHud()) {
            for (HudElement element : elements) {
                element.render(event.context, 0, 0, event.tickCounter.getTickProgress(true));
            }
        }
    }

    @EventHandler
    private static void onJoinServer(ServerJoinEvent event) {
        pingTicks = 0;
        lagMeterElement.setTickTime(0); // temporarily disables the element, as the server doesn't send tick packets for a few seconds after joining
        serverTicks = 0;
        tpsTimer = 0;
        tpsElement.setTps(0);
    }

    @EventHandler
    private static void onPing(ReceivePacketEvent event) {
        if (event.packet instanceof PingResultS2CPacket pingPacket) {
            if (Config.pingEnabled) {
                pingElement.setPing(Util.getMeasuringTimeMs() - pingPacket.startTime());
            }
            pingTicks = 0;
        }
    }

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (Config.powerEnabled) {
            powerElement.setPower(SkyblockData.dungeonPower);
        }
        if (Config.dayEnabled) {
            dayElement.setDay(mc.world.getTimeOfDay() / 24000L);
        }
        if (Config.pingEnabled && pingTicks <= 20) { // pings every second when element is enabled, waits until ping result is received
            pingTicks++;
            if (pingTicks == 20) {
                Utils.sendPingPacket();
            }
        }
        if (Config.tpsEnabled) {
            tpsTimer++;
            if (tpsTimer == 20) {
                tpsElement.setTps(serverTicks);
                serverTicks = 0;
                tpsTimer = 0;
            }
        }
        if (Config.seaCreaturesEnabled) {
            seaCreaturesElement.setCount(FishingFeatures.seaCreatures.size());
        }
        if (Config.bobberEnabled && mc.player != null) {
            if (mc.player.fishHook != null && (bobberHologram == null || !bobberHologram.isAlive())) {
                bobberElement.setActive();
            } else if (mc.player.fishHook == null) {
                bobberElement.setInactive();
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (Config.lagMeterEnabled) {
            lagMeterElement.setTickTime(Util.getMeasuringTimeMs());
        }
        if (Config.tpsEnabled) {
            serverTicks++;
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (Config.bobberEnabled && event.namePlain.length() == 3) {
            if (event.namePlain.equals("!!!") || event.namePlain.indexOf(".") == 1) {
                bobberHologram = event.entity;
                bobberElement.setTimer(event.namePlain);
            }
        }
    }
}
