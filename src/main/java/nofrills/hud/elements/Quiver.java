package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public final class Quiver extends SimpleTextElement {
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

    public void update(ItemStack stack) {
        Item item = stack.getItem();
        if (item.equals(Items.ARROW) || item.equals(Items.FEATHER)) {
            for (Text text : Utils.getLoreText(stack)) {
                String line = Utils.toPlain(text);
                if (line.startsWith("Arrows Remaining: ")) {
                    String name = Utils.toPlain(stack.getName());
                    String quantity = line.substring(line.indexOf(":") + 2);
                    Style nameStyle = Utils.getStyle(stack.getName(), s -> s.trim().equals(name)).orElse(Style.EMPTY.withColor(Formatting.WHITE));
                    Style quantityStyle = Utils.getStyle(text, s -> s.trim().startsWith(quantity)).orElse(Style.EMPTY.withColor(Formatting.WHITE));
                    this.setText(Text.literal("Quiver: ")
                            .append(Text.literal(name.replace(" Arrow", "")).setStyle(nameStyle))
                            .append(Text.literal(" x" + quantity).setStyle(quantityStyle))
                    );
                    break;
                }
            }
        }
    }
}
