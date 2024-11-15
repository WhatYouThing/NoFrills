package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Config;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class WardrobeHotkeys {
    private static final Item[] validButtons = {
            Items.LIME_DYE,
            Items.PINK_DYE,
            Items.GRAY_DYE
    };

    @EventHandler
    public static void onKey(InputEvent event) {
        if (Config.wardrobeHotkeys && event.action == GLFW.GLFW_PRESS) {
            if (event.key >= GLFW.GLFW_KEY_1 && event.key <= GLFW.GLFW_KEY_9) {
                if (mc.currentScreen instanceof GenericContainerScreen container) {
                    String containerTitle = container.getTitle().getString();
                    if (containerTitle.startsWith("Wardrobe (") && containerTitle.endsWith(")")) {
                        int wardrobePage = Integer.parseInt(containerTitle.replace("Wardrobe (", "").split("/")[0]);
                        int targetSlot = event.key - 48 + (wardrobePage - 1) * 9;
                        for (Slot slot : container.getScreenHandler().slots) {
                            String itemName = slot.getStack().getName().getString();
                            if (itemName.startsWith("Slot " + targetSlot + ": ")) {
                                for (Item slotItem : validButtons) {
                                    if (slot.getStack().getItem() == slotItem) {
                                        mc.interactionManager.clickSlot(container.getScreenHandler().syncId, slot.id, 0, SlotActionType.PICKUP, mc.player);
                                        if (Config.wardrobeHotkeysSound) {
                                            Utils.playSound(SoundEvents.ENTITY_HORSE_ARMOR, SoundCategory.MASTER, 0.65f, 1.0f);
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
}
