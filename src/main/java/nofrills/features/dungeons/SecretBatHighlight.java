package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.passive.BatEntity;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityUpdatedEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

public class SecretBatHighlight {
    public static final Feature instance = new Feature("secretBatHighlight");

    public static final SettingColor color = new SettingColor(new RenderColor(85, 255, 85, 255), "color", instance.key());

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.entity instanceof BatEntity bat) {
            if (bat.getHealth() == 100.0f && !Rendering.Entities.isDrawingGlow(bat)) {
                Rendering.Entities.drawGlow(bat, true, color.value());
            }
        }
    }
}
