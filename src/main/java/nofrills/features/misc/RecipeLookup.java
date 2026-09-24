package nofrills.features.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingEnum;
import nofrills.config.SettingKeybind;
import nofrills.events.EventListener;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

import static nofrills.Main.mc;

@EventListener
public class RecipeLookup {
    public static final Feature instance = new Feature("recipeLookup");

    public static final SettingEnum<Mode> mode = new SettingEnum<>(Mode.Recipe, Mode.class, "mode", instance);
    public static final SettingKeybind keybind = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "bind", instance.key());

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && event.isKey(keybind) && mc.screen instanceof AbstractContainerScreen<?> container) {
            Slot focused = Utils.getFocusedSlot();
            if (focused == null) return;
            ItemStack stack = focused.getItem();
            String itemId = Utils.getSkyblockId(stack);
            if (!itemId.isEmpty()) {
                event.consume(() -> {
                    if (itemId.equals("PET")) {
                        CompoundTag data = Utils.getCustomData(stack);
                        if (!data.contains("petInfo")) return;
                        JsonObject petData = JsonParser.parseString(data.getString("petInfo").orElse("")).getAsJsonObject();
                        Utils.sendMessage("/recipe " + Utils.uppercaseFirst(Utils.toLower(petData.get("type").getAsString()), true));
                    } else {
                        String command = mode.value().equals(Mode.Viewrecipe) ? "/viewrecipe" : "/recipe";
                        Utils.sendMessage(command + " " + itemId);
                    }
                });
            } else if (container.getTitle().getString().contains("Museum")) {
                event.consume(() -> {
                    String entryName = Utils.toPlain(stack.getHoverName());
                    if (entryName.endsWith("Armor") || entryName.endsWith("Set") || entryName.endsWith("Equipment")) {
                        String[] words = entryName.split(" ");
                        entryName = String.join(" ", Arrays.copyOf(words, words.length - 1));
                    }
                    Utils.sendMessage("/recipe " + entryName);
                });
            } else if (stack.getHoverName().getString().equals("Accept Offer")) {
                // will only give recipe of the first item (in case of multi-item requests like Bartender)
                // might be worth revisiting later with a stateful approach
                event.consume(() -> {
                    String target = Utils.getLoreLines(stack).stream()
                            .dropWhile(s -> !s.contains("Items Required:"))
                            .skip(1)
                            .findFirst()
                            .orElse("");
                    if (!target.isEmpty()) {
                        int index = target.lastIndexOf(" x");
                        target = index > 0 ? target.substring(0, index) : target;
                        Utils.sendMessage("/recipe " + target);
                    }
                });
            }
        }
    }

    public enum Mode {
        Recipe,
        Viewrecipe
    }
}
