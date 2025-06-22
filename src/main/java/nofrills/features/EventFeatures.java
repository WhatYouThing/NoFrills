package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.events.DrawItemTooltip;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import static nofrills.Main.Config;
import static nofrills.Main.mc;

public class EventFeatures {
    private static final EntityCache chestList = new EntityCache();

    private static boolean isSpooky() {
        for (String line : SkyblockData.getLines()) {
            if (line.startsWith("Spooky Festival") && line.contains(":")) {
                return true;
            }
        }
        return false;
    }

    private static int parseTime(String time, String unit) {
        int index = time.indexOf(unit);
        return index != -1 ? Integer.parseInt(time.substring(Math.max(0, time.lastIndexOf(" ", index)), index).trim()) : 0;
    }

    private static String parseDate(Calendar calendar) {
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + " " + DateFormat.getInstance().format(calendar.getTime());
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (isSpooky()) {
            String name = event.namePlain.toLowerCase();
            if (name.equals("trick or treat?") || name.equals("party chest")) {
                if (event.entity.distanceTo(mc.player) <= 16.0f && !chestList.has(event.entity)) {
                    if (Config.spookyChestAlert()) {
                        Utils.showTitle("§6§lCHEST SPAWNED!", "", 5, 20, 5);
                        Utils.playSound(SoundEvents.BLOCK_VAULT_ACTIVATE, SoundCategory.MASTER, 1.0f, 1.0f);
                    }
                    chestList.add(event.entity);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        for (Entity chest : chestList.get()) {
            if (Config.spookyChestHighlight()) {
                BlockPos pos = Utils.findGround(chest.getBlockPos(), 4).up(1);
                event.drawFilledWithBeam(Box.enclosing(pos, pos), 256, true, RenderColor.fromColor(Config.spookyChestHighlightColor()));
            }
        }
    }

    @EventHandler
    private static void onTooltip(DrawItemTooltip event) {
        if (Config.calendarDate() && mc.currentScreen instanceof GenericContainerScreen container) {
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
                        if (second % 5 != 0) { // scuffed patch for when the calendar is desynced and the second is slightly off
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
