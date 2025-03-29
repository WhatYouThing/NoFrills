package nofrills.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class HudEditorScreen extends Screen {
    HudElement test = HudManager.testElement;
    private boolean lastClicked = false;
    private HudElement selected = null;

    public HudEditorScreen() {
        super(Text.of(""));
    }

    private boolean isLeftClickPressed() {
        return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == 1;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        test.render(context, mouseX, mouseY, delta);
        boolean clicked = isLeftClickPressed();
        if (selected != null) {
            if (clicked) {
                selected.move(context, mouseX, mouseY, hasShiftDown());
            } else {
                selected = null;
            }
        } else {
            if (clicked && !lastClicked && test.isHovered(mouseX, mouseY)) {
                selected = test;
            }
        }
        lastClicked = clicked;
    }
}