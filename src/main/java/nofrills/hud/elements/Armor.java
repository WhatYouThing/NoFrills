package nofrills.hud.elements;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
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
    private final List<ItemStack> defaultArmor = List.of(
            Items.LEATHER_HELMET.getDefaultStack(),
            Items.LEATHER_CHESTPLATE.getDefaultStack(),
            Items.LEATHER_LEGGINGS.getDefaultStack(),
            Items.LEATHER_BOOTS.getDefaultStack()
    );
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
        return switch (alignment) {
            case Horizontal -> Containers.horizontalFlow(Sizing.fixed(64), Sizing.fixed(16));
            case Vertical -> Containers.verticalFlow(Sizing.fixed(16), Sizing.fixed(64));
        };
    }

    public void updateArmor() {
        if (mc.player != null) {
            this.content.clearChildren();
            for (ItemStack armor : this.isEditingHud() ? this.defaultArmor : Utils.getEntityArmor(mc.player)) {
                this.content.child(Components.item(armor));
            }
        }
    }

    public enum Alignment {
        Horizontal,
        Vertical
    }
}
