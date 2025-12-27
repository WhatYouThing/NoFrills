package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

public class FishingBobber extends SimpleTextElement {
    public final SettingBool inactive = new SettingBool(false, "inactive", instance.key());
    public final SettingBool timer = new SettingBool(false, "timer", instance.key());

    public Entity hologram = null;
    public int timerTicks = 0;
    public boolean active = false;
    public String currentText = "Bobber: §7Inactive";

    public FishingBobber(String text) {
        super(Text.literal(text), new Feature("bobberElement"), "Bobber Element");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Hide If Inactive", this.inactive, "Hides the element if your fishing bobber is inactive."),
                new Settings.Toggle("Bobber Timer", this.timer, "Tracks how long your fishing bobber has existed for, useful for Slugfish.")
        ));
        this.setDesc("Displays the fishing hologram timer, and optionally the existence time of your bobber.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud()) {
            if (!this.active && this.inactive.value()) return;
        }
        if (timer.value()) {
            this.setText(Utils.format("{} §7{}s", this.currentText, Utils.formatDecimal(this.timerTicks / 20.0, 1)));
        } else {
            this.setText(this.currentText);
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    private void updateText(String text) {
        this.currentText = text;
    }

    public void setActive() {
        this.updateText("Bobber: §aActive");
        this.active = true;
    }

    public void setInactive() {
        this.updateText("Bobber: §7Inactive");
        this.timerTicks = 0;
        this.active = false;
    }

    public void setTimer(String timer) {
        if (timer.equals("!!!")) {
            this.updateText("Bobber: §c§lReel!");
        } else {
            this.updateText(Utils.format("Bobber: §e§l{}", timer));
        }
        this.active = true;
    }
}
