package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import nofrills.config.*;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.events.ScreenRenderEvent;
import nofrills.events.SlotClickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static nofrills.Main.mc;

@EventListener
public class SlotLocking {
    public static final Feature instance = new Feature("slotLocking");

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);
    public static final SettingKeybind keybind = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "keybind", instance);
    public static final SettingBool overlay = new SettingBool(true, "overlay", instance);
    public static final SettingColor color = new SettingColor(RenderColor.fromFormat(ChatFormatting.YELLOW).withAlpha(0.33f), "color", instance);

    private static List<Slot> getInventorySlots(AbstractContainerMenu menu) {
        return menu.slots.stream().filter(slot -> slot.container instanceof Inventory).toList();
    }

    private static int getFirstSlotOffset(List<Slot> inventorySlots) {
        return inventorySlots.size() == 36 ? 4 : 0;
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.screen instanceof AbstractContainerScreen<?> container && keybind.isKey(event.key)) {
            Slot focused = Utils.getFocusedSlot();
            List<Slot> inventorySlots = getInventorySlots(container.getMenu());
            if (focused == null || !inventorySlots.contains(focused)) return;
            int slotIndex = inventorySlots.indexOf(focused) + getFirstSlotOffset(inventorySlots);
            if (event.action == GLFW.GLFW_PRESS) {
                data.edit(obj -> {
                    if (!obj.has("slots")) {
                        obj.add("slots", new JsonArray());
                    }
                    if (obj.get("slots").getAsJsonArray().remove(new JsonPrimitive(slotIndex))) {
                        Utils.infoRaw(Component.literal("Slot unlocked.").withStyle(ChatFormatting.YELLOW));
                        Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 0.0f);
                    } else {
                        obj.get("slots").getAsJsonArray().add(slotIndex);
                        Utils.infoRaw(Component.literal("Slot locked.").withStyle(ChatFormatting.GREEN));
                        Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    }
                });
            }
            event.cancel();
        }
    }

    @EventHandler
    private static void onRender(ScreenRenderEvent.After event) {
        if (instance.isActive() && overlay.value() && mc.screen instanceof AbstractContainerScreen<?>) {
            if (!data.value().has("slots")) return;
            List<Slot> inventorySlots = getInventorySlots(event.handler);
            int offset = getFirstSlotOffset(inventorySlots);
            JsonArray array = data.value().get("slots").getAsJsonArray();
            for (int i = 0; i < inventorySlots.size(); i++) {
                if (array.contains(new JsonPrimitive(i + offset))) {
                    event.drawFill(inventorySlots.get(i).index, color.value());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onClickSlot(SlotClickEvent event) {
        if (instance.isActive() && mc.screen instanceof AbstractContainerScreen<?>) {
            if (!data.value().has("slots")) return;
            List<Slot> inventorySlots = getInventorySlots(event.handler);
            if (event.slot == null || event.slot.getItem().isEmpty() || !inventorySlots.contains(event.slot)) return;
            int offset = getFirstSlotOffset(inventorySlots);
            int slotIndex = inventorySlots.indexOf(event.slot) + offset;
            if (data.value().get("slots").getAsJsonArray().contains(new JsonPrimitive(slotIndex))) {
                event.cancel();
            }
        }
    }
}
