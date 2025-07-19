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

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (mc.player != null && instance.isActive() && event.messagePlain.endsWith("is now available!")) {
            String ability = event.messagePlain.replace("is now available!", "").trim();
            PlayerInventory inv = mc.player.getInventory();
            for (int i = 0; i <= 35; i++) {
                ItemStack stack = inv.getStack(i);
                if (Utils.hasEitherStat(stack, "Mining Speed") && Utils.getRightClickAbility(stack).contains(ability)) {
                    Utils.showTitleCustom(ability.toUpperCase() + "!", 60, -20, 4.0f, 0xffaa00);
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                    break;
                }
            }
        }
    }
}
