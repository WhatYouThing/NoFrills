package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static nofrills.Main.mc;

public class WardrobeKeybinds {
    public static final Feature instance = new Feature("wardrobeKeybinds");

    public static final SettingBool noUnequip = new SettingBool(false, "noUnequip", instance.key());
    public static final SettingBool sound = new SettingBool(false, "sound", instance.key());

    private static final List<Item> validButtons = List.of(
            Items.LIME_DYE,
            Items.PINK_DYE,
            Items.GRAY_DYE
    );

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && event.action == GLFW.GLFW_PRESS && mc.currentScreen instanceof GenericContainerScreen container) {
            if (event.key >= GLFW.GLFW_KEY_1 && event.key <= GLFW.GLFW_KEY_9) {
                String title = container.getTitle().getString();
                if (title.startsWith("Wardrobe (") && title.endsWith(")")) {
                    int page = Integer.parseInt(title.replace("Wardrobe (", "").split("/")[0]);
                    int target = event.key - 48 + (page - 1) * 9;
                    for (Slot slot : container.getScreenHandler().slots) {
                        String name = Formatting.strip(slot.getStack().getName().getString());
                        if (name.startsWith("Slot " + target + ": ")) {
                            Item item = slot.getStack().getItem();
                            if (noUnequip.value() && item.equals(Items.LIME_DYE)) {
                                continue;
                            }
                            for (Item valid : validButtons) {
                                if (item.equals(valid)) {
                                    mc.interactionManager.clickSlot(container.getScreenHandler().syncId, slot.id, 0, SlotActionType.PICKUP, mc.player);
                                    if (sound.value()) {
                                        Utils.playSound(SoundEvents.ENTITY_HORSE_ARMOR.value(), SoundCategory.MASTER, 0.65f, 1.0f);
                                    }
                                    event.cancel();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
