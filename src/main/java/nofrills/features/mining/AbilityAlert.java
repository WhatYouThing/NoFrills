package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ReceivePacketEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class AbilityAlert {
    public static final Feature instance = new Feature("abilityAlert");

    public static int ticks = 0;
    public static String cooldownLine = "";

    private static ItemStack getMiningTool() {
        if (mc.player != null) {
            PlayerInventory inv = mc.player.getInventory();
            for (int i = 0; i <= 35; i++) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty() && !Utils.getRightClickAbility(stack).isEmpty() && Utils.hasEitherStat(stack, "Mining Speed")) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static void showAlert(String ability) {
        Utils.showTitle("§6" + Utils.toUpper(ability) + "!", "", 0, 50, 10);
        Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && !Utils.isInDungeons() && event.messagePlain.endsWith(" is now available!")) {
            String ability = event.messagePlain.replace(" is now available!", "").trim();
            ItemStack tool = getMiningTool();
            if (!tool.isEmpty() && Utils.getRightClickAbility(tool).contains(ability) && cooldownLine.isEmpty()) {
                showAlert(ability);
            }
        }
    }

    @EventHandler
    private static void onPacket(ReceivePacketEvent event) {
        if (instance.isActive() && event.packet instanceof PlayerListS2CPacket packet) {
            String ability = Utils.getRightClickAbility(getMiningTool());
            if (ability.isEmpty()) return;
            for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                if (entry.displayName() == null) continue;
                String name = Utils.toPlain(entry.displayName()).trim();
                int index = name.indexOf(":");
                if (index != -1 && ability.contains(name.substring(0, index))) {
                    if (ticks == 0 && name.endsWith("s")) {
                        String duration = name.substring(name.indexOf(":") + 2);
                        try {
                            int durationTicks = Integer.parseInt(duration.replace("s", "")) * 20;
                            if (durationTicks > 0) {
                                ticks = durationTicks;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    cooldownLine = name;
                    return;
                }
            }
            cooldownLine = "";
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive()) {
            if (ticks > 0) {
                ticks--;
                if (ticks == 0 && !cooldownLine.isEmpty()) {
                    showAlert(cooldownLine.substring(0, cooldownLine.indexOf(":")));
                }
            }
        }
    }
}
