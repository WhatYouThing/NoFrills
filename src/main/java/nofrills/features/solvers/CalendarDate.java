package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.events.TooltipRenderEvent;
import nofrills.misc.Utils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import static nofrills.Main.mc;

public class CalendarDate {
    public static final Feature instance = new Feature("calendarDate");

    private static int parseTime(String time, String unit) {
        int index = time.indexOf(unit);
        return index != -1 ? Integer.parseInt(time.substring(Math.max(0, time.lastIndexOf(" ", index)), index).trim()) : 0;
    }

    private static String parseDate(Calendar calendar) {
        return Utils.format("{} {}",
                calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()),
                DateFormat.getInstance().format(calendar.getTime())
        );
    }

    private static Text buildLine(String prefix, Calendar calendar) {
        return Text.literal(Utils.format("{}: §b{}", prefix, parseDate(calendar)));
    }

    @EventHandler
    private static void onTooltip(TooltipRenderEvent event) {
        if (instance.isActive() && mc.currentScreen instanceof GenericContainerScreen container) {
            if (container.getTitle().getString().equals("Calendar and Events")) {
                for (Text line : event.lines) {
                    String l = Utils.toPlainString(line);
                    if (l.startsWith("Starts in: ")) {
                        String time = l.substring(l.indexOf(":")).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_MONTH, parseTime(time, "d"));
                        calendar.add(Calendar.HOUR, parseTime(time, "h"));
                        calendar.add(Calendar.MINUTE, parseTime(time, "m"));
                        calendar.add(Calendar.SECOND, parseTime(time, "s"));
                        int second = calendar.get(Calendar.SECOND);
                        if (second % 5 != 0) { // scuffed patch for when the second is slightly off in the GUI
                            calendar.add(Calendar.SECOND, 5 - (second % 5));
                        }
                        event.addLine(Text.of(""));
                        event.addLine(Utils.getShortTag().append(buildLine("§eDate of Event", calendar)));
                        String stackName = Utils.toPlainString(event.stack.getName());
                        if (stackName.endsWith("Spooky Festival")) {
                            calendar.add(Calendar.HOUR, -1);
                            event.addLine(Utils.getShortTag().append(buildLine("§6Fear Mongerer Arrives", calendar)));
                        } else if (stackName.endsWith("Season of Jerry")) {
                            calendar.add(Calendar.HOUR, -7);
                            calendar.add(Calendar.MINUTE, -40);
                            event.addLine(Utils.getShortTag().append(buildLine("§cWorkshop Opens", calendar)));
                        }
                        return;
                    }
                }
            }
        }
    }
}
