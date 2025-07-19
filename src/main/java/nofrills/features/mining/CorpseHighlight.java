package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityUpdatedEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

public class CorpseHighlight {
    public static final Feature instance = new Feature("corpseHighlight");

    public static final SettingColor lapisColor = new SettingColor(new RenderColor(85, 85, 255, 0), "lapis", instance.key());
    public static final SettingColor mineralColor = new SettingColor(new RenderColor(85, 85, 255, 0), "mineral", instance.key());
    public static final SettingColor yogColor = new SettingColor(new RenderColor(85, 85, 255, 0), "yog", instance.key());
    public static final SettingColor vanguardColor = new SettingColor(new RenderColor(85, 85, 255, 0), "vanguard", instance.key());

    @EventHandler
    private static void onTick(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInZone(Utils.Symbols.zone + " Glacite Mineshafts", false)) {
            if (event.entity instanceof ArmorStandEntity armorStand && !armorStand.isInvisible() && !Rendering.Entities.isDrawingGlow(armorStand)) {
                ItemStack helmet = Utils.getEntityArmor(armorStand).getFirst();
                if (!helmet.isEmpty()) {
                    String pieceName = Formatting.strip(helmet.getName().getString());
                    RenderColor color = switch (pieceName) {
                        case "Lapis Armor Helmet" -> lapisColor.value();
                        case "Mineral Helmet" -> mineralColor.value();
                        case "Yog Helmet" -> yogColor.value();
                        case "Vanguard Helmet" -> vanguardColor.value();
                        default -> null;
                    };
                    if (color != null) {
                        Rendering.Entities.drawGlow(armorStand, true, color);
                    }
                }
            }
        }
    }
}
