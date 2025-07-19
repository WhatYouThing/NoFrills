package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.InputEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class SpaceFarmer {
    public static final Feature instance = new Feature("spaceFarmer");

    public static boolean spaceHeld = false;

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && event.key == GLFW.GLFW_KEY_SPACE) {
            if (mc.currentScreen != null && spaceHeld) {
                spaceHeld = false;
                mc.options.attackKey.setPressed(false);
                return;
            }
            if (event.action == GLFW.GLFW_PRESS && mc.options.sneakKey.isPressed() && Utils.isOnGardenPlot()) {
                spaceHeld = true;
                mc.options.attackKey.setPressed(true);
                event.cancel();
            } else if (event.action == GLFW.GLFW_RELEASE && spaceHeld) {
                spaceHeld = false;
                if (mc.options.attackKey.isPressed()) {
                    mc.options.attackKey.setPressed(false);
                }
                event.cancel();
            } else if (spaceHeld) {
                event.cancel();
            }
        }
    }

    @EventHandler
    public static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive() && spaceHeld) {
            spaceHeld = false;
            mc.options.attackKey.setPressed(false);
        }
    }
}
