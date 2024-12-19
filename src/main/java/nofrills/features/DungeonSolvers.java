package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.ScreenSlotUpdateEvent;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class DungeonSolvers {
    private static final Pattern panesRegex = Pattern.compile("Correct all the panes!");
    private static final Pattern colorsRegex = Pattern.compile("Change all to same color!");
    private static final Pattern selectRegex = Pattern.compile("Select all the .* items!");
    private static final Pattern startsWithRegex = Pattern.compile("What starts with: '.'\\?");
    private static final Pattern inOrderRegex = Pattern.compile("Click in order!");
    private static final Item[] colorsOrder = {
            Items.GREEN_STAINED_GLASS_PANE
    };
    private static final ItemStack backgroundStack = Utils.setStackName(Items.BLACK_STAINED_GLASS_PANE.getDefaultStack(), " ");
    private static final ItemStack firstStack = Utils.setStackName(Items.LIME_CONCRETE.getDefaultStack(), Utils.Symbols.format + "aClick here!");
    private static final ItemStack secondStack = Utils.setStackName(Items.YELLOW_CONCRETE.getDefaultStack(), Utils.Symbols.format + "eClick next.");
    private static final ItemStack thirdStack = Utils.setStackName(Items.ORANGE_CONCRETE.getDefaultStack(), Utils.Symbols.format + "cClick later.");

    @EventHandler
    public static void onInventory(ScreenSlotUpdateEvent event) {
        if (Config.solveTerminals && Utils.isInDungeons() && event.packet.getSlot() != -1) {
            String title = event.screen.getTitle().getString();
            GenericContainerScreenHandler handler = event.screen.getScreenHandler();
            Inventory inventory = handler.getInventory();
            if (panesRegex.matcher(title).matches()) {
                for (Slot slot : handler.slots) {
                    ItemStack stack = inventory.getStack(slot.id);
                    if (!stack.isEmpty()) {
                        if (stack.getItem() == Items.RED_STAINED_GLASS_PANE && event.isFinal) {
                            Utils.setSpoofed(event.screen, slot, firstStack);
                            Utils.setDisabled(event.screen, slot, false);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                }
            } else if (inOrderRegex.matcher(title).matches()) {
                List<Slot> slots = new ArrayList<>();
                for (Slot slot : handler.slots) {
                    ItemStack stack = inventory.getStack(slot.id);
                    if (!stack.isEmpty()) {
                        if (stack.getItem() == Items.RED_STAINED_GLASS_PANE && event.isFinal) {
                            slots.add(slot);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                }
                if (!slots.isEmpty()) {
                    slots.sort(Comparator.comparingInt(slot -> slot.getStack().getCount()));
                    Utils.setSpoofed(event.screen, slots.getFirst(), firstStack);
                    Utils.setDisabled(event.screen, slots.getFirst(), false);
                    if (slots.size() > 2) {
                        Utils.setSpoofed(event.screen, slots.get(2), thirdStack);
                        Utils.setDisabled(event.screen, slots.get(2), false);
                    }
                    if (slots.size() > 1) {
                        Utils.setSpoofed(event.screen, slots.get(1), secondStack);
                        Utils.setDisabled(event.screen, slots.get(1), false);
                    }
                }
            } else if (startsWithRegex.matcher(title).matches()) {
                String character = String.valueOf(title.charAt(title.indexOf("'") + 1)).toLowerCase();
                for (Slot slot : handler.slots) {
                    ItemStack stack = inventory.getStack(slot.id);
                    if (!stack.isEmpty()) {
                        String name = Formatting.strip(stack.getName().getString()).toLowerCase();
                        if (name.startsWith(character) && !Utils.hasGlint(stack) && event.isFinal) {
                            Utils.setSpoofed(event.screen, slot, firstStack);
                            Utils.setDisabled(event.screen, slot, false);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                }
            }
        }
    }
}
