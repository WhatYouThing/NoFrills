package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
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

    public static boolean isSecretBat(Entity entity) {
        if (entity instanceof BatEntity bat) {
            return Utils.isBaseHealth(bat, 100.0f) && !Utils.isInDungeonBoss("4");
        }
        return false;
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && isSecretBat(event.entity) && !Rendering.Entities.isDrawingGlow(event.entity)) {
            Rendering.Entities.drawGlow(event.entity, true, color.value());
        }
    }
}
