package nofrills.hud;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class HudEditorScreen extends BaseOwoScreen<FlowLayout> {
    public HudEditorScreen() {
        super(Text.of(""));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        root.allowOverflow(false);
        for (HudElement element : HudManager.elements) {
            root.child(element);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (HudElement element : HudManager.elements) {
            element.updatePosition();
        }
        super.render(context, mouseX, mouseY, delta);
        int center = context.getScaledWindowWidth() / 2;
        context.drawCenteredTextWithShadow(mc.textRenderer, "NoFrills HUD Editor", center, 10, 0xffffff);
        context.drawCenteredTextWithShadow(mc.textRenderer, "Left click element to toggle", center, 20, 0xffffff);
        context.drawCenteredTextWithShadow(mc.textRenderer, "Right click element to view its settings", center, 30, 0xffffff);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (HudElement element : HudManager.elements) {
                if (element.toggling) {
                    element.toggling = false;
                    element.toggle();
                    return true;
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (HudElement element : HudManager.elements) {
            if (element.toggling) {
                element.toggling = false;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void close() {
        super.close();
    }
}