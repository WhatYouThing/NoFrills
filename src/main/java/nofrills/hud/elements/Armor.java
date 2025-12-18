package nofrills.hud.elements;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import nofrills.config.Feature;
import nofrills.config.SettingEnum;
import nofrills.hud.HudElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public class Armor extends HudElement {
    public final SettingEnum<Alignment> align = new SettingEnum<>(Alignment.Horizontal, Alignment.class, "align", instance.key());
    private FlowLayout content;
    private Alignment lastAlign;

    public Armor() {
        super(new Feature("armorElement"), "Armor Element");
        this.content = this.getAlignment(align.value());
        this.lastAlign = align.value();
        this.layout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.layout.child(this.content);
        this.options = this.getBaseSettings(List.of(
                new Settings.Dropdown<>("Alignment", align, "The alignment direction of the element.")
        ));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            if (!this.lastAlign.equals(align.value())) {
                this.layout.clearChildren();
                this.content = this.getAlignment(align.value());
                this.updateArmor();
                this.layout.child(this.content);
                this.lastAlign = align.value();
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    private FlowLayout getAlignment(Alignment alignment) {
        FlowLayout container = switch (alignment) {
            case Horizontal -> Containers.horizontalFlow(Sizing.fixed(72), Sizing.fixed(18));
            case Vertical -> Containers.verticalFlow(Sizing.fixed(18), Sizing.fixed(72));
        };
        for (int i = 0; i <= 3; i++) {
            ItemComponent component = Components.item(ItemStack.EMPTY);
            component.showOverlay(true).margins(Insets.of(1));
            container.child(component);
        }
        return container;
    }

    private List<ItemStack> getArmorItems() {
        if (this.isEditingHud()) {
            return List.of(
                    Items.LEATHER_HELMET.getDefaultStack(),
                    Items.LEATHER_CHESTPLATE.getDefaultStack(),
                    Items.LEATHER_LEGGINGS.getDefaultStack(),
                    Items.LEATHER_BOOTS.getDefaultStack()
            );
        }
        if (mc.player != null) {
            return Utils.getEntityArmor(mc.player);
        }
        return List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public void updateArmor() {
        List<ItemStack> armor = this.getArmorItems();
        List<Component> children = this.content.children();
        for (int i = 0; i <= 3; i++) {
            ((ItemComponent) children.get(i)).stack(armor.get(i));
        }
    }

    public enum Alignment {
        Horizontal,
        Vertical
    }
}
