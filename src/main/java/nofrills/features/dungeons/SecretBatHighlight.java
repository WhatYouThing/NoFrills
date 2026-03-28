package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.*;

public class SecretBatHighlight {
    public static final Feature instance = new Feature("secretBatHighlight");

    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Outline, RenderStyle.class, "style", instance);
    public static final SettingColor outlineColor = new SettingColor(new RenderColor(85, 255, 85, 255), "color", instance);
    public static final SettingColor fillColor = new SettingColor(new RenderColor(85, 255, 85, 127), "fillColor", instance);

    private static final EntityCache cache = new EntityCache();

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && DungeonUtil.isSecretBat(event.entity)) {
            cache.add(event.entity);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && cache.size() > 0) {
            for (Entity ent : cache.get()) {
                if (!ent.isAlive()) continue;
                Box box = Utils.getLerpedBox(ent, event.tickCounter.getTickProgress(true));
                event.drawStyled(box, style.value(), false, outlineColor.value(), fillColor.value());
            }
        }
    }
}
