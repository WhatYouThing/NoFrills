package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class Quiver extends SimpleTextElement {

    public Quiver(String text) {
        super(Text.literal(text), new Feature("quiverElement"), "Quiver Element");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the type and the amount of arrows in your quiver.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    public void update() {
        if (mc.player == null) return;
        ItemStack stack = mc.player.getInventory().getStack(8);
        Item item = stack.getItem();
        if (item.equals(Items.ARROW) || item.equals(Items.FEATHER)) {
            for (String line : Utils.getLoreLines(stack)) {
                if (line.startsWith("Active Arrow: ")) {
                    String info = line.substring(line.indexOf(":") + 2);
                    String arrowName = info.substring(0, info.indexOf("(") - 1).replace(" Arrow", "");
                    String arrowColor = switch (arrowName) {
                        case "Redstone-tipped", "Emerald-tipped" -> "§a";
                        case "Bouncy", "Icy", "Armorshred", "Explosive", "Glue", "Nansorb" -> "§9";
                        case "Magma" -> "§5";
                        default -> "§f";
                    };
                    String quantity = info.substring(info.indexOf("(") + 1, info.indexOf(")"));
                    this.setText(Utils.format("Quiver: {}{} §7(§e{}§7)", arrowColor, arrowName, quantity));
                }
            }
        }
    }
}
