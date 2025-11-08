package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.hud.clickgui.Settings;

public class ToggleButton extends ButtonComponent {
    public static final MutableText enabledText = Text.literal("Enabled").withColor(0x55ff55);
    public static final MutableText disabledText = Text.literal("Disabled").withColor(0xff5555);
    private final EventStream<ToggleChanged> changedEvents = ToggleChanged.newStream();
    private boolean toggle;

    public ToggleButton(boolean toggled) {
        super(disabledText, buttonComponent -> {
        });
        this.renderer(Settings.buttonRenderer);
        this.toggle = toggled;
        this.setMessage(this.toggle ? enabledText : disabledText);
        this.onPress(button -> this.setToggle());
    }

    public void setToggle() {
        this.setToggle(!this.toggle);
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
        this.setMessage(this.toggle ? enabledText : disabledText);
        changedEvents.sink().onToggle(this.toggle);
    }

    public EventSource<ToggleChanged> onToggled() {
        return changedEvents.source();
    }

    public interface ToggleChanged {
        static EventStream<ToggleChanged> newStream() {
            return new EventStream<>(subscribers -> (toggle) -> {
                for (var subscriber : subscribers) {
                    subscriber.onToggle(toggle);
                }
            });
        }

        void onToggle(boolean toggle);
    }
}
