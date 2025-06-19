package nofrills.hud.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import nofrills.config.Config;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class ClickGuiScreen extends Screen {
    private final ClickGuiCategory deez = new ClickGuiCategory(Text.of("nuts"), 100, 100);
    private boolean lastClicked = false;

    public ClickGuiScreen() {
        super(Text.of(""));
    }

    private boolean isLeftClickPressed() {
        return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == 1;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        float scale = 2f / mc.options.getGuiScale().getValue();
        context.getMatrices().scale(scale, scale, scale);
        deez.render(context, mouseX, mouseY, delta);
        boolean clicked = isLeftClickPressed();
        lastClicked = clicked;
        context.getMatrices().pop();
    }

    @Override
    public void close() {
        Config.configHandler.save();
        super.close();
    }
}