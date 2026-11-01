package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import nofrills.config.Feature;
import nofrills.config.SettingInt;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

@EventListener
public class HotbarSwap {
    public static final Feature instance = new Feature("hotbarSwap");

    public static final SettingInt override = new SettingInt(8, "override", instance.key());

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && event.isLeftClick() && event.mouseInput.hasControlDownWithQuirk() && mc.gui.screen() instanceof InventoryScreen screen) {
            Slot focusedSlot = Utils.getFocusedSlot();
            if (focusedSlot != null && !focusedSlot.getItem().isEmpty()) {
                int focusedSlotId = focusedSlot.getContainerSlot();
                if (focusedSlotId >= 9 && focusedSlotId <= 35) {
                    event.consume(() -> {
                        int button = focusedSlotId % 9;
                        if (button == 8) {
                            button = override.value() - 1;
                        }
                        Utils.click(screen.getMenu().containerId, focusedSlotId, button, ContainerInput.SWAP);
                    });
                }
            }
        }
    }
}
