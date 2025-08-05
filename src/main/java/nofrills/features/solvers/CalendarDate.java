package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.events.DrawItemTooltip;

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
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + " " + DateFormat.getInstance().format(calendar.getTime());
    }

    @EventHandler
    private static void onTooltip(DrawItemTooltip event) {
        if (instance.isActive() && mc.currentScreen instanceof GenericContainerScreen container) {
            if (container.getTitle().getString().equals("Calendar and Events")) {
                for (Text line : event.lines) {
                    String l = Formatting.strip(line.getString());
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
                        event.addLine(Text.of("§c[NF] §eDate of Event: §b" + parseDate(calendar)));
                        String stackName = Formatting.strip(event.stack.getName().getString());
                        if (stackName.endsWith("Spooky Festival")) {
                            calendar.add(Calendar.HOUR, -1);
                            event.addLine(Text.of("§c[NF] §6Fear Mongerer Arrives: §b" + parseDate(calendar)));
                        } else if (stackName.endsWith("Season of Jerry")) {
                            calendar.add(Calendar.HOUR, -7);
                            calendar.add(Calendar.MINUTE, -40);
                            event.addLine(Text.of("§c[NF] §4Workshop Opens: §b" + parseDate(calendar)));
                        }
                        return;
                    }
                }
            }
        }
    }
}
