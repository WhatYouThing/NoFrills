package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityUpdatedEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.util.List;

public class MinibossHighlight {
    public static final Feature instance = new Feature("minibossHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xffffff00), "color", instance.key());

    private static final List<String> minibossList = List.of(
            "Lost Adventurer",
            "Diamond Guy",
            "Shadow Assassin",
            "King Midas",
            "Spirit Bear"
    );

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.entity instanceof PlayerEntity player && !Utils.isPlayer(player)) {
            String name = player.getName().getString();
            for (String mini : minibossList) {
                if (name.equals(mini)) {
                    Rendering.Entities.drawOutline(event.entity, true, color.value());
                    return;
                }
            }
        }
    }
}
