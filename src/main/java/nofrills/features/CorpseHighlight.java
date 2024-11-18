package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class CorpseHighlight {
    @EventHandler
    public static void tick(WorldTickEvent event) {
        if (Config.miningCorpseGlow && Utils.isInZone(Utils.Symbols.zone + " Glacite Mineshafts", false)) {
            for (Entity ent : mc.world.getEntities()) {
                if (ent.getType() == EntityType.ARMOR_STAND && !ent.isInvisible() && !Utils.isGlowing(ent)) {
                    Iterable<ItemStack> armor = ((ArmorStandEntity) ent).getArmorItems();
                    if (armor != null) {
                        for (ItemStack piece : armor) {
                            String pieceName = Formatting.strip(piece.getName().getString());
                            int color = switch (pieceName) {
                                case "Lapis Armor Helmet" -> 5592575;
                                case "Mineral Helmet" -> 11184810;
                                case "Yog Helmet" -> 16755200;
                                case "Vanguard Helmet" -> 16777215;
                                default -> 0;
                            };
                            if (color != 0) {
                                Utils.setGlowing(ent, true, color);
                            }
                        }
                    }
                }
            }
        }
    }
}
