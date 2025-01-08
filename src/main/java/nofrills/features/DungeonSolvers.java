package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ScreenSlotUpdateEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DungeonSolvers {
    private static final ItemStack backgroundStack = Utils.setStackName(Items.BLACK_STAINED_GLASS_PANE.getDefaultStack(), " ");
    private static final ItemStack firstStack = Utils.setStackName(Items.LIME_CONCRETE.getDefaultStack(), Utils.Symbols.format + "aClick here!");
    private static final ItemStack secondStack = Utils.setStackName(Items.BLUE_CONCRETE.getDefaultStack(), Utils.Symbols.format + "9Click next.");
    private static final Item[] colorsOrder = {
            Items.GREEN_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE,
    };
    public static boolean isInTerminal = false;
    private static boolean isTerminalBuilt = false;
    private static int melodyTicks = 0;

    public static boolean checkStackColor(ItemStack stack, DyeColor color, String colorName) {
        Item item = stack.getItem();
        if (Formatting.strip(stack.getName().getString()).trim().isEmpty()) {
            return false;
        }
        if (stack.getItem().toString().startsWith("minecraft:" + colorName)) {
            return true;
        }
        return switch (color) {
            case BLACK -> item.equals(Items.INK_SAC);
            case BLUE -> item.equals(Items.LAPIS_LAZULI);
            case BROWN -> item.equals(Items.COCOA_BEANS);
            case WHITE -> item.equals(Items.BONE_MEAL);
            default -> false;
        };
    }

    @EventHandler
    public static void onScreenOpen(ScreenOpenEvent event) {
        isTerminalBuilt = false;
        isInTerminal = false;

        if (Utils.isInDungeons() && event.screen.getTitle().getString().equals("Click the button on time!")) {
            isInTerminal = true;
            if (Config.melodyAnnounce) {
                if (melodyTicks == 0 && !Config.melodyMessage.isEmpty()) {
                    Utils.sendMessage(Config.melodyMessage);
                    melodyTicks = 100;
                }
            }
        }
    }

    @EventHandler
    public static void onTick(WorldTickEvent event) {
        if (melodyTicks > 0) {
            melodyTicks--;
        }
    }

    @EventHandler
    public static void onSlotUpdate(ScreenSlotUpdateEvent event) {
        if (Config.solveTerminals && Utils.isInDungeons() && !isTerminalBuilt) {
            String title = event.screen.getTitle().getString();
            GenericContainerScreenHandler handler = event.screen.getScreenHandler();
            Inventory inventory = handler.getInventory();
            isTerminalBuilt = event.isFinal;
            List<Slot> orderSlots = new ArrayList<>();
            for (Slot slot : handler.slots) {
                ItemStack stack = inventory.getStack(slot.id);
                if (!stack.isEmpty()) {
                    if (title.startsWith("Correct all the panes!")) {
                        isInTerminal = true;
                        if (stack.getItem() == Items.RED_STAINED_GLASS_PANE) {
                            Utils.setSpoofed(event.screen, slot, firstStack);
                            Utils.setDisabled(event.screen, slot, false);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                    if (title.startsWith("Click in order!")) {
                        isInTerminal = true;
                        if (stack.getItem() == Items.RED_STAINED_GLASS_PANE && event.isFinal) {
                            orderSlots.add(slot);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                    if (title.startsWith("What starts with:") && title.endsWith("?")) {
                        isInTerminal = true;
                        String character = String.valueOf(title.charAt(title.indexOf("'") + 1)).toLowerCase();
                        String name = Formatting.strip(stack.getName().getString()).toLowerCase().trim();
                        if (!name.isEmpty() && name.startsWith(character) && !Utils.hasGlint(stack)) {
                            Utils.setSpoofed(event.screen, slot, firstStack);
                            Utils.setDisabled(event.screen, slot, false);
                        } else {
                            Utils.setSpoofed(event.screen, slot, backgroundStack);
                            Utils.setDisabled(event.screen, slot, true);
                        }
                    }
                    if (title.startsWith("Select all the") && title.endsWith("items!")) {
                        isInTerminal = true;
                        String color = title.replace("Select all the", "").replace("items!", "").trim();
                        String colorName = color.equals("SILVER") ? "light_gray" : color.toLowerCase().replace(" ", "_");
                        for (DyeColor dye : DyeColor.values()) {
                            if (dye.getName().equals(colorName)) {
                                if (!Utils.hasGlint(stack) && checkStackColor(stack, dye, colorName)) {
                                    Utils.setSpoofed(event.screen, slot, firstStack);
                                    Utils.setDisabled(event.screen, slot, false);
                                } else {
                                    Utils.setSpoofed(event.screen, slot, backgroundStack);
                                    Utils.setDisabled(event.screen, slot, true);
                                }
                                break;
                            }
                        }
                    }
                    if (title.startsWith("Change all to same color!")) {
                        isInTerminal = true;
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
