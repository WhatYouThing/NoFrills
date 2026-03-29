package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import nofrills.config.Feature;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static nofrills.Main.mc;

public class QuickClose {
    public static final Feature instance = new Feature("quickClose");

    private static final List<KeyMapping> movementKeys = List.of(
            mc.options.keyUp,
            mc.options.keyLeft,
            mc.options.keyDown,
            mc.options.keyRight
    );

    private static boolean isChest(String title) {
        return title.endsWith("Chest") || DungeonChestValue.isChest(title);
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.isKeyboard && mc.screen instanceof ContainerScreen container) {
            if (isChest(container.getTitle().getString()) && movementKeys.stream().anyMatch(key -> Utils.matchesKey(key, event.keyInput))) {
                if (event.action == GLFW.GLFW_PRESS) {
                    container.onClose();
                }
                event.cancel();
            }
        }
    }
}
