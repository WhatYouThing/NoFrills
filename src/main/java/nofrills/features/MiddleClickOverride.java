package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import nofrills.config.Config;
import nofrills.events.InputEvent;
import nofrills.misc.SlotOptions;
import nofrills.misc.Utils;
import nofrills.mixin.HandledScreenAccessor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static nofrills.Main.mc;

public class MiddleClickOverride {
    private static final List<String> matchBlacklist = List.of(
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
            "Auctions Browser",
            Utils.format("A{}iphone", Utils.Symbols.bingo)
    );

    private static final List<String> containBlacklist = List.of(
            "Wardrobe",
            "Minion",
            "Abiphone",
            "The Hex",
            "Enchant Item"
    );

    private static final List<String> matchWhitelist = List.of(
            "Your Equipment and Stats"
    );

    private static final List<String> containWhitelist = List.of(
            "Pets"
    );

    private static boolean isBlacklisted(String title) {
        return matchBlacklist.stream().anyMatch(title::equals) || containBlacklist.stream().anyMatch(title::contains);
    }

    private static boolean isWhitelisted(String title) {
        return matchWhitelist.stream().anyMatch(title::equals) || containWhitelist.stream().anyMatch(title::contains);
    }

    private static boolean isTransaction(ItemStack stack) {
        return Utils.getLoreLines(stack).stream().anyMatch(line -> line.equals("Cost") || line.equals("Sell Price") || line.equals("Bazaar Price"));
    }

    @EventHandler
    private static void onClick(InputEvent event) {
        if (Config.middleClickOverride && event.key == GLFW.GLFW_MOUSE_BUTTON_1 && event.modifiers == 0 && event.action == GLFW.GLFW_PRESS) {
            if (mc.currentScreen instanceof GenericContainerScreen container) {
                String title = container.getTitle().getString();
                Slot focusedSlot = ((HandledScreenAccessor) mc.currentScreen).getFocusedSlot();
                if (focusedSlot != null && !isBlacklisted(title) && !Utils.isLeapMenu(title) && !SlotOptions.isSlotDisabled(focusedSlot)) {
                    ItemStack stack = focusedSlot.getStack();
                    if (!stack.isEmpty()) {
                        if (Utils.getSkyblockId(stack).isEmpty() || isTransaction(stack) || isWhitelisted(container.getTitle().getString())) {
                            mc.interactionManager.clickSlot(container.getScreenHandler().syncId, focusedSlot.id, GLFW.GLFW_MOUSE_BUTTON_3, SlotActionType.CLONE, mc.player);
                            event.cancel();
                        }
                    }
                }
            }
        }
    }
}
