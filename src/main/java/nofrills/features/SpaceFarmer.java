package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Config;
import nofrills.events.InputEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class SpaceFarmer {
    public static boolean spaceHeld = false;

    @EventHandler
    public static void onKey(InputEvent event) {
        if (Config.spaceFarmer && event.key == GLFW.GLFW_KEY_SPACE && Utils.isOnGardenPlot()) {
            if (mc.currentScreen != null) {
                spaceHeld = false;
                mc.options.attackKey.setPressed(false);
                return;
            }
            if (event.action == GLFW.GLFW_PRESS && mc.options.sneakKey.isPressed()) {
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
    public static void onTick(WorldTickEvent event) {
        if (mc.currentScreen != null) {
            spaceHeld = false;
            mc.options.attackKey.setPressed(false);
        }
    }
}
