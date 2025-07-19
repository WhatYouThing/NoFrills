package nofrills.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static nofrills.Main.mc;

public class HudEditorScreen extends Screen {
    public HudEditorScreen() {
        super(Text.of(""));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(mc.textRenderer, "NoFrills HUD Editor", context.getScaledWindowWidth() / 2, 10, 0xffffff);
        for (HudElement element : HudManager.elements) {
            element.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void close() {
        super.close();
    }
}