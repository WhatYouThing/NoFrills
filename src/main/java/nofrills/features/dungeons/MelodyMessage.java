package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingString;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.Utils;

import java.util.List;

public class MelodyMessage {
    public static final Feature instance = new Feature("melodyMessage");

    public static final SettingString msg = new SettingString("/pc Melody Terminal start!", "msg", instance.key());
    public static final SettingBool progress = new SettingBool(false, "progress", instance.key());
    public static final SettingString progressMsg = new SettingString("/pc Melody {percent}", "progressMsg", instance.key());

    private static int lastCount = 4;

    private static boolean isMelody(String title) {
        return TerminalSolvers.getTerminalType(title).equals(TerminalSolvers.TerminalType.Melody);
    }

    private static void resetCount() {
        lastCount = 4;
    }

    @EventHandler
    private static void onScreenOpen(ScreenOpenEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("7") && isMelody(event.screen.getTitle().getString())) {
            Utils.sendMessage(msg.value());
            resetCount();
        }
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("7") && !event.isInventory && isMelody(event.title) && progress.value()) {
            List<Slot> slots = Utils.getContainerSlots(event.handler).reversed();
            if (slots.stream().filter(slot -> !slot.getStack().isEmpty()).toList().size() != 54) {
                return; // quick and dirty check for if the screen is built
            }
            int count = 0;
            for (Slot slot : slots) {
                Item item = slot.getStack().getItem();
                if (item.equals(Items.RED_TERRACOTTA) || item.equals(Items.LIME_TERRACOTTA)) {
                    count++;
                } // go from last slot to first, count every terracotta item, find the terminal progress from the first lime terracotta
                if (item.equals(Items.LIME_TERRACOTTA) && count < lastCount) {
                    String percent = switch (count) {
                        case 1 -> "75%";
                        case 2 -> "50%";
                        case 3 -> "25%";
                        default -> "";
                    };
                    if (!percent.isEmpty()) {
                        Utils.sendMessage(progressMsg.value().replace("{percent}", percent));
                        lastCount = count;
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        resetCount();
    }
}