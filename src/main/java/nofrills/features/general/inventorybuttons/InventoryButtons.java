package nofrills.features.general.inventorybuttons;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import nofrills.events.ServerJoinEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static nofrills.Main.mc;

@EventListener
public class InventoryButtons {
    public static final Feature instance = new Feature("inventoryButtons");

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);
    public static final SettingKeybind addButtonKey = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "addButtonKey", instance);
    public static final SettingKeybind copyTexturesKey = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "copyTexturesKey", instance);

    private static final ConcurrentHashMap<String, ResolvableProfile> profileCache = new ConcurrentHashMap<>();

    public static ResolvableProfile getOrInitTextures(String payload) {
        if (!profileCache.containsKey(payload)) {
            profileCache.put(payload, Utils.toResolvableProfile(payload));
        }
        return profileCache.get(payload);
    }

    private static JsonObject fillDefaults(JsonObject object) {
        List<Pair<String, JsonPrimitive>> values = List.of(
                Pair.of("x", new JsonPrimitive((mc.mouseHandler.getScaledXPos(mc.getWindow()) - 10) / mc.getWindow().getGuiScaledWidth())),
                Pair.of("y", new JsonPrimitive((mc.mouseHandler.getScaledYPos(mc.getWindow()) - 10) / mc.getWindow().getGuiScaledHeight())),
                Pair.of("uniform", new JsonPrimitive(true)),
                Pair.of("scaleX", new JsonPrimitive(1.0)),
                Pair.of("scaleY", new JsonPrimitive(1.0)),
                Pair.of("command", new JsonPrimitive("")),
                Pair.of("tooltip", new JsonPrimitive("Inventory Button")),
                Pair.of("model", new JsonPrimitive("")),
                Pair.of("textures", new JsonPrimitive("")),
                Pair.of("glint", new JsonPrimitive(false)),
                Pair.of("style", new JsonPrimitive(InventoryButtonStyle.Vanilla.name())),
                Pair.of("colorBackground", new JsonPrimitive(RenderColor.GRAY.withAlpha(0.33f).getArgb())),
                Pair.of("colorBorder", new JsonPrimitive(RenderColor.NF_BLUE.getArgb())),
                Pair.of("colorBorderHover", new JsonPrimitive(RenderColor.WHITE.getArgb()))
        );
        for (Pair<String, JsonPrimitive> value : values) {
            if (!object.has(value.getKey())) {
                object.add(value.getKey(), value.getValue());
            }
        }
        return object;
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.screen instanceof AbstractContainerScreen<?> container) {
            if (addButtonKey.isKey(event.key)) {
                if (event.action == GLFW.GLFW_PRESS) {
                    data.edit(object -> {
                        if (!object.has("buttons")) {
                            object.add("buttons", new JsonArray());
                        }
                        JsonObject obj = fillDefaults(new JsonObject());
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
                container.addRenderableWidget(InventoryButtonWidget.of(fillDefaults(element.getAsJsonObject())));
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (instance.isActive() && data.value().has("buttons")) {
            JsonArray buttons = data.value().get("buttons").getAsJsonArray();
            profileCache.entrySet().removeIf(entry -> {
                for (JsonElement button : buttons) {
                    if (button.getAsJsonObject().get("textures").getAsString().equals(entry.getKey())) {
                        return false;
                    }
                }
                return true;
            });
        }
    }
}