package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

@EventListener
public class SpawnTimer {
    public static final Feature instance = new Feature("spawnTimer");

    private static int ticks = 0;
    private static int serverTicks = 0;

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && SlayerUtil.getActiveQuest() != null) {
            if (!SlayerUtil.isBossAlive()) {
                ticks++;
            } else if (ticks > 0) {
                if (ticks < 12000) { // skip notification if not actively doing the slayer within a 10-minute window
                    Utils.infoRaw(Component.literal("Slayer boss took " + Utils.formatDecimal(ticks / 20.0f) + "s ").withStyle(ChatFormatting.GREEN)
                            .append(Component.literal("(" + Utils.formatDecimal(serverTicks / 20.0f) + "s server time) ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("to spawn.").withStyle(ChatFormatting.GREEN))
                    );
                }
                ticks = 0;
                serverTicks = 0;
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && !SlayerUtil.isBossAlive() && SlayerUtil.getActiveQuest() != null) {
            serverTicks++;
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        ticks = 0;
        serverTicks = 0;
    }
}
