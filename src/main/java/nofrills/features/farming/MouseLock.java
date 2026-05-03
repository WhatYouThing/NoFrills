package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

public class MouseLock {
    public static final Feature instance = new Feature("mouseLock");

    public static SettingKeybind keybind = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "keybind", instance);

    public static boolean locked = false;

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && keybind.isKey(event.key) && Utils.isInGarden()) {
            if (event.action == GLFW.GLFW_PRESS) {
                locked = !locked;
                Utils.info(locked ? "§aMouse lock activated." : "§cMouse lock deactivated.");
            }
            event.cancel();
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        locked = false;
    }
}
