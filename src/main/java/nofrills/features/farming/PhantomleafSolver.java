package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.sounds.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.*;
import nofrills.hud.HudManager;
import nofrills.misc.Utils;

public class PhantomleafSolver {
    public static final Feature instance = new Feature("phantomleafSolver");

    private static float lastVolume = 0.0f;
    private static int ticks = 0;

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && event.isSound(SoundEvents.NOTE_BLOCK_BASEDRUM) && ticks > 0 && Utils.isInGarden()) {
            lastVolume = event.volume();
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInGarden()) {
            if (lastVolume > 0.0f && ticks > 0) {
                if (lastVolume >= 0.99f) {
                    HudManager.setCustomTitle("§aFound Phantomleaf", 1);
                } else if (lastVolume >= 0.94f) {
                    HudManager.setCustomTitle("§eClose to Phantomleaf", 1);
                }
            } else if (lastVolume != 0.0f) {
                lastVolume = 0.0f;
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && ticks > 0) {
            ticks--;
        }
    }

    @EventHandler
    private static void onPacket(ReceivePacketEvent event) {
        if (instance.isActive() && event.packet instanceof ClientboundSetSubtitleTextPacket(
                Component text
        ) && Utils.isInGarden()) {
            String subtitle = Utils.toPlain(text).trim();
            if (subtitle.startsWith("(") && subtitle.contains(Utils.Symbols.heart) && subtitle.endsWith(")")) {
                ticks = 40;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        lastVolume = 0.0f;
        ticks = 0;
    }
}
