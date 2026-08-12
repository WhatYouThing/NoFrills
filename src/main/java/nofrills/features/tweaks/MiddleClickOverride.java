package nofrills.features.tweaks;

import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

import static nofrills.Main.mc;

public class MiddleClickOverride {
    public static final Feature instance = new Feature("middleClickOverride");

    public static final SettingBool debug = new SettingBool(false, "debug", instance);

    private static final Set<String> matchBlacklist = Set.of(
            "Attribute Fusion",
            "Beacon",
            "Chest",
            "Large Chest",
            "Anvil",
            "Storage",
            "Drill Anvil",
            "Runic Pedestal",
            "Rune Removal",
            "Reforge Anvil",
            "Reforge Item",
            "Offer Pets",
            "Exp Sharing",
            "Convert to Dungeon Item",
            "Upgrade Item",
            "Salvage Items",
            "Fishing Rod Parts",
            "Stats Tuning",
            "Pet Sitter",
            "Transfer to Profile",
            "Kuudra Armor Type Transfer",
            "Composter",
            "Midas Anvil",
            "Hunting Box"
    );
    private static final Set<String> matchWhitelist = Set.of(
            "Stats & Equipment",
            "Accessory Bag Thaumaturgy",
            "Pets",
            "Community Shop"
    );
    private static final Set<String> containBlacklist = Set.of(
            " Minion ",
            "Abiphone",
            Utils.format("A{}iphone", Utils.Symbols.bingo),
            "The Hex",
            "Enchant Item",
            "Auction",
            "Cosmetic",
            "Trap",
            "Gemstones",
            "Heart of the ",
            "Widgets"
    );
    private static final Set<String> containWhitelist = Set.of(
            "Bits Shop"
    );

    private static boolean isLeftClick(int button, ContainerInput actionType) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT && actionType.equals(ContainerInput.PICKUP);
    }

    private static boolean isBlacklisted(String title) {
        return matchBlacklist.stream().anyMatch(s -> Utils.isPaginatedMenu(title, s)) || containBlacklist.stream().anyMatch(title::contains);
    }

    private static boolean isWhitelisted(String title) {
        return matchWhitelist.stream().anyMatch(s -> Utils.isPaginatedMenu(title, s)) || containWhitelist.stream().anyMatch(title::contains);
    }

    private static boolean isTransaction(ItemStack stack) {
        return Utils.getLoreLines(stack).stream().anyMatch(line -> line.equals("Cost") || line.equals("Sell Price") || line.equals("Bazaar Price"));
    }

    private static boolean isInLoadoutEdit(ContainerScreen screen) {
        return screen.getMenu().slots.stream()
                .map(Slot::getItem)
                .filter(stack -> !stack.isEmpty())
                .anyMatch(stack -> {
                    String name = stack.getHoverName().getString();
                    return name.equals("Rename Loadout");
                });
    }

    public static boolean shouldOverride(Slot slot, int button, ContainerInput actionType) {
        if (instance.isActive() && mc.screen instanceof ContainerScreen container && slot != null && isLeftClick(button, actionType)) {
            String title = container.getTitle().getString();
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || isBlacklisted(title) || !Utils.isInSkyblock() || isInLoadoutEdit(container)) {
                return false;
            }
            if (Utils.getSkyblockId(stack).isEmpty() || isWhitelisted(title) || isTransaction(stack)) {
                if (debug.value()) {
                    Utils.infoFormat("Middle Click Override replaced click: slot {}, button {}, {} action type.", slot.index, button, actionType.name());
                }
                return true;
            }
        }
        return false;
    }
}
