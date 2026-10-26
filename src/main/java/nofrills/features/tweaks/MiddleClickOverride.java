package nofrills.features.tweaks;

import com.google.gson.JsonElement;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.config.*;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

public class MiddleClickOverride {
    public static final Feature instance = new Feature("middleClickOverride", APIFlags.UseMiddleClickListsAPI);

    public static final SettingBool debug = new SettingBool(false, "debug", instance);

    private static final DataFile lists = Config.getDataFile("MiddleClickOverrideCache.json");

    private static boolean isLeftClick(int button, ContainerInput actionType) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT && actionType.equals(ContainerInput.PICKUP);
    }

    private static boolean isOnList(String title, String list) {
        if (lists.get().has(list)) {
            for (JsonElement element : lists.get().get(list).getAsJsonArray()) {
                try {
                    if (Utils.pattern(element.getAsString()).matcher(title).matches()) {
                        return true;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to compile Middle Click Override pattern: {}, {}", element.getAsString(), list, e);
                }
            }
        }
        return false;
    }

    private static boolean isBlacklisted(String title) {
        return isOnList(title, "blacklist");
    }

    private static boolean isWhitelisted(String title) {
        return isOnList(title, "whitelist");
    }

    private static boolean isTransaction(ItemStack stack) {
        return Utils.getLoreLines(stack).stream().anyMatch(line -> line.equals("Cost") || line.equals("Sell Price") || line.equals("Bazaar Price"));
    }

    private static boolean isInLoadoutEdit(ContainerScreen screen) {
        return screen.getMenu().slots.stream()
                .map(Slot::getItem)
                .filter(stack -> !stack.isEmpty())
                .anyMatch(stack -> stack.getHoverName().getString().equals("Rename Loadout"));
    }

    public static boolean shouldOverride(Slot slot, int button, ContainerInput actionType) {
        if (instance.isActive() && mc.screen instanceof ContainerScreen container && slot != null && isLeftClick(button, actionType)) {
            String title = container.getTitle().getString();
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || isBlacklisted(title) || !Utils.isInSkyblock() || isInLoadoutEdit(container)) {
                return false;
            }
            if (Utils.getSkyblockId(stack).isEmpty() || isWhitelisted(title) || isTransaction(stack)) {
                if (debug.value()) {
                    Utils.infoFormat("Middle Click Override replaced click: slot {}, button {}, {} action type.", slot.index, button, actionType.name());
                }
                return true;
            }
        }
        return false;
    }

    public static DataFile getLists() {
        return lists;
    }
}
