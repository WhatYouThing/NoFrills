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

    private static boolean isChest(String title) {
        return title.endsWith("Chest") || DungeonChestValue.isChest(title);
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.isKeyboard && mc.currentScreen instanceof GenericContainerScreen container) {
            if (isChest(container.getTitle().getString()) && movementKeys.stream().anyMatch(key -> Utils.matchesKey(key, event.keyInput))) {
                if (event.action == GLFW.GLFW_PRESS) {
                    container.close();
                }
                event.cancel();
            }
        }
    }
}
