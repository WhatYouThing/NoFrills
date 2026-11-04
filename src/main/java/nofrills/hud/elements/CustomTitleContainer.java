package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;

public final class CustomTitleContainer extends SimpleTextElement {

    public CustomTitleContainer() {
        super(Component.literal("Custom Title"), new Feature("customTitleContainer"), "Custom Title Container");
        this.options = this.getBaseSettings();
        this.setDesc("Replaces non-vanilla title rendering with itself while active.");
        this.setCategory(Category.Misc);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && this.text == this.defaultText) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }
}
