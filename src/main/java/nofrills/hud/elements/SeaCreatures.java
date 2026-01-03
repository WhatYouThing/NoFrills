package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

public class SeaCreatures extends SimpleTextElement {
    public final SettingBool zero = new SettingBool(false, "zero", instance);
    private boolean active = false;

    public SeaCreatures(String text) {
        super(Text.literal(text), new Feature("seaCreaturesElement"), "Sea Creatures Element");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Hide If Zero", zero, "Hides the element if there are 0 sea creatures nearby.")
        ));
        this.setDesc("Displays the amount of nearby sea creatures. Used by the Cap Tracker feature.");
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud()) {
            if (!this.active && zero.value()) return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setCount(int count) {
        if (count > 0) {
            this.setText(Utils.format("Sea Creatures: §f{}", count));
            this.active = true;
        } else {
            this.setText("Sea Creatures: §70");
            this.active = false;
        }
    }
}
