package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import java.util.Optional;

import static nofrills.Main.mc;

public class AbilityAlert {
    public static final Feature instance = new Feature("abilityAlert");

    public static int ticks = 0;
    public static String cooldownLine = "";

    private static ItemStack getMiningTool() {
        if (mc.player != null) {
            for (int i = 0; i <= 8; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!Utils.getRightClickAbility(stack).isEmpty() && Utils.hasEitherStat(stack, "Mining Speed")) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static String updateCooldownLine() {
        String ability = Utils.getRightClickAbility(getMiningTool());
        if (!ability.isEmpty()) {
            for (String line : Utils.getTabListLines()) {
                int index = line.indexOf(":");
                if (index != -1 && ability.contains(line.substring(0, index))) {
                    return line;
                }
            }
        }
        return "";
    }

    private static void showAlert(String ability) {
        Utils.showTitle("§6" + Utils.toUpper(ability) + "!", "", 0, 50, 10);
        Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && event.messagePlain.endsWith(" is now available!") && cooldownLine.isEmpty() && ticks == 0 && !Utils.isInDungeons()) {
            String ability = event.messagePlain.replace(" is now available!", "").trim();
            ItemStack tool = getMiningTool();
            if (!tool.isEmpty() && Utils.getRightClickAbility(tool).contains(ability)) {
                showAlert(ability);
            }
        }
    }

    @EventHandler
    private static void onWorldTick(WorldTickEvent event) {
        if (instance.isActive()) {
            cooldownLine = updateCooldownLine();
            if (!cooldownLine.isEmpty()) {
                String duration = cooldownLine.substring(cooldownLine.indexOf(":") + 2);
                if (ticks > 1 && duration.equals("Available")) {
                    ticks = 1;
                }
                if (ticks == 0 && duration.endsWith("s")) {
                    Optional<Integer> durationTicks = Utils.parseInt(duration.replace("s", ""));
                    if (durationTicks.isPresent() && durationTicks.get() > 5) {
                        ticks = durationTicks.get() * 20;
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && ticks > 0) {
            ticks--;
            if (ticks == 0 && !cooldownLine.isEmpty()) {
                showAlert(cooldownLine.substring(0, cooldownLine.indexOf(":")));
            }
        }
    }
}
