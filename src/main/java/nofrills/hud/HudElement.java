package nofrills.hud;

import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import nofrills.config.SettingDouble;
import org.lwjgl.glfw.GLFW;

import java.util.List;

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
        this.layout.allowOverflow(true);
        this.foreheadSize(0);
        this.allowOverflow(true);
        this.child(this.layout);
        HudManager.addNew(this);
    }

    @Override
    protected void drawChildren(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta, List<? extends Component> children) {
        try {
            super.drawChildren(context, mouseX, mouseY, partialTicks, delta, children);
        } catch (Exception ignored) {
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
        int width = window.getScaledWidth(), height = window.getScaledHeight();
        this.xOffset = Math.clamp(x.value() * width, 0, Math.clamp(width - this.width, 0, width));
        this.yOffset = Math.clamp(y.value() * height, 0, Math.clamp(height - this.height, 0, height));
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