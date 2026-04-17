package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingInt;
import nofrills.events.ChatMsgEvent;
import nofrills.events.InventoryUpdateEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.hud.HudManager.pickAbilityTimer;

public class AbilityAlert {
    public static final Feature instance = new Feature("abilityAlert");

    public static final SettingInt override = new SettingInt(0, "override", instance);

    private static ToolData toolData = ToolData.empty();
    private static int ticks = 0;

    private static boolean isMiningTool(ItemStack stack) {
        if (!stack.isEmpty()) {
            List<String> lines = Utils.getLoreLines(stack);
            if (!lines.isEmpty()) {
                String last = lines.getLast();
                if (last.contains(" DRILL") || last.contains(" PICKAXE") || last.contains(" GAUNTLET")) {
                    return Utils.hasEitherStat(stack, "Mining Speed");
                }
            }
        }
        return false;
    }

    private static boolean isUsedMessage(String msg) {
        return msg.startsWith("You used your ") && msg.endsWith(" Pickaxe Ability!");
    }

    private static String getMiningAbility(ItemStack tool) {
        String ability = Utils.getRightClickAbility(tool);
        return !ability.isEmpty() ? ability.substring(ability.indexOf(":") + 1).replace("RIGHT CLICK", "").trim() : "";
    }

    private static void setCooldown(int tickDuration) {
        ticks = tickDuration;
        if (pickAbilityTimer.isActive()) {
            pickAbilityTimer.start(tickDuration * 50L);
        }
    }

    private static String getWidget() {
        if (!toolData.isEmpty()) {
            List<String> lines = Utils.getTabListLines();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.equals("Pickaxe Ability:") && i + 1 < lines.size()) {
                    String next = lines.get(i + 1);
                    return next.contains(":") ? next : "";
                }
            }
        }
        return "";
    }

    @EventHandler
    private static void onInventory(InventoryUpdateEvent event) {
        if (instance.isActive() && isMiningTool(event.stack)) {
            toolData = new ToolData(event.stack);
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && !Utils.isInDungeons() && isUsedMessage(event.messagePlain)) {
            if (!toolData.isEmpty() && getWidget().isEmpty()) {
                if (pickAbilityTimer.isActive()) {
                    pickAbilityTimer.setCurrentAbility(toolData.ability);
                }
                if (override.value() > 0) {
                    setCooldown(override.value());
                } else {
                    for (String line : Utils.getLoreLines(toolData.tool).reversed()) {
                        if (line.startsWith("Cooldown: ")) {
                            String duration = line.substring(line.indexOf(":") + 2).replace("s", "");
                            Utils.parseInt(duration).ifPresent(seconds -> setCooldown(seconds * 20));
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && !Utils.isInDungeons() && !toolData.isEmpty()) {
            String widget = getWidget();
            if (!widget.isEmpty()) {
                String duration = widget.substring(widget.indexOf(":") + 2);
                if (pickAbilityTimer.isActive()) {
                    pickAbilityTimer.setCurrentAbility(widget.substring(0, widget.indexOf(":")));
                }
                if (ticks > 1 && duration.equals("Available")) {
                    setCooldown(1); // instantly skips cooldown if the server does, such as if the player enters a mineshaft
                }
                if (ticks == 0 && duration.endsWith("s")) {
                    Utils.parseInt(duration.replace("s", "")).ifPresent(seconds -> {
                        if (seconds < 10) return;
                        setCooldown(seconds * 20 + 20);
                    });
                }
            }
            if (ticks > 0) {
                ticks--;
                if (ticks == 0) {
                    Utils.showTitle("§6" + Utils.toUpper(toolData.ability) + "!", "", 0, 50, 10);
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                }
            }
        }
    }

    public static class ToolData {
        public ItemStack tool;
        public String ability;

        public ToolData(ItemStack stack) {
            this.tool = stack;
            this.ability = !this.tool.isEmpty() ? getMiningAbility(this.tool) : "";
        }

        public static ToolData empty() {
            return new ToolData(ItemStack.EMPTY);
        }

        public boolean isEmpty() {
            return this.tool == null || this.tool.isEmpty() || this.ability.isEmpty();
        }
    }
}
