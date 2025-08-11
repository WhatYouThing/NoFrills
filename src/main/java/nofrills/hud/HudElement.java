package nofrills.hud;

import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import nofrills.config.SettingDouble;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class HudElement extends DraggableContainer<FlowLayout> {
    public FlowLayout layout;
    public HudSettings options;
    public boolean toggling = false;
    public Surface disabledSurface = Surface.flat(0x55ff0000);

    public HudElement(FlowLayout layout) {
        super(Sizing.content(), Sizing.content(), layout);
        this.positioning(Positioning.absolute(0, 0));
        this.layout = layout;
        this.layout.sizing(Sizing.content(), Sizing.content());
        this.foreheadSize(0);
        this.child(this.layout);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.focusHandler() != null) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.toggling = true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            mc.setScreen(this.options);
            return true;
        }
        return super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        boolean result = super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
        Window window = mc.getWindow();
        this.savePosition(this.xOffset / window.getScaledWidth(), this.yOffset / window.getScaledHeight());
        return result;
    }

    @Override
    public Component childAt(int x, int y) {
        if (this.isInBoundingBox(x, y)) { // gets rid of the forehead
            return this;
        }
        return super.childAt(x, y);
    }

    public void updateSurface(boolean active) {
        this.layout.surface(active ? Surface.BLANK : this.disabledSurface);
    }

    public void updatePosition(SettingDouble x, SettingDouble y) {
        Window window = mc.getWindow();
        this.xOffset = Math.clamp(x.value() * window.getScaledWidth(), 0, window.getScaledWidth() - this.width);
        this.yOffset = Math.clamp(y.value() * window.getScaledHeight(), 0, window.getScaledHeight() - this.height);
        this.updateX(0);
        this.updateY(0);
    }

    public void toggle() {

    }

    public void updatePosition() {

    }

    public void savePosition(double x, double y) {

    }

    public Identifier getIdentifier() {
        return null;
    }
}