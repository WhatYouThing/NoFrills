package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public class Quiver extends SimpleTextElement {
    public final SettingBool onlyBow = new SettingBool(true, "onlyBow", this.instance);

    public Quiver(String text) {
        super(Text.literal(text), new Feature("quiverElement"), "Quiver Element");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Only If Bow", this.onlyBow, "Automatically hides the element if you are not holding a bow.")
        ));
        this.setDesc("Displays the type and the amount of arrows in your quiver.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && this.onlyBow.value() && !mc.player.isHolding(Items.BOW)) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
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
                    break;
                }
            }
        }
    }
}
