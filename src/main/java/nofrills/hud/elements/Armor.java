package nofrills.hud.elements;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nofrills.config.Feature;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;
import nofrills.hud.HudElement;
import nofrills.hud.HudManager;
import nofrills.hud.HudSettings;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public class Armor extends HudElement {
    public final Feature instance = new Feature("armorElement");

    public final SettingDouble x;
    public final SettingDouble y;
    public final SettingEnum<Alignment> align = new SettingEnum<>(Alignment.Horizontal, Alignment.class, "align", instance.key());

    private final Identifier identifier = Identifier.of("nofrills", "armor-element");
    private FlowLayout content;
    private Alignment lastAlign;

    public Armor(double x, double y) {
        super(Containers.verticalFlow(Sizing.content(), Sizing.content()));
        this.content = this.getAlignment(align.value());
        this.lastAlign = align.value();
        this.layout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.layout.child(this.content);
        this.x = new SettingDouble(x, "x", instance.key());
        this.y = new SettingDouble(y, "y", instance.key());
        this.options = new HudSettings(List.of(
                new Settings.Dropdown<>("Alignment", align, "The alignment direction of the element.")
        ));
        this.options.setTitle(Text.of("Armor Element"));
    }

    public Armor() {
        this(HudManager.getDefaultX(), HudManager.getDefaultY());
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (HudManager.isEditingHud()) {
            super.layout.surface(instance.isActive() ? Surface.BLANK : this.disabledSurface);
        } else if (!instance.isActive()) {
            return;
        }
        if (!this.lastAlign.equals(align.value())) {
            this.layout.clearChildren();
            this.content = this.getAlignment(align.value());
            this.updateArmor();
            this.layout.child(this.content);
            this.lastAlign = align.value();
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    private FlowLayout getAlignment(Alignment alignment) {
        return switch (alignment) {
            case Horizontal -> Containers.horizontalFlow(Sizing.content(), Sizing.content());
            case Vertical -> Containers.verticalFlow(Sizing.content(), Sizing.content());
        };
    }

    public void updateArmor() {
        if (mc.player != null) {
            this.content.clearChildren();
            for (ItemStack armor : Utils.getEntityArmor(mc.player)) {
                this.content.child(Components.item(armor));
            }
        }
    }

    @Override
    public void toggle() {
        instance.setActive(!instance.isActive());
    }

    @Override
    public void updatePosition() {
        this.updatePosition(x, y);
    }

    @Override
    public void savePosition(double x, double y) {
        this.x.set(x);
        this.y.set(y);
    }

    @Override
    public Identifier getIdentifier() {
        return this.identifier;
    }

    public enum Alignment {
        Horizontal,
        Vertical
    }
}
