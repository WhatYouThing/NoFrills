package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class PearlRefill {
    public static void getPearls() {
        int totalPearls = 0;
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i <= 35; i++) {
            ItemStack stack = inv.getStack(i);
            String itemName = Formatting.strip(stack.getName().getString());
            if (itemName.equals("Ender Pearl")) {
                totalPearls += stack.getCount();
            }
        }
        if (totalPearls < 16) {
            Utils.sendMessage("/gfs Ender Pearl " + (16 - totalPearls));
        }
    }


    @EventHandler
    public static void onKey(InputEvent event) {
        if (Utils.Keybinds.getPearls.matchesKey(event.key, 0) && event.action == GLFW.GLFW_PRESS) {
            if (mc.currentScreen == null) {
                getPearls();
                event.cancel();
            }
        }
    }
}
