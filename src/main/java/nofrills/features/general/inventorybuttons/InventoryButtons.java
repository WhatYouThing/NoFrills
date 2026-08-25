package nofrills.features.general.inventorybuttons;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import nofrills.config.Feature;
import nofrills.config.SettingInt;
import nofrills.config.SettingJson;
import nofrills.config.SettingKeybind;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static nofrills.Main.mc;

@EventListener
public class InventoryButtons {
    public static final Feature instance = new Feature("inventoryButtons");

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);
    public static final SettingKeybind manageKey = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "addButtonKey", instance);
    public static final SettingInt gridPrecision = new SettingInt(5, "gridPrecision", instance);

    private static final ConcurrentHashMap<String, ResolvableProfile> profileCache = new ConcurrentHashMap<>();

    public static ResolvableProfile getOrInitTextures(String payload) {
        if (!profileCache.containsKey(payload)) {
            profileCache.put(payload, Utils.toResolvableProfile(payload));
        }
        return profileCache.get(payload);
    }

    public static void addWidgets(AbstractContainerScreen<?> container) {
        if (!data.value().has("buttons") || container instanceof CreativeModeInventoryScreen) {
            return;
        }
        for (JsonElement element : data.value().get("buttons").getAsJsonArray()) {
            JsonObject button = fillDefaults(element.getAsJsonObject());
            if (button.get("inventoryOnly").getAsBoolean() && !(container instanceof InventoryScreen)) {
                continue;
            }
            container.addRenderableWidget(InventoryButtonWidget.of(button, container));
        }
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
                Pair.of("itemId", new JsonPrimitive("")),
                Pair.of("customModel", new JsonPrimitive("")),
                Pair.of("textures", new JsonPrimitive("")),
                Pair.of("glint", new JsonPrimitive(false)),
                Pair.of("inventoryOnly", new JsonPrimitive(false)),
                Pair.of("snapPosition", new JsonPrimitive(true)),
                Pair.of("style", new JsonPrimitive(InventoryButtonStyle.Vanilla.name())),
                Pair.of("colorBackground", new JsonPrimitive(RenderColor.NF_BLUE.withAlpha(0.25f).getArgb())),
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
        if (instance.isActive() && mc.screen instanceof AbstractContainerScreen<?> container && manageKey.isKey(event.key)) {
            Optional<InventoryButtonWidget> hoveredWidget = container.renderables
                    .stream()
                    .filter(renderable -> renderable instanceof InventoryButtonWidget button && button.isHovered())
                    .map(renderable -> (InventoryButtonWidget) renderable)
                    .findFirst();
            Slot hoveredSlot = Utils.getFocusedSlot();
            if (event.action == GLFW.GLFW_PRESS) {
                if (hoveredWidget.isPresent()) {
                    InventoryButtonWidget widget = hoveredWidget.get();
                    if (widget.unlockPosition) {
                        Utils.infoRaw(Component.literal("Button positioning locked.").withStyle(ChatFormatting.YELLOW));
                        Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 0.0f);
                    } else {
                        Utils.infoRaw(Component.literal("Button positioning unlocked.").withStyle(ChatFormatting.GREEN)
                                .append(Component.literal("\n- Left Click: Drag button.").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal("\n- Shift + Left Click: Drag button and snap to grid.").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal("\n- Alt + Left Click: Resize button.").withStyle(ChatFormatting.GRAY))
                        );
                        Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    }
                    widget.unlockPosition = !widget.unlockPosition;
                } else if (hoveredSlot != null) {
                    ItemStack stack = hoveredSlot.getItem();
                    if (!stack.isEmpty()) {
                        String id = Utils.getSkyblockId(stack);
                        Identifier model = stack.get(DataComponents.ITEM_MODEL);
                        Optional<String> payload = Utils.getTexturePayload(stack);
                        List<Pair<String, String>> buttons = new ArrayList<>();
                        if (!id.isEmpty()) buttons.add(Pair.of("[SKYBLOCK ID]", id));
                        if (model != null) buttons.add(Pair.of("[ITEM MODEL]", model.toString()));
                        payload.ifPresent(string -> buttons.add(Pair.of("[HEAD TEXTURES]", string)));
                        MutableComponent msg = Component.literal("Copy options for ").withStyle(ChatFormatting.GREEN)
                                .append(stack.getHoverName())
                                .append(Component.literal(": ").withStyle(ChatFormatting.GREEN));
                        if (buttons.isEmpty()) {
                            msg.append(Component.literal("None").withStyle(ChatFormatting.GRAY));
                        } else {
                            for (Pair<String, String> button : buttons) {
                                msg.append(Component.literal(" " + button.getKey()).withStyle(s -> s
                                        .withClickEvent(new ClickEvent.CopyToClipboard(button.getValue()))
                                        .applyFormats(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                                ));
                            }
                        }
                        Utils.infoRaw(msg);
                    }
                } else {
                    data.edit(object -> {
                        if (!object.has("buttons")) {
                            object.add("buttons", new JsonArray());
                        }
                        JsonObject obj = fillDefaults(new JsonObject());
                        object.get("buttons").getAsJsonArray().add(obj);
                        container.addRenderableWidget(InventoryButtonWidget.of(obj, container));
                    });
                    Utils.infoRaw(Component.literal("Created new inventory button.").withStyle(ChatFormatting.GREEN));
                    Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }
            }
            event.cancel();
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