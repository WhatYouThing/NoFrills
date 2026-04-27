package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.SlayerUtil;

public class SlayerTimer extends SimpleTextElement {
    private boolean visible = false;

    public SlayerTimer() {
        super(Component.literal("Slayer Timer"), new Feature("slayerTimerElement"), "Slayer Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the timer hologram of your slayer boss.");
        this.setCategory(Category.Slayer);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && !this.visible) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void update() {
        ArmorStand entity = SlayerUtil.getTimerEntity();
        if (entity == null || entity.getCustomName() == null) {
            if (this.visible) {
                this.setText(this.defaultText);
                this.visible = false;
            }
            return;
        }
        this.label.text(entity.getCustomName());
        this.visible = true;
    }
}
