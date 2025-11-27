package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingInt;
import nofrills.events.ChatMsgEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class AbilityAlert {
    public static final Feature instance = new Feature("abilityAlert");

    public static final SettingInt override = new SettingInt(0, "override",instance);

    private static int ticks = 0;

    private static boolean isMiningTool(ItemStack stack) {
        return !stack.isEmpty() && !Utils.getRightClickAbility(stack).isEmpty() && Utils.hasEitherStat(stack, "Mining Speed");
    }

    private static boolean isUsedMessage(String msg) {
        return msg.startsWith("You used your ") && msg.endsWith(" Pickaxe Ability!");
    }

    private static ItemStack getMiningTool() {
        if (mc.player != null) {
            PlayerInventory inv = mc.player.getInventory();
            for (int i = 0; i <= 8; i++) {
                ItemStack stack = inv.getStack(i);
                if (isMiningTool(stack)) return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static String getMiningAbility(ItemStack tool) {
        String ability = Utils.getRightClickAbility(tool);
        return !ability.isEmpty() ? ability.substring(ability.indexOf(":") + 1).replace("RIGHT CLICK", "").trim() : "";
    }

    private static String getCooldownLine(String ability) {
        for (String line : Utils.getTabListLines()) {
            int index = line.indexOf(":");
            if (index != -1 && ability.contains(line.substring(0, index))) {
                return line;
            }
        }
        return "";
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && !Utils.isInDungeons() && isUsedMessage(event.messagePlain)) {
            ToolData data = new ToolData();
            if (!data.tool.isEmpty() && !data.ability.isEmpty() && data.widget.isEmpty()) {
                if (override.value() > 0) {
                    ticks = override.value();
                } else {
                    for (String line : Utils.getLoreLines(data.tool).reversed()) {
                        if (line.startsWith("Cooldown: ")) {
                            String duration = line.substring(line.indexOf(":") + 2).replace("s", "");
                            ticks = Utils.parseInt(duration).orElse(0) * 20;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && !Utils.isInDungeons()) {
            ToolData data = new ToolData();
            if (!data.tool.isEmpty() && !data.ability.isEmpty() && !data.widget.isEmpty()) {
                String duration = data.widget.substring(data.widget.indexOf(":") + 2);
                if (ticks > 1 && duration.equals("Available")) {
                    ticks = 1; // instantly skips cooldown if the server does, such as if the player enters a mineshaft
                }
                if (ticks == 0 && duration.endsWith("s")) {
                    int durationTicks = Utils.parseInt(duration.replace("s", "")).orElse(0);
                    if (durationTicks >= 5) {
                        ticks = durationTicks * 20;
                    }
                }
            }
            if (ticks > 0) {
                ticks--;
                if (ticks == 0 && !data.ability.isEmpty()) {
                    Utils.showTitle("§6" + Utils.toUpper(data.ability) + "!", "", 0, 50, 10);
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                }
            }
        }
    }

    public static class ToolData {
        public ItemStack tool;
        public String ability;
        public String widget;

        public ToolData() {
            this.tool = getMiningTool();
            this.ability = !this.tool.isEmpty() ? getMiningAbility(this.tool) : "";
            this.widget = !this.ability.isEmpty() ? getCooldownLine(this.ability) : "";
        }
    }
}
