package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        super(Component.literal(text), new Feature("quiverElement"), "Quiver Display");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Only If Bow", this.onlyBow, "Automatically hides the element if you are not holding a bow.")
        ));
        this.setDesc("Displays the type and the amount of arrows in your quiver.");
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
        ItemStack stack = mc.player.getInventory().getItem(8);
        Item item = stack.getItem();
        if (item.equals(Items.ARROW) || item.equals(Items.FEATHER)) {
            for (Component text : Utils.getLoreText(stack)) {
                String line = Utils.toPlain(text);
                if (line.startsWith("Active Arrow: ")) {
                    String info = line.substring(line.indexOf(":") + 2);
                    String name = info.substring(0, info.indexOf("(") - 1).replace(" Arrow", "");
                    String quantity = info.substring(info.indexOf("(") + 1, info.indexOf(")"));
                    Optional<Style> style = Utils.getStyle(text, str -> str.startsWith(name));
                    MutableComponent arrowName = Component.literal(name).setStyle(style.orElse(Style.EMPTY));
                    this.setText(Component.literal("Quiver: ").append(arrowName).append(Utils.format(" §7(§e{}§7)", quantity)));
                    break;
                }
            }
        }
    }
}
