package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import nofrills.config.Config;
import nofrills.events.EntityNamedEvent;
import nofrills.events.SpawnParticleEvent;

import java.util.List;
import java.util.regex.Pattern;

import static nofrills.misc.Utils.Symbols;

public class NoRender {
    private static final List<Pattern> deadPatterns = List.of(
            Pattern.compile(".* 0" + Symbols.heart),
            Pattern.compile(".* 0/.*" + Symbols.heart),
            Pattern.compile(".* 0/.*" + Symbols.heart + " " + Symbols.vampLow)
    );

    private static final List<ParticleType<?>> explosionParticles = List.of(
            ParticleTypes.EXPLOSION,
            ParticleTypes.EXPLOSION_EMITTER,
            ParticleTypes.GUST,
            ParticleTypes.GUST_EMITTER_LARGE
    );

    @EventHandler
    public static void onNamed(EntityNamedEvent event) {
        if (Config.hideDeadMobs && event.entity instanceof ArmorStandEntity) {
            for (Pattern pattern : deadPatterns) {
                if (pattern.matcher(event.namePlain).matches()) {
                    event.entity.setCustomNameVisible(false);
                    break;
                }
            }
        }
    }

    @EventHandler
    public static void onParticle(SpawnParticleEvent event) {
        if (Config.noExplosions) {
            for (ParticleType<?> type : explosionParticles) {
                if (event.type.equals(type)) {
                    event.cancel();
                    break;
                }
            }
        }
    }
}
