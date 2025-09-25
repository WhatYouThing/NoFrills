package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class AbilityAlert {
    public static final Feature instance = new Feature("abilityAlert");

    private static ItemStack getMiningTool(String ability) {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i <= 35; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && Utils.getRightClickAbility(stack).contains(ability) && Utils.hasEitherStat(stack, "Mining Speed")) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && mc.player != null) {
            String msg = event.getPlainMessage();
            if (msg.endsWith(" is now available!")) {
                String ability = msg.replace(" is now available!", "").trim();
                if (!getMiningTool(ability).isEmpty()) {
                    Utils.showTitle("§6" + Utils.toUpper(ability) + "!", "", 0, 50, 10);
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                }
            }
        }
    }
}
