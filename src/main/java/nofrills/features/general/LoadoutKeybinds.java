package nofrills.features.general;

import com.google.gson.JsonObject;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingJson;
import nofrills.config.SettingKeybind;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import nofrills.mixin.AbstractContainerScreenAccessor;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Pattern;

import static nofrills.Main.mc;

@EventListener
public class LoadoutKeybinds {
    public static final Feature instance = new Feature("loadoutKeybinds");

    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);
    public static final SettingKeybind editBindKey = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "editBindKey", instance);

    private static final Pattern loadoutsPattern = Pattern.compile("\\([0-9]*/[0-9]*\\) Loadouts");
    private static final Pattern loadoutButtonPattern = Pattern.compile("Loadout [0-9]*");
    private static String binding = "";

    private static boolean isLoadoutsMenu(String title) {
        return loadoutsPattern.matcher(title).matches();
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && mc.screen instanceof AbstractContainerScreen<?> screen && isLoadoutsMenu(screen.getTitle().getString())) {
            String key = String.valueOf(event.key);
            if (!binding.isEmpty()) {
                if (event.action == GLFW.GLFW_PRESS) {
                    data.edit(obj -> {
                        obj.entrySet().removeIf(entry -> entry.getValue().getAsString().equals(binding));
                        obj.addProperty(key, binding);
                    });
                    Utils.infoRaw(Component.literal("Successfully bound key to " + binding + ".").withStyle(ChatFormatting.GREEN));
                    binding = "";
                }
                event.cancel();
            } else if (editBindKey.isKey(event.key)) {
                Slot focused = ((AbstractContainerScreenAccessor) mc.screen).getHoveredSlot();
                if (focused == null) return;
                ItemStack stack = focused.getItem();
                String name = Utils.toPlain(stack.getHoverName());
                if (loadoutButtonPattern.matcher(name).matches()) {
                    if (event.action == GLFW.GLFW_PRESS) {
                        if (data.value().entrySet().stream().anyMatch(entry -> entry.getValue().getAsString().equals(name))) {
                            data.edit(obj -> obj.entrySet().removeIf(entry -> entry.getValue().getAsString().equals(name)));
                            Utils.infoRaw(Component.literal("Cleared bound key from " + name + ".").withStyle(ChatFormatting.GREEN));
                        } else {
                            Utils.infoRaw(Component.literal("Press any key to bind to " + name + ".").withStyle(ChatFormatting.GRAY));
                            binding = name;
                        }
                    }
                }
                event.cancel();
            } else if (data.value().has(key)) {
                String name = data.value().get(key).getAsString();
                for (Slot slot : Utils.getContainerSlots(screen.getMenu())) {
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    if (name.equals(Utils.toPlain(stack.getHoverName()))) {
                        if (event.action == GLFW.GLFW_PRESS) {
                            Utils.click(screen.getMenu().containerId, slot.index, GLFW.GLFW_MOUSE_BUTTON_LEFT, ContainerInput.PICKUP);
                        }
                        event.cancel();
                        break;
                    }
                }
            }
        }
    }
}
