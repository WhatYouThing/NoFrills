package nofrills.features.dungeons;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityUpdatedEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.util.HashSet;

public class MinibossHighlight {
    public static final Feature instance = new Feature("minibossHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xffffff00), "color", instance.key());

    private static final HashSet<String> minibossList = Sets.newHashSet(
            "Lost Adventurer",
            "Diamond Guy",
            "Shadow Assassin",
            "King Midas",
            "Spirit Bear"
    );

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.entity instanceof PlayerEntity player && !Utils.isPlayer(player)) {
            if (minibossList.contains(player.getName().getString())) {
                if (Utils.isInDungeonBoss("4") && event.entity.getPos().getY() >= 76.0) {
                    return; // prevents the Floor 4 "crowd" NPC's from getting highlighted
                }
                Rendering.Entities.drawOutline(event.entity, true, color.value());
            }
        }
    }
}
