package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import nofrills.config.Feature;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

@EventListener
public class QuickClose {
    public static final Feature instance = new Feature("quickClose");

    private static final List<KeyMapping> movementKeys = List.of(
            mc.options.keyUp,
            mc.options.keyLeft,
            mc.options.keyDown,
            mc.options.keyRight
    );

    private static boolean isChest(String title) {
        return Utils.isInDungeons() && (title.endsWith("Chest") || DungeonChestValue.isChest(title));
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.gui.screen() instanceof AbstractContainerScreen<?> container && isChest(container.getTitle().getString())) {
            if (movementKeys.stream().anyMatch(event::isKey)) {
                event.consume(container::onClose);
            }
        }
    }
}
