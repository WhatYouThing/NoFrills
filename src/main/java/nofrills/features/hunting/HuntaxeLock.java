package nofrills.features.hunting;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import nofrills.config.Feature;
import nofrills.events.InteractBlockEvent;
import nofrills.events.InteractItemEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

public class HuntaxeLock {
    public static final Feature instance = new Feature("huntaxeLock");

    private static int lockTicks = 0;

    private static boolean isHoldingHuntaxe() {
        ItemStack item = Utils.getHeldItem();
        if (!item.isEmpty() && item.getItem().equals(Items.GOLDEN_AXE)) {
            return Utils.getRightClickAbility(item).contains("Ability: Absorptio ");
        }
        return false;
    }

    @EventHandler
    private static void onInteractItem(InteractItemEvent event) {
        if (instance.isActive() && isHoldingHuntaxe()) {
            if (lockTicks == 0) {
                event.cancel();
            }
            lockTicks = 10;
        }
    }

    @EventHandler
    private static void onInteractBlock(InteractBlockEvent event) {
        if (instance.isActive() && isHoldingHuntaxe()) {
            if (lockTicks == 0) {
                event.cancel();
            }
            lockTicks = 10;
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && lockTicks > 0) {
            if (isHoldingHuntaxe()) {
                lockTicks--;
            } else {
                lockTicks = 0;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        lockTicks = 0;
    }
}
