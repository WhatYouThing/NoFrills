package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;

public class BuilderRulerCounter extends SimpleTextElement {

    public static BuilderRulerCounter instance;

    public BuilderRulerCounter(String text) {
        super(Text.literal(text), new Feature("builderRulerCounter"), "Builder Ruler Counter");
        this.setDesc("Shows how many blocks will be placed or removed by the Builder's Ruler.");
        instance = this;
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    public void update(int count, int max, boolean place) {
        String action = place ? "§aPlace" : "§cRemove";
        this.setText(Utils.format("{}: §f{} §7/ §f{}", action, count, max));
    }

    public void hide() {
        this.setText("");
    }
}
