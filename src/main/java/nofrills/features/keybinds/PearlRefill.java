package nofrills.features.keybinds;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class PearlRefill {
    public static final KeyBinding bind = new KeyBinding("key.nofrills.refillPearls", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.nofrills");

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
        if (bind.matchesKey(event.key, 0) && event.action == GLFW.GLFW_PRESS && mc.currentScreen == null) {
            getPearls();
            event.cancel();
        }
    }
}
