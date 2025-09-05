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

public class Ping extends SimpleTextElement {
    public final Feature instance = new Feature("pingElement");

    public final SettingDouble x;
    public final SettingDouble y;
    public final SettingBool shadow = new SettingBool(true, "shadow", instance.key());
    public final SettingEnum<alignment> align = new SettingEnum<>(alignment.Left, alignment.class, "align", instance.key());
    public final SettingBool average = new SettingBool(false, "average", instance.key());

    private final Identifier identifier = Identifier.of("nofrills", "ping-element");

    public int ticks = 20;
    public List<Long> pingList = new ArrayList<>();

    public Ping(String text, double x, double y) {
        super(Text.literal(text));
        this.x = new SettingDouble(x, "x", instance.key());
        this.y = new SettingDouble(y, "y", instance.key());
        this.options = new HudSettings(List.of(
                new Settings.Toggle("Shadow", shadow, "Adds a shadow to the element's text."),
                new Settings.Dropdown<>("Alignment", align, "The alignment of the element's text."),
                new Settings.Toggle("Average", average, "Tracks and adds your average ping to the element.")
        ));
        this.options.setTitle(Text.of("Ping Element"));
    }

    public Ping(String text) {
        this(text, HudManager.getDefaultX(), HudManager.getDefaultY());
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (HudManager.isEditingHud()) {
            super.layout.surface(instance.isActive() ? Surface.BLANK : this.disabledSurface);
        } else if (!instance.isActive()) {
            return;
        }
        this.updateShadow(shadow);
        this.updateAlignment(align);
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setPing(long ping) {
        if (average.value()) {
            if (this.pingList.size() > 30) {
                this.pingList.removeFirst();
            }
            this.pingList.add(ping);
            long avg = 0;
            for (long previous : this.pingList) {
                avg += previous;
            }
            this.setText(Utils.format("§bPing: §f{}ms §7{}ms", ping, avg / pingList.size()));
        } else {
            this.setText(Utils.format("§bPing: §f{}ms", ping));
        }
    }

    public void reset() {
        this.ticks = 20;
        this.pingList.clear();
        this.setText("§bPing: §f0ms");
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
}
