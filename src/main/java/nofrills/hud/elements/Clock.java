package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class Clock extends SimpleTextElement {
    public final SettingBool format24 = new SettingBool(false, "format24", instance.key());

    public Clock(String text) {
        super(Text.literal(text), new Feature("clockElement"), "Clock Display");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("24H Format", this.format24, "Use 24H format instead of 12H.")
        ));
        this.setDesc("Displays system clock.");
        this.setCategory(Category.Info);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            this.updateTime();
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    public void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = format24.value() ? DateTimeFormatter.ofPattern("HH:mm:ss") : DateTimeFormatter.ofPattern("hh:mm:ss a");
        this.setText(Utils.format("Time: §f{}", now.format(formatter)));
    }
}
