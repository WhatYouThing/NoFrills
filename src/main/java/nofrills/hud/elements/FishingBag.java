package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.InventoryUpdateEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public final class FishingBag extends SimpleTextElement implements ListeningHudElement {
    public final SettingBool onlyRod = new SettingBool(true, "onlyRod", this.instance);

    public FishingBag() {
        super(Component.literal("Bait: §fN/A"), new Feature("fishingBagElement"), "Fishing Bag Display");
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

    @Override
    public void onInventoryUpdate(InventoryUpdateEvent event) {
        if (event.slotId != 44 && event.slotId != 9) return;
        ItemStack stack = event.stack;
        String name = Utils.toPlain(stack.getHoverName());
        if (name.endsWith(" Bait")) {
            for (Component text : Utils.getLoreText(stack)) {
                String line = Utils.toPlain(text);
                if (line.startsWith("Bait Remaining: ")) {
                    String quantity = line.substring(line.indexOf(":") + 2);
                    Style color = Utils.getStyle(stack.getHoverName(), s -> s.trim().equals(name)).orElse(Style.EMPTY.withColor(ChatFormatting.WHITE));
                    double percentage = Utils.parseInt(quantity.replaceAll(",", "")).orElse(0) / 2880.0; // max capacity, 5 * 9 * 64
                    this.setText(Component.literal("Bait: ")
                            .append(Component.literal(name.replace(" Bait", "")).setStyle(color))
                            .append(Component.literal(" x" + quantity).withColor(Utils.getPercentageColor(percentage, true).getHex()))
                    );
                    break;
                }
            }
        }
    }
}
