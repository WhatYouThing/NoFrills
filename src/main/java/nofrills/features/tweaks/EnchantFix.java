package nofrills.features.tweaks;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class EnchantFix {
    public static final Feature instance = new Feature("enchantFix");

    public static final SettingBool skyblockCheck = new SettingBool(false, "skyblockCheck", instance.key());
    public static final SettingBool modernCheck = new SettingBool(false, "modernCheck", instance.key());

    public static boolean active() {
        boolean isActive = instance.isActive();
        if (isActive) {
            if (skyblockCheck.value() && !Utils.isInSkyblock()) {
                return false;
            }
            if (modernCheck.value() && Utils.isOnModernIsland()) {
                return false;
            }
        }
        return isActive;
    }

    private static double getEnchantLevel(ItemStack stack, String id) {
        ItemEnchantmentsComponent enchants = stack.getComponents().get(DataComponentTypes.ENCHANTMENTS);
        if (enchants != null) {
            for (RegistryEntry<Enchantment> enchant : enchants.getEnchantments()) {
                if (enchant.getIdAsString().equals(id)) {
                    return enchants.getLevel(enchant);
                }
            }
        }
        return 0.0;
    }

    public static double getEfficiencyValue() {
        double level = getEnchantLevel(Utils.getHeldItem(), "minecraft:efficiency");
        return level > 0.0 ? Math.pow(level, 2) + 1 : 0.0;
    }

    public static double getAquaAffinityValue() {
        if (mc.player == null) return 0.2;
        return getEnchantLevel(Utils.getEntityArmor(mc.player).getFirst(), "minecraft:aqua_affinity") > 0.0 ? 1.0 : 0.2;
    }
}
