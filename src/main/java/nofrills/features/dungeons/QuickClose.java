package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.option.KeyBinding;
import nofrills.config.Feature;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static nofrills.Main.mc;

public class QuickClose {
    public static final Feature instance = new Feature("quickClose");

    private static final List<KeyBinding> movementKeys = List.of(
            mc.options.forwardKey,
            mc.options.leftKey,
            mc.options.backKey,
            mc.options.rightKey
    );

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.isKeyboard && mc.currentScreen instanceof GenericContainerScreen container) {
            if (container.getTitle().getString().endsWith("Chest") && movementKeys.stream().anyMatch(key -> key.matchesKey(event.keyInput))) {
                if (event.action == GLFW.GLFW_PRESS) {
                    container.close();
                }
                event.cancel();
            }
        }
    }
}
