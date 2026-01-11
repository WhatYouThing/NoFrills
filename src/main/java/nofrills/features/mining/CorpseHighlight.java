package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.InteractEntityEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.HashSet;

import static nofrills.Main.mc;

public class CorpseHighlight {
    public static final Feature instance = new Feature("corpseHighlight");

    public static final SettingBool hideOpened = new SettingBool(true, "hideOpened", instance);
    public static final SettingColor lapisColor = new SettingColor(new RenderColor(85, 85, 255, 255), "lapis", instance.key());
    public static final SettingColor mineralColor = new SettingColor(new RenderColor(170, 170, 170, 255), "mineral", instance.key());
    public static final SettingColor yogColor = new SettingColor(new RenderColor(255, 170, 0, 255), "yog", instance.key());
    public static final SettingColor vanguardColor = new SettingColor(new RenderColor(255, 85, 255, 255), "vanguard", instance.key());

    private static final HashSet<Integer> openedCorpses = new HashSet<>();

    private static boolean active() {
        return instance.isActive() && Utils.isInArea("Mineshaft");
    }

    private static CorpseType getCorpseType(ArmorStandEntity ent) {
        ItemStack helmet = Utils.getEntityArmor(ent).getFirst();
        if (!helmet.isEmpty()) {
            return switch (Utils.toPlain(helmet.getName())) {
                case "Lapis Armor Helmet" -> CorpseType.Lapis;
                case "Mineral Helmet" -> CorpseType.Tungsten;
                case "Yog Helmet" -> CorpseType.Umber;
                case "Vanguard Helmet" -> CorpseType.Vanguard;
                default -> CorpseType.None;
            };
        }
        return CorpseType.None;
    }

    private static RenderColor getCorpseColor(CorpseType type) {
        return switch (type) {
            case Lapis -> lapisColor.value();
            case Tungsten -> mineralColor.value();
            case Umber -> yogColor.value();
            case Vanguard -> vanguardColor.value();
            default -> null;
        };
    }

    private static boolean hasKeyForCorpse(CorpseType type) {
        String id = switch (type) {
            case Tungsten -> "TUNGSTEN_KEY";
            case Umber -> "UMBER_KEY";
            case Vanguard -> "SKELETON_KEY";
            default -> "";
        };
        if (!id.isEmpty()) {
            PlayerInventory inv = mc.player.getInventory();
            for (int i = 0; i <= 35; i++) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty() && Utils.getSkyblockId(stack).equals(id)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (active()) {
            for (Entity ent : Utils.getEntities()) {
                if (ent instanceof ArmorStandEntity stand && !stand.isInvisible()) {
                    if (openedCorpses.contains(stand.getId()) || Utils.isGlowing(stand)) {
                        continue;
                    }
                    RenderColor color = getCorpseColor(getCorpseType(stand));
                    if (color != null) Utils.setGlowing(stand, true, color);
                }
            }
        }
    }

    @EventHandler
    private static void onInteractEntity(InteractEntityEvent event) {
        if (active() && hideOpened.value() && event.entity instanceof ArmorStandEntity stand) {
            CorpseType type = getCorpseType(stand);
            if (!type.equals(CorpseType.None) && hasKeyForCorpse(type)) {
                openedCorpses.add(stand.getId());
                Utils.setGlowing(stand, false, RenderColor.white);
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        openedCorpses.clear();
    }

    public enum CorpseType {
        Lapis,
        Tungsten,
        Umber,
        Vanguard,
        None
    }
}
