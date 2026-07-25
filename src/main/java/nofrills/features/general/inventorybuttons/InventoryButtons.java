package nofrills.features.general.inventorybuttons;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import nofrills.config.Feature;
import nofrills.config.SettingJson;
import nofrills.config.SettingKeybind;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.events.ScreenOpenEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

@EventListener
public class InventoryButtons {
    public static final Feature instance = new Feature("inventoryButtons");

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);
    public static final SettingKeybind addButtonKey = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "addButtonKey", instance);
    public static final SettingKeybind copyTexturesKey = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "copyTexturesKey", instance);

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.screen instanceof AbstractContainerScreen<?> container) {
            if (addButtonKey.isKey(event.key)) {
                if (event.action == GLFW.GLFW_PRESS) {
                    data.edit(object -> {
                        if (!object.has("buttons")) {
                            object.add("buttons", new JsonArray());
                        }
                        JsonObject obj = new JsonObject();
                        obj.addProperty("x", (mc.mouseHandler.getScaledXPos(mc.getWindow()) - 10) / mc.getWindow().getGuiScaledWidth());
                        obj.addProperty("y", (mc.mouseHandler.getScaledYPos(mc.getWindow()) - 10) / mc.getWindow().getGuiScaledHeight());
                        obj.addProperty("keepSquare", true);
                        obj.addProperty("scaleX", 1.0);
                        obj.addProperty("scaleY", 1.0);
                        obj.addProperty("command", "");
                        obj.addProperty("tooltip", "Inventory button");
                        obj.addProperty("model", "lime_concrete");
                        obj.addProperty("textures", "");
                        obj.addProperty("glint", false);
                        object.get("buttons").getAsJsonArray().add(obj);
                        container.addRenderableWidget(InventoryButtonWidget.of(obj));
                    });
                }
                event.cancel();
            } else if (copyTexturesKey.isKey(event.key)) {
                Slot focused = Utils.getFocusedSlot();
                if (focused == null) return;
                if (event.action == GLFW.GLFW_PRESS) {
                    ItemStack stack = focused.getItem();
                    ResolvableProfile profile = stack.get(DataComponents.PROFILE);
                    if (profile != null) {
                        Utils.getTexturePayload(profile.partialProfile()).ifPresent(payload -> {
                            mc.keyboardHandler.setClipboard(payload);
                            Utils.infoRaw(Component.literal("Copied head textures payload from item: ")
                                    .withStyle(ChatFormatting.GREEN)
                                    .append(stack.getHoverName())
                            );
                        });
                    }
                }
                event.cancel();
            }
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        if (instance.isActive() && event.screen instanceof AbstractContainerScreen<?> container && data.value().has("buttons")) {
            for (JsonElement element : data.value().get("buttons").getAsJsonArray()) {
                container.addRenderableWidget(InventoryButtonWidget.of(element.getAsJsonObject()));
            }
        }
    }
}
