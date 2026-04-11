package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.SlayerUtil;

public class SlayerHealth extends SimpleTextElement {
    private boolean visible = false;

    public SlayerHealth() {
        super(Text.literal("Slayer Health"), new Feature("slayerHealthElement"), "Slayer Health");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the name and health hologram of your slayer boss.");
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
        ArmorStandEntity entity = SlayerUtil.getNameEntity();
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
