package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.ReceivePacketEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class ExperimentSolver {
    public static final Pattern ultraPattern = Pattern.compile("[0-9]{1,2}?");
    public static final List<ChronoElement> chronoElements = new ArrayList<>();
    public static boolean ultraRemember = false;
    public static ChronoElement chronoPartial = null;
    public static String chronoLast = "";
    public static boolean chronoRemember = true;

    public static void setClickNow(int slotId) {
        ItemStack item = Items.LIME_CONCRETE.getDefaultStack();
        item.set(DataComponentTypes.CUSTOM_NAME, Text.of("§aClick here!"));
        Utils.setDisabled(mc.currentScreen, slotId, false, item);
    }

    public static void setClickLater(int slotId) {
        ItemStack item = Items.YELLOW_CONCRETE.getDefaultStack();
        item.set(DataComponentTypes.CUSTOM_NAME, Text.of("§eClick next."));
        Utils.setDisabled(mc.currentScreen, slotId, true, item);
    }

    public static void setBackground(int slotId) {
        ItemStack item = Items.GRAY_STAINED_GLASS_PANE.getDefaultStack();
        item.set(DataComponentTypes.CUSTOM_NAME, Text.of(""));
        Utils.setDisabled(mc.currentScreen, slotId, true, item);
    }

    public static void setClickNow(ChronoElement element) {
        if (element.slot1 != -1) {
            setClickNow(element.slot1);
        }
        if (element.slot2 != -1) {
            setClickNow(element.slot2);
        }
        if (element.slot3 != -1) {
            setClickNow(element.slot3);
        }
    }

    public static void setClickLater(ChronoElement element) {
        if (element.slot1 != -1) {
            setClickLater(element.slot1);
        }
        if (element.slot2 != -1) {
            setClickLater(element.slot2);
        }
        if (element.slot3 != -1) {
            setClickLater(element.slot3);
        }
    }

    public static void setBackground(ChronoElement element) {
        if (element.slot1 != -1) {
            setBackground(element.slot1);
        }
        if (element.slot2 != -1) {
            setBackground(element.slot2);
        }
        if (element.slot3 != -1) {
            setBackground(element.slot3);
        }
    }

    private static boolean isColor(String name) {
        return switch (name) {
            case "Red", "Blue", "Lime", "Yellow", "Aqua", "Pink", "Green", "Cyan", "Orange", "Purple" -> true;
            default -> false;
        };
    }

    @EventHandler
    public static void onPacket(ReceivePacketEvent event) {
        if (Config.solveExperiments && Utils.isOnPrivateIsland()) {
            if (event.packet instanceof ScreenHandlerSlotUpdateS2CPacket slotUpdate) {
                if (mc.currentScreen instanceof GenericContainerScreen containerScreen) {
                    GenericContainerScreenHandler handler = containerScreen.getScreenHandler();
                    int newSlot = slotUpdate.getSlot();
                    ItemStack newStack = slotUpdate.getStack();
                    Item newItem = newStack.getItem();
                    if (containerScreen.getTitle().getString().startsWith("Ultrasequencer (")) {
                        if (newItem.equals(Items.GLOWSTONE)) {
                            ultraRemember = true;
                        } else if (newItem.equals(Items.CLOCK)) {
                            ultraRemember = false;
                        }
                        if (newSlot != -1 && ultraRemember) {
                            ItemStack oldStack = handler.getSlot(newSlot).getStack();
                            String newStackName = newStack.getName().getString().trim();
                            String oldStackName = Formatting.strip(oldStack.getName().getString());
                            if (ultraPattern.matcher(oldStackName).matches() && newStackName.isEmpty()) {
                                event.cancel();
                            }
                        }
                    }
                    if (containerScreen.getTitle().getString().startsWith("Chronomatron (")) {
                        if (newItem.equals(Items.GLOWSTONE)) {
                            chronoRemember = true;
                        } else if (newItem.equals(Items.CLOCK) && chronoRemember) {
                            chronoRemember = false;
                            if (!chronoElements.isEmpty()) {
                                ChronoElement first = chronoElements.getFirst();
                                setClickNow(first);
                                if (chronoElements.size() > 1) {
                                    ChronoElement second = chronoElements.get(1);
                                    if (first.slot1 != second.slot1) {
                                        setClickLater(second);
                                    }
                                }
                            }
                        }
                        String name = Formatting.strip(newStack.getName().getString());
                        if (newSlot != -1 && isColor(name)) {
                            if (slotUpdate.getStack().hasGlint()) {
                                if (chronoRemember) {
                                    if (chronoPartial == null) {
                                        chronoPartial = new ChronoElement(newSlot, -1, -1);
                                    } else if (chronoPartial.slot2 == -1) {
                                        chronoPartial.slot2 = newSlot;
                                    } else if (chronoPartial.slot3 == -1) {
                                        chronoPartial.slot3 = newSlot;
                                    }
                                } else {
                                    if (!chronoElements.isEmpty()) {
                                        ChronoElement first = chronoElements.getFirst();
                                        if (first.slot1 != -1) {
                                            first.slot1 = -1;
                                            setBackground(first);
                                            if (!chronoElements.isEmpty()) {
                                                ChronoElement next = chronoElements.getFirst();
                                                setClickNow(next);
                                                if (chronoElements.size() > 1) {
                                                    ChronoElement nextNext = chronoElements.get(1);
                                                    if (next.slot1 != nextNext.slot1) {
                                                        setClickLater(nextNext);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (chronoRemember) {
                                    if (chronoPartial != null && chronoPartial.slot1 != -1 && chronoPartial.slot2 != -1) {
                                        chronoElements.add(chronoPartial);
                                        chronoPartial = null;
                                    }
                                } else {
                                    if (!chronoElements.isEmpty() && chronoElements.getFirst().slot1 == -1) {
                                        chronoElements.removeFirst();
                                    }
                                }
                            }
                            event.cancel();
                        }
                    }
                    if (containerScreen.getTitle().getString().equalsIgnoreCase("Experiment Over")) {
                        chronoElements.clear();
                        ultraRemember = false;
                        chronoPartial = null;
                        chronoLast = "";
                        chronoRemember = true;
                    }
                }
            }
        }
    }

    @EventHandler
    public static void onTick(WorldTickEvent event) {
        if (Config.solveExperiments && Utils.isOnPrivateIsland()) {
            if (mc.currentScreen instanceof GenericContainerScreen containerScreen) {
                GenericContainerScreenHandler handler = containerScreen.getScreenHandler();
                if (containerScreen.getTitle().getString().startsWith("Chronomatron (")) {
                    for (Slot slot : handler.slots) {
                        String name = Formatting.strip(slot.getStack().getName().getString());
                        if (isColor(name)) {
                            setBackground(slot.id);
                        }
                    }
                }
            }
        }
    }

    public static class ChronoElement {
        public int slot1;
        public int slot2;
        public int slot3;

        public ChronoElement(int slot1, int slot2, int slot3) {
            this.slot1 = slot1;
            this.slot2 = slot2;
            this.slot3 = slot3;
        }
    }
}
