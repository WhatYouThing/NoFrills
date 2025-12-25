package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;

public class BossHealth extends SimpleTextElement {
    private boolean visible = false;

    public BossHealth() {
        super(Text.literal("Boss Health: §fN/A"), new Feature("bossHealthElement"), "Boss Health Element");
        this.options = this.getBaseSettings();
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && !this.visible) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void setHealth(String health) {
        this.setText(Utils.format("Boss Health: {}", health));
        this.visible = true;
    }

    public void setHidden() {
        this.setVisible(false);
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
