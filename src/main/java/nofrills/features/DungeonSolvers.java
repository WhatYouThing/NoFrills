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
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ScreenSlotUpdateEvent;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class DungeonSolvers {
    private static final Terminal[] terminalData = {
            new Terminal(Pattern.compile("Correct all the panes!"), "Panes", true),
            new Terminal(Pattern.compile("Change all to same color!"), "Colors", false),
            new Terminal(Pattern.compile("Select all the .* items!"), "Select", false),
            new Terminal(Pattern.compile("What starts with: '.'\\?"), "Starts With", true),
            new Terminal(Pattern.compile("Click in order!"), "In Order", true),
            new Terminal(Pattern.compile("Click the button on time!"), "Melodeez Nuts", false),
    };
    private static final Item[] colorsOrder = {
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE,
    };
    private static final ItemStack backgroundStack = Utils.setStackName(Items.BLACK_STAINED_GLASS_PANE.getDefaultStack(), " ");
    private static final ItemStack firstStack = Utils.setStackName(Items.LIME_CONCRETE.getDefaultStack(), Utils.Symbols.format + "aClick here!");
    private static final ItemStack secondStack = Utils.setStackName(Items.BLUE_CONCRETE.getDefaultStack(), Utils.Symbols.format + "eClick next.");
    private static boolean isTerminalBuilt = false;

    public static Terminal resolveTerminal(String title) {
        for (Terminal terminal : terminalData) {
            if (terminal.pattern.matcher(title).matches()) {
                return terminal;
            }
        }
        return null;
    }

    @EventHandler
    public static void onScreenOpen(ScreenOpenEvent event) {
        isTerminalBuilt = false;
    }

    @EventHandler
    public static void onSlotUpdate(ScreenSlotUpdateEvent event) {
        if (Config.solveTerminals && Utils.isInDungeons() && !isTerminalBuilt) {
            String title = event.screen.getTitle().getString();
            GenericContainerScreenHandler handler = event.screen.getScreenHandler();
            Inventory inventory = handler.getInventory();
            Terminal terminal = resolveTerminal(title);
            if (terminal != null && terminal.solve) {
                isTerminalBuilt = event.isFinal;
                List<Slot> orderSlots = new ArrayList<>();
                for (Slot slot : handler.slots) {
                    ItemStack stack = inventory.getStack(slot.id);
                    if (!stack.isEmpty()) {
                        if (!event.isFinal) {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        } else {
                            switch (terminal.name) {
                                case "Panes": {
                                    if (stack.getItem() == Items.RED_STAINED_GLASS_PANE) {
                                        Utils.setSpoofed(event.screen, slot, firstStack);
                                        Utils.setDisabled(event.screen, slot, false);
                                    }
                                }
                                case "In Order": {
                                    if (stack.getItem() == Items.RED_STAINED_GLASS_PANE) {
                                        orderSlots.add(slot);
                                    }
                                }
                                case "Starts With": {
                                    String character = String.valueOf(title.charAt(title.indexOf("'") + 1)).toLowerCase();
                                    String name = Formatting.strip(stack.getName().getString()).toLowerCase().trim();
                                    if (!name.isEmpty() && name.startsWith(character) && !Utils.hasGlint(stack) && event.isFinal) {
                                        Utils.setSpoofed(event.screen, slot, firstStack);
                                        Utils.setDisabled(event.screen, slot, false);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!orderSlots.isEmpty()) {
                    orderSlots.sort(Comparator.comparingInt(slot -> slot.getStack().getCount()));
                    Slot first = orderSlots.getFirst();
                    Utils.setSpoofed(event.screen, first, firstStack);
                    Utils.setDisabled(event.screen, first, false);
                    if (orderSlots.size() > 1) {
                        Slot second = orderSlots.get(1);
                        Utils.setSpoofed(event.screen, second, secondStack);
                        Utils.setDisabled(event.screen, second, true);
                    }
                }
            }
        }
    }

    public static class Terminal {
        public Pattern pattern;
        public String name;
        public boolean solve; // specify if the terminal should be solved, and if the invalid clicks should be hidden

        public Terminal(Pattern pattern, String name, boolean solve) {
            this.pattern = pattern;
            this.name = name;
            this.solve = solve;
        }
    }
}
