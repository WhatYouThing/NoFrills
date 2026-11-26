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
public class KillTimer {
    public static final Feature instance = new Feature("killTimer");

    private static final AtomicLong spawnedAt = new AtomicLong();

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive()) {
            if (SlayerUtil.isSpawnedMessage(event.messagePlain)) {
                spawnedAt.set(Instant.now().toEpochMilli());
            }
            if (SlayerUtil.isQuestCompleteMessage(event.messagePlain)) {
                long time = Instant.now().toEpochMilli() - spawnedAt.get();
                Utils.infoRaw(Component.literal("Slayer boss took " + Utils.formatDecimal(time * 0.001) + "s ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("to kill.").withStyle(ChatFormatting.GREEN))
                );
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        spawnedAt.set(0);
    }
}
