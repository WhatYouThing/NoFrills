package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@EventListener
public class SpawnTimer {
    public static final Feature instance = new Feature("spawnTimer");

    private static final AtomicLong startedAt = new AtomicLong();

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive()) {
            if (SlayerUtil.isQuestStartedMessage(event.messagePlain)) {
                startedAt.set(Instant.now().toEpochMilli());
            }
            if (SlayerUtil.isSpawnedMessage(event.messagePlain)) {
                long start = startedAt.get();
                if (start == 0) return;
                long time = Instant.now().toEpochMilli() - start;
                Utils.infoRaw(Component.literal("Slayer boss took " + Utils.formatDecimal(time * 0.001) + "s ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("to spawn.").withStyle(ChatFormatting.GREEN))
                );
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        startedAt.set(0);
    }
}
