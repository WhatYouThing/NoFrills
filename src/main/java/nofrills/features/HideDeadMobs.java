package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityType;
import nofrills.config.Config;
import nofrills.events.EntityNamedEvent;

import java.util.regex.Pattern;

import static nofrills.misc.Utils.Symbols;

public class HideDeadMobs {
    private static final Pattern[] deadPatterns = {
            Pattern.compile(".* 0" + Symbols.heart),
            Pattern.compile(".* 0/.*" + Symbols.heart)
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
}
