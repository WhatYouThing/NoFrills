package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.events.OverlayMsgEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import java.util.Optional;

public class SkillTracker {
    public static final Feature instance = new Feature("skillTracker");

    public static long countedTicks = 0;
    public static long totalTicks = 0;
    public static long pauseTicks = 0;
    public static double lastExp = 0;
    public static double currentExp = 0;

    private static boolean isPaused() {
        return pauseTicks >= 1200;
    }

    public static String formatTicks(long ticks) {
        long current = ticks;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        StringBuilder builder = new StringBuilder();
        while (current >= 72000) {
            hours++;
            current -= 72000;
        }
        if (hours > 0) {
            builder.append(hours).append("h");
        }
        while (current >= 1200) {
            minutes++;
            current -= 1200;
        }
        if (minutes > 0) {
            builder.append(minutes).append("m");
        }
        while (current >= 20) {
            seconds++;
            current -= 20;
        }
        if (seconds > 0) {
            builder.append(seconds).append("s");
        }
        return builder.toString();
    }

    public static MutableText getText() {
        MutableText text = Text.literal("§bSkill Tracker");
        text.append(Utils.format("\nTime Elapsed: {}", formatTicks(totalTicks)));
        text.append(Utils.format("\nTime Counter: {}", formatTicks(countedTicks)));
        text.append(Utils.format("\nEXP Gained: {}", Utils.formatSeparator(currentExp)));
        text.append(Utils.format("\nEXP per hour: {}", Utils.formatSeparator(currentExp / (countedTicks / 72000.0))));
        return text;
    }

    @EventHandler
    private static void onOverlay(OverlayMsgEvent event) {
        if (instance.isActive()) {
            String msg = event.messagePlain;
            int index = msg.indexOf("Mining");
            if (index != -1) {
                String expPart = msg.substring(msg.indexOf("(", index) + 1, msg.indexOf("/", index)).replaceAll(",", "");
                Optional<Double> exp = Utils.parseDouble(expPart);
                if (exp.isPresent() && lastExp != exp.get()) {
                    if (lastExp != 0) {
                        currentExp += exp.get() - lastExp;
                    }
                    pauseTicks = 0;
                    lastExp = exp.get();
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            if (!isPaused()) {
                countedTicks++;
                pauseTicks++;
            }
            totalTicks++;
        } else {
            countedTicks = 0;
            totalTicks = 0;
            pauseTicks = 0;
        }
    }
}
