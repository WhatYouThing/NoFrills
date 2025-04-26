package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class CorpseHighlight {
    @EventHandler
    public static void tick(WorldTickEvent event) {
        if (Config.miningCorpseGlow && Utils.isInZone(Utils.Symbols.zone + " Glacite Mineshafts", false)) {
            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof ArmorStandEntity armorStand && !ent.isInvisible() && !Rendering.Entities.isDrawingGlow(ent)) {
                    Iterable<ItemStack> armor = Utils.getEntityArmor(armorStand);
                    for (ItemStack piece : armor) {
                        if (piece.isEmpty()) {
                            continue;
                        }
                        String pieceName = Formatting.strip(piece.getName().getString());
                        RenderColor color = switch (pieceName) {
                            case "Lapis Armor Helmet" -> new RenderColor(85, 85, 255, 0);
                            case "Mineral Helmet" -> new RenderColor(170, 170, 170, 0);
                            case "Yog Helmet" -> new RenderColor(255, 170, 0, 0);
                            case "Vanguard Helmet" -> new RenderColor(255, 255, 255, 0);
                            default -> null;
                        };
                        if (color != null) {
                            Rendering.Entities.drawGlow(ent, true, color);
                        }
                    }
                }
            }
        }
    }
}
