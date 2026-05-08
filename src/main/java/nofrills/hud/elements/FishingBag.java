package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
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

public final class FishingBag extends SimpleTextElement {
    public final SettingBool onlyRod = new SettingBool(true, "onlyRod", this.instance);

    public FishingBag(String text) {
        super(Text.literal(text), new Feature("fishingBagElement"), "Fishing Bag Display");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Only If Rod", this.onlyRod, "Automatically hides the element if you are not holding a fishing rod.")
        ));
        this.setDesc("Displays the type and the amount of bait in your fishing bag.");
        this.setCategory(Category.Fishing);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && this.onlyRod.value() && !mc.player.isHolding(Items.FISHING_ROD)) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void update(ItemStack stack) {
        String name = Utils.toPlain(stack.getName());
        if (name.endsWith(" Bait")) {
            for (Text text : Utils.getLoreText(stack)) {
                String line = Utils.toPlain(text);
                if (line.startsWith("Bait Remaining: ")) {
                    String quantity = line.substring(line.indexOf(":") + 2);
                    Style nameStyle = Utils.getStyle(stack.getName(), s -> s.trim().equals(name)).orElse(Style.EMPTY.withColor(Formatting.WHITE));
                    Style quantityStyle = Utils.getStyle(text, s -> s.trim().startsWith(quantity)).orElse(Style.EMPTY.withColor(Formatting.WHITE));
                    this.setText(Text.literal("Bait: ")
                            .append(Text.literal(name.replace(" Bait", "")).setStyle(nameStyle))
                            .append(Text.literal(" x" + quantity).setStyle(quantityStyle))
                    );
                    break;
                }
            }
        }
    }
}
