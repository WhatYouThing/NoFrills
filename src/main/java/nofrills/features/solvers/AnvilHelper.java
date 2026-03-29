package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.ScreenCloseEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ScreenRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.List;

public class AnvilHelper {
    public static final Feature instance = new Feature("anvilHelper");

    public static final SettingColor color = new SettingColor(RenderColor.green, "color", instance);

    private static boolean isInAnvil = false;

    private static String getEnchantInContainer(ChestMenu handler) {
        List<Slot> slots = Utils.getContainerSlots(handler);
        if (slots.stream().noneMatch(slot -> slot.getItem().getItem().equals(Items.BARRIER) && Utils.toPlain(slot.getItem().getHoverName()).equals("Anvil"))) {
            return "";
        }
        List<Slot> idSlots = slots.stream().filter(slot -> !Utils.getSkyblockId(slot.getItem()).isEmpty()).toList();
        if (idSlots.size() == 1) {
            ItemStack stack = idSlots.getFirst().getItem();
            if (stack.getItem().equals(Items.ENCHANTED_BOOK)) {
                return Utils.getMarketId(stack);
            }
        }
        return "";
    }

    @EventHandler
    private static void onScreenRender(ScreenRenderEvent.Before event) {
        if (instance.isActive() && isInAnvil && event.handler instanceof ChestMenu handler) {
            String enchantId = getEnchantInContainer(handler);
            if (!enchantId.isEmpty() && !enchantId.equals("ENCHANTMENT_UNKNOWN")) {
                for (Slot slot : Utils.getContainerSlots(handler, true)) {
                    ItemStack stack = slot.getItem();
                    Item item = stack.getItem();
                    if (item.equals(Items.ENCHANTED_BOOK) && Utils.getMarketId(stack).equals(enchantId)) {
                        event.drawFill(slot.index, color.value());
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onScreenOpen(ScreenOpenEvent event) {
        if (instance.isActive() && Utils.isInSkyblock() && event.screen.getTitle().getString().equals("Anvil")) {
            isInAnvil = true;
        }
    }

    @EventHandler
    private static void onScreenClose(ScreenCloseEvent event) {
        isInAnvil = false;
    }
}
