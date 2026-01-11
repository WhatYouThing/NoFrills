package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.SlimSliderComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import org.lwjgl.glfw.GLFW;

public class FlatSlider extends SlimSliderComponent {
    public int trackColor;
    public int sliderColor;

    public FlatSlider(int trackColor, int sliderColor) {
        super(Axis.HORIZONTAL);
        this.trackColor = trackColor;
        this.sliderColor = sliderColor;
        this.cursorStyle(CursorStyle.POINTER);
        this.keyPress().subscribe((click) -> {
            if (click.key() == GLFW.GLFW_KEY_LEFT) {
                this.value(this.value() - this.stepSize);
                return true;
            }
            if (click.key() == GLFW.GLFW_KEY_RIGHT) {
                this.value(this.value() + this.stepSize);
                return true;
            }
            return false;
        });
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        context.fill(this.x, this.y + 8, this.x + this.width, this.y + this.height - 8, this.trackColor);
        int sliderX = (int) (this.x + (this.width - 4) * this.value.get());
        context.fill(sliderX, this.y + 2, sliderX + 4, this.y + this.height - 2, this.sliderColor);
    }
}
