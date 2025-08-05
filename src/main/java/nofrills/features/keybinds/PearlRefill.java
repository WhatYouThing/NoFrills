package nofrills.features.keybinds;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class PearlRefill {
    public static final Feature instance = new Feature("pearlRefill");

    public static final SettingKeybind keybind = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "bind", instance.key());

    public static void getPearls() {
        int totalPearls = 0;
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i <= 35; i++) {
            ItemStack stack = inv.getStack(i);
            if (Utils.getSkyblockId(stack).equals("ENDER_PEARL")) {
                totalPearls += stack.getCount();
            }
        }
        if (totalPearls < 16) {
            Utils.sendMessage("/gfs Ender Pearl " + (16 - totalPearls));
        }
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (instance.isActive() && keybind.value() == event.key && event.action == GLFW.GLFW_PRESS && mc.currentScreen == null) {
            getPearls();
            event.cancel();
        }
    }
}
