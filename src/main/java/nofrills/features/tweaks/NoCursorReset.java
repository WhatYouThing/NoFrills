package nofrills.features.tweaks;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.Screen;
import nofrills.config.Feature;
import nofrills.config.SettingInt;
import nofrills.events.EventListener;
import nofrills.events.ScreenCloseEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.WorldTickEvent;
import nofrills.features.dungeons.LeapOverlay;

import static nofrills.Main.mc;

@EventListener
public class NoCursorReset {
    public static final Feature instance = new Feature("noCursorReset");

    public static final SettingInt clearTicks = new SettingInt(200, "clearTicks", instance);

    public static int ticks = 0;
    public static double cursorX = -1.0;
    public static double cursorY = -1.0;

    public static boolean isActive(Screen screen) {
        return instance.isActive() && !LeapOverlay.isLeapMenu(screen.getTitle().getString());
    }

    public static boolean isActive() {
        return isActive(mc.screen);
    }

    public static boolean isPosStored() {
        return cursorX >= 0.0 && cursorY >= 0.0;
    }

    public static void startTicking() {
        ticks = clearTicks.value();
    }

    public static void updateCursorPos(double x, double y) {
        cursorX = x;
        cursorY = y;
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (isActive(event.screen)) {
            startTicking();
        }
    }

    @EventHandler
    private static void onScreenClose(ScreenCloseEvent event) {
        if (instance.isActive()) {
            startTicking();
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && ticks > 0 && mc.screen == null) {
            ticks--;
            if (ticks == 0) {
                cursorX = -1.0;
                cursorY = -1.0;
            }
        }
    }
}
