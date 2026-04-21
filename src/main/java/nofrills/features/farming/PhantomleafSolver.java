package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.PlaySoundEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;
import nofrills.hud.HudManager;
import nofrills.misc.Utils;

public class PhantomleafSolver {
    public static final Feature instance = new Feature("phantomleafSolver");

    private static float lastVolume = 0.0f;

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && Utils.isInGarden() && event.isSound(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM)) {
            lastVolume = event.volume();
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInGarden()) {
            if (lastVolume > 0.0f && Utils.hasWorldBorderVignette()) {
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
    private static void onJoin(ServerJoinEvent event) {
        lastVolume = 0.0f;
    }
}
