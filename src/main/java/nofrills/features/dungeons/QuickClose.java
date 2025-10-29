package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import nofrills.config.Feature;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class QuickClose {
    public static final Feature instance = new Feature("quickClose");

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && mc.currentScreen instanceof GenericContainerScreen container) {
            if (event.key >= GLFW.GLFW_MOUSE_BUTTON_1 && event.key <= GLFW.GLFW_MOUSE_BUTTON_8) {
                return;
            }
            if (container.getTitle().getString().endsWith("Chest")) {
                if (event.action == GLFW.GLFW_PRESS) {
                    container.close();
                }
                event.cancel();
            }
        }
    }
}
