package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.TooltipRenderEvent;
import nofrills.misc.Utils;

import java.util.Calendar;
import java.util.Locale;

public class InfoTooltips {
    public static final Feature instance = new Feature("infoTooltips");

    public static final SettingBool dungeonQuality = new SettingBool(false, "dungeonQuality", instance);
    public static final SettingBool createdDate = new SettingBool(false, "createdDate", instance);
    public static final SettingBool hexColor = new SettingBool(false, "hexColor", instance);
    public static final SettingBool museumDonated = new SettingBool(false, "museumDonated", instance);

    @EventHandler
    private static void onTooltip(TooltipRenderEvent event) {
        if (instance.isActive()) {
            if (dungeonQuality.value() && event.customData != null) {
                int boost = event.customData.getInt("baseStatBoostPercentage", 0);
                int tier = event.customData.getInt("item_tier", 0);
                if (boost != 0) {
                    String color = boost == 50 ? "§6§l" : "§6";
                    event.addLine(Utils.getShortTag().append(Utils.format("§bQuality: {}{}/50, Tier {}", color, boost, tier)));
                }
            }
            if (createdDate.value() && event.customData != null) {
                long timestamp = event.customData.getLong("timestamp", 0L);
                if (timestamp != 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    event.addLine(Utils.getShortTag().append(Utils.format("§bCreated: §6{}", Utils.parseDate(calendar))));
                }
            }
            if (hexColor.value()) {
                DyedColorComponent color = event.stack.get(DataComponentTypes.DYED_COLOR);
                if (color != null) {
                    String hex = Utils.toLower(String.format(Locale.ROOT, "#%06X", color.rgb()));
                    event.addLine(Utils.getShortTag().append(Utils.format("§bDye Color: §6{}", hex)));
                }
            }
            if (museumDonated.value() && event.customData != null) {
                byte donated = event.customData.getByte("donated_museum", (byte) 0);
                if (donated != 0) {
                    event.addLine(Utils.getShortTag().append("§bDonated to Museum"));
                }
            }
        }
    }
}
