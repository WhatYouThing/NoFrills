package nofrills.features.slayer;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import nofrills.config.Feature;
import nofrills.events.*;
import nofrills.features.general.NoRender;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.time.Instant;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@EventListener
public class KillTimer {
    public static final Feature instance = new Feature("killTimer");

    private static final HashSet<String> resetMessages = Sets.newHashSet(
            "SLAYER QUEST COMPLETE!",
            "SLAYER QUEST FAILED!",
            "SLAYER QUEST STARTED!",
            "YOU COCOONED YOUR SLAYER BOSS",
            "Your Slayer Quest has been cancelled!"
    );
    private static final AtomicLong bossStart = new AtomicLong();
    private static final AtomicInteger serverTicks = new AtomicInteger();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SlayerUtil.getActiveQuest() != null) {
            if (SlayerUtil.isSpawner(event.namePlain) && bossStart.get() == 0L) {
                bossStart.set(Instant.now().toEpochMilli());
            }
            ArmorStand name = SlayerUtil.getNameEntity();
            if (bossStart.get() > 0L && event.entity.equals(name) && NoRender.deadPattern.matcher(event.namePlain).matches()) {
                if (SlayerUtil.getActiveQuest() == SlayerUtil.BLAZE && !event.namePlain.contains("Inferno Demonlord")) {
                    return;
                }
                long time = Instant.now().toEpochMilli() - bossStart.get();
                Utils.infoRaw(Component.literal("Slayer boss took " + Utils.formatDecimal(time * 0.001) + "s ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("(" + Utils.formatDecimal(serverTicks.get() / 20.0f) + "s tick) ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("to kill.").withStyle(ChatFormatting.GREEN))
                );
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && bossStart.get() != 0L) {
            serverTicks.incrementAndGet();
        }
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && resetMessages.contains(event.messagePlain.trim())) {
            bossStart.set(0L);
            serverTicks.set(0);
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        bossStart.set(0L);
        serverTicks.set(0);
    }
}
