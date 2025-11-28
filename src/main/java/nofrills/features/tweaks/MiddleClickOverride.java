package nofrills.features.tweaks;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import nofrills.config.Feature;
import nofrills.events.SlotClickEvent;
import nofrills.features.solvers.ExperimentSolver;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;

import static nofrills.Main.mc;

public class MiddleClickOverride {
    public static final Feature instance = new Feature("middleClickOverride");

    private static final HashSet<String> matchBlacklist = Sets.newHashSet(
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
            Utils.format("A{}iphone", Utils.Symbols.bingo),
            "Fishing Rod Parts",
            "Stats Tuning",
            "Pet Sitter",
            "Transfer to Profile",
            "Attribute Transfer",
            "Hunting Box"
    );

    private static final HashSet<String> matchWhitelist = Sets.newHashSet(
            "Your Equipment and Stats",
            "Accessory Bag Thaumaturgy",
            "Community Shop"
    );

    private static final HashSet<String> containBlacklist = Sets.newHashSet(
            "Wardrobe",
            "Minion",
            "Abiphone",
            "The Hex",
            "Enchant Item",
            "Auction",
            "Cosmetic",
            "Trap",
            "Gemstone",
            "Heart of the",
            "Widgets"
    );

    private static final HashSet<String> containWhitelist = Sets.newHashSet(
            "Pets",
            "Bits Shop"
    );

    public static boolean isBlacklisted(String title) {
        return matchBlacklist.contains(title) || containBlacklist.stream().anyMatch(title::contains);
    }

    public static boolean isWhitelisted(String title) {
        return matchWhitelist.contains(title) || containWhitelist.stream().anyMatch(title::contains);
    }

    public static boolean isTransaction(ItemStack stack) {
        return Utils.getLoreLines(stack).stream().anyMatch(line -> line.equals("Cost") || line.equals("Sell Price") || line.equals("Bazaar Price"));
    }

    private static boolean experimentCheck() {
        return switch (ExperimentSolver.getExperimentType()) {
            case Chronomatron -> ExperimentSolver.chronomatron.value();
            case Ultrasequencer -> ExperimentSolver.ultrasequencer.value();
            case Superpairs -> ExperimentSolver.superpairs.value();
            default -> true;
        };
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onClick(SlotClickEvent event) {
        if (instance.isActive() && Utils.isInSkyblock() && mc.currentScreen instanceof GenericContainerScreen container) {
            if (event.slot != null && event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.actionType.equals(SlotActionType.PICKUP)) {
                String title = container.getTitle().getString();
                ItemStack stack = event.slot.getStack();
                if (!SlotOptions.isDisabled(event.slot) && !stack.isEmpty() && !isBlacklisted(title) && experimentCheck()) {
                    if (Utils.getSkyblockId(stack).isEmpty() || isWhitelisted(title) || isTransaction(stack)) {
                        mc.interactionManager.clickSlot(container.getScreenHandler().syncId, event.slot.id, GLFW.GLFW_MOUSE_BUTTON_3, SlotActionType.CLONE, mc.player);
                        event.cancel();
                    }
                }
            }
        }
    }
}
