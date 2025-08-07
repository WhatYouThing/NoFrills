package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

public class CorpseHighlight {
    public static final Feature instance = new Feature("corpseHighlight");

    public static final SettingColor lapisColor = new SettingColor(new RenderColor(85, 85, 255, 255), "lapis", instance.key());
    public static final SettingColor mineralColor = new SettingColor(new RenderColor(170, 170, 170, 255), "mineral", instance.key());
    public static final SettingColor yogColor = new SettingColor(new RenderColor(255, 170, 0, 255), "yog", instance.key());
    public static final SettingColor vanguardColor = new SettingColor(new RenderColor(255, 63, 255, 255), "vanguard", instance.key());

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInArea("Mineshaft")) {
            for (Entity ent : Utils.getEntities()) {
                if (ent instanceof ArmorStandEntity stand && !stand.isInvisible() && !Rendering.Entities.isDrawingGlow(stand)) {
                    ItemStack helmet = Utils.getEntityArmor(stand).getFirst();
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
                            Rendering.Entities.drawGlow(stand, true, color);
                        }
                    }
                }
            }
        }
    }
}
