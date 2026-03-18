package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.InteractEntityEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import nofrills.misc.Utils;

import java.util.HashSet;

import static nofrills.Main.mc;

public class CorpseHighlight {
    public static final Feature instance = new Feature("corpseHighlight");

    public static final SettingBool hideOpened = new SettingBool(true, "hideOpened", instance);
    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Outline, RenderStyle.class, "style", instance);
    public static final SettingColor lapisOutline = new SettingColor(new RenderColor(85, 85, 255, 255), "lapisOutline", instance);
    public static final SettingColor lapisFill = new SettingColor(new RenderColor(85, 85, 255, 127), "lapisFill", instance);
    public static final SettingColor mineralOutline = new SettingColor(new RenderColor(170, 170, 170, 255), "mineralOutline", instance);
    public static final SettingColor mineralFill = new SettingColor(new RenderColor(170, 170, 170, 127), "mineralFill", instance);
    public static final SettingColor yogOutline = new SettingColor(new RenderColor(255, 170, 0, 255), "yogOutline", instance);
    public static final SettingColor yogFill = new SettingColor(new RenderColor(255, 170, 0, 127), "yogFill", instance);
    public static final SettingColor vanguardOutline = new SettingColor(new RenderColor(255, 85, 255, 255), "vanguardOutline", instance);
    public static final SettingColor vanguardFill = new SettingColor(new RenderColor(255, 85, 255, 127), "vanguardFill", instance);

    private static final EntityCache cache = new EntityCache();
    private static final HashSet<Integer> openedCorpses = new HashSet<>();

    private static boolean isActive() {
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
        if (isActive()) {
            for (Entity ent : Utils.getEntities()) {
                if (ent instanceof ArmorStandEntity stand && !stand.isInvisible()) {
                    cache.add(stand);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (isActive() && cache.size() > 0) {
            for (Entity ent : cache.get()) {
                if (hideOpened.value() && openedCorpses.contains(ent.getId())) {
                    continue;
                }
                ArmorStandEntity stand = (ArmorStandEntity) ent;
                Box box = new Box(BlockPos.ofFloored(stand.getEyePos()));
                switch (getCorpseType(stand)) {
                    case Lapis -> event.drawStyled(box, style.value(), false, lapisOutline.value(), lapisFill.value());
                    case Tungsten ->
                            event.drawStyled(box, style.value(), false, mineralOutline.value(), mineralFill.value());
                    case Umber -> event.drawStyled(box, style.value(), false, yogOutline.value(), yogFill.value());
                    case Vanguard ->
                            event.drawStyled(box, style.value(), false, vanguardOutline.value(), vanguardFill.value());
                }
            }
        }
    }

    @EventHandler
    private static void onInteractEntity(InteractEntityEvent event) {
        if (isActive() && hideOpened.value() && event.entity instanceof ArmorStandEntity stand) {
            CorpseType type = getCorpseType(stand);
            if (!type.equals(CorpseType.None) && hasKeyForCorpse(type)) {
                openedCorpses.add(stand.getId());
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
