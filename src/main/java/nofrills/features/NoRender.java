package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleTypes;
import nofrills.config.Config;
import nofrills.events.EntityNamedEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.SpawnParticleEvent;

import java.util.regex.Pattern;

import static nofrills.Main.mc;
import static nofrills.misc.Utils.Symbols;

public class NoRender {
    private static final Pattern[] deadPatterns = {
            Pattern.compile(".* 0" + Symbols.heart),
            Pattern.compile(".* 0/.*" + Symbols.heart),
            Pattern.compile(".* 0/.*" + Symbols.heart + " " + Symbols.vampLow)
    };

    @EventHandler
    public static void onNamed(EntityNamedEvent event) {
        if (Config.hideDeadMobs && event.entity.getType() == EntityType.ARMOR_STAND) {
            for (Pattern pattern : deadPatterns) {
                if (pattern.matcher(event.namePlain).matches()) {
                    event.entity.setCustomNameVisible(false);
                    return;
                }
            }
        }
    }

    @EventHandler
    public static void onParticle(SpawnParticleEvent event) {
        if (Config.noExplosions && (event.type.equals(ParticleTypes.EXPLOSION) || event.type.equals(ParticleTypes.EXPLOSION_EMITTER))) {
            event.cancel();
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (Config.noLoadingScreen && event.screen instanceof DownloadingTerrainScreen) {
            mc.setScreen(null);
        }
    }
}
