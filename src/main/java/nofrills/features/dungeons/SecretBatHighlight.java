package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityUpdatedEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

public class SecretBatHighlight {
    public static final Feature instance = new Feature("secretBatHighlight");

    public static final SettingColor color = new SettingColor(new RenderColor(85, 255, 85, 255), "color", instance.key());

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && DungeonUtil.isSecretBat(event.entity) && !Rendering.Entities.isDrawingGlow(event.entity)) {
            Rendering.Entities.drawGlow(event.entity, true, color.value());
        }
    }
}
