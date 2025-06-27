package nofrills.hud.clickgui.components;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.text.Text;

import java.util.function.Consumer;

import static nofrills.Main.Config;

public class ResetButton extends ButtonComponent {
    public Option.Key optionKey;

    public ResetButton(Option.Key optionKey, Consumer<ButtonComponent> onPress) {
        super(Text.literal("Reset").withColor(0xdddddd), onPress);
        this.optionKey = optionKey;
        this.renderer((context, button, delta) -> {
            context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
            context.drawBorder(button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xffdddddd);
        });
    }

    public Object resetValue() {
       Object defaultValue = Config.optionForKey(this.optionKey).defaultValue();
       Config.optionForKey(this.optionKey).set(defaultValue);
       return defaultValue;
    }
}
