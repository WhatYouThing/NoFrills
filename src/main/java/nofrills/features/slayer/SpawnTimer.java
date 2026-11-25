package nofrills.features.slayer;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.time.Instant;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

@EventListener
public class SpawnTimer {
    public static final Feature instance = new Feature("spawnTimer");

    private static final HashSet<String> startMessages = Sets.newHashSet(
            "SLAYER QUEST STARTED!"
    );
    private static final AtomicLong questStart = new AtomicLong();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SlayerUtil.getActiveQuest() != null) {
            if (SlayerUtil.isSpawner(event.namePlain) && questStart.get() > 0L) {
                long time = Instant.now().toEpochMilli() - questStart.get();
                Utils.infoRaw(Component.literal("Slayer boss took " + Utils.formatDecimal(time * 0.001) + "s ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("to spawn.").withStyle(ChatFormatting.GREEN))
                );
                questStart.set(0L);
            }
        }
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && startMessages.contains(event.messagePlain.trim())) {
            questStart.set(Instant.now().toEpochMilli());
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        questStart.set(0L);
    }
}
