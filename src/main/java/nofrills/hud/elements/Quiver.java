package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;
import java.util.Optional;

import static nofrills.Main.mc;

public class Quiver extends SimpleTextElement {
    public final SettingBool onlyBow = new SettingBool(true, "onlyBow", this.instance);

    public Quiver(String text) {
        super(Text.literal(text), new Feature("quiverElement"), "Quiver Display");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Only If Bow", this.onlyBow, "Automatically hides the element if you are not holding a bow.")
        ));
        this.setDesc("Displays the type and the amount of arrows in your quiver.");
        this.setCategory(Category.Info);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
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
            for (Text text : Utils.getLoreText(stack)) {
                String line = Utils.toPlain(text);
                if (line.startsWith("Active Arrow: ")) {
                    String info = line.substring(line.indexOf(":") + 2);
                    String name = info.substring(0, info.indexOf("(") - 1).replace(" Arrow", "");
                    String quantity = info.substring(info.indexOf("(") + 1, info.indexOf(")"));
                    Optional<Style> style = Utils.getStyle(text, str -> str.startsWith(name));
                    MutableText arrowName = Text.literal(name).setStyle(style.orElse(Style.EMPTY));
                    this.setText(Text.literal("Quiver: ").append(arrowName).append(Utils.format(" §7(§e{}§7)", quantity)));
                    break;
                }
            }
        }
    }
}
