package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingDouble;
import nofrills.config.SettingEnum;
import nofrills.hud.HudManager;
import nofrills.hud.HudSettings;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;


public class TPS extends SimpleTextElement {
    public final Feature instance = new Feature("tpsElement");

    public final SettingDouble x = new SettingDouble(0.01, "x", instance.key());
    public final SettingDouble y = new SettingDouble(0.06, "y", instance.key());
    public final SettingBool shadow = new SettingBool(true, "shadow", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());
    public final SettingBool average = new SettingBool(false, "average", instance.key());

    private final Identifier identifier = Identifier.of("nofrills", "tps-element");

    public int clientTicks = 20;
    public int serverTicks = 0;
    public List<Integer> tpsList = new ArrayList<>();

    public TPS(Text text) {
        super(text);
        this.options = new HudSettings(List.of(
                new Settings.Toggle("Shadow", shadow, "Adds a shadow to the element's text."),
                new Settings.Dropdown<>("Alignment", align, "The alignment of the element's text."),
                new Settings.Toggle("Average", average, "Tracks and adds the average TPS to the element.")
        ));
        this.options.setTitle(Text.of("TPS Element"));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.focusHandler() == null) {
            return;
        }
        if (HudManager.isEditingHud()) {
            super.layout.surface(instance.isActive() ? Surface.BLANK : this.disabledSurface);
        } else if (!instance.isActive()) {
            return;
        }
        this.updateShadow(shadow);
        this.updateAlignment(align);
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setTps(int tps) {
        if (average.value()) {
            if (this.tpsList.size() > 30) {
                this.tpsList.removeFirst();
            }
            this.tpsList.add(Math.clamp(tps, 0, 20));
            int avg = 0;
            for (int previous : this.tpsList) {
                avg += previous;
            }
            this.setText(Utils.format("§bTPS: §f{} §7{}", tps, Utils.formatDecimal(avg / (double) tpsList.size())));
        } else {
            this.setText(Utils.format("§bTPS: §f{}", tps));
        }
    }

    public void reset() {
        this.clientTicks = 20;
        this.serverTicks = 0;
        this.tpsList.clear();
        this.setText("§bTPS: §f0");
    }

    @Override
    public void updatePosition() {
        this.updatePosition(x, y);
    }

    @Override
    public void toggle() {
        instance.setActive(!instance.isActive());
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
}
