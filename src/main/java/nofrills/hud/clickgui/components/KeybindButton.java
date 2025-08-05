package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class KeybindButton extends ButtonComponent {
    private final EventStream<KeybindChanged> changedEvents = KeybindChanged.newStream();
    private final List<Integer> keybindBlacklist = List.of(
            GLFW.GLFW_KEY_UNKNOWN,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            GLFW.GLFW_KEY_ESCAPE
    );
    public Text unbound = Text.literal("Not Bound").withColor(0xffffff);
    public Text binding = Text.literal("Press Key...").withColor(0xffffff);
    public boolean isBinding = false;

    public KeybindButton() {
        super(Text.empty(), button -> {
        });
        this.onPress(button -> {
            if (this.isBinding) {
                this.bind(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                this.setMessage(this.binding);
                this.isBinding = true;
            }
        });
        this.renderer((context, btn, delta) -> {
            context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0xff101010);
            context.drawBorder(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), 0xff5ca0bf);
        });
        this.horizontalSizing(Sizing.fixed(80));
        this.setMessage(this.unbound);
    }

    public Text getKeyLabel(int keycode) {
        InputUtil.Key input = InputUtil.Type.KEYSYM.createFromCode(keycode);
        if (input.getLocalizedText().getString().equals(input.getTranslationKey())) { // fall back to a mouse key if the keyboard key has no translation
            return InputUtil.Type.MOUSE.createFromCode(keycode).getLocalizedText();
        } else {
            return input.getLocalizedText();
        }
    }

    public void bind(int key) {
        if (!this.valid(key)) {
            this.setMessage(this.unbound);
            changedEvents.sink().onBind(GLFW.GLFW_KEY_UNKNOWN);
        } else {
            this.setMessage(getKeyLabel(key));
            changedEvents.sink().onBind(key);
        }
        this.isBinding = false;
    }

    public EventSource<KeybindChanged> onBound() {
        return changedEvents.source();
    }

    private boolean valid(int key) {
        for (int blacklisted : this.keybindBlacklist) {
            if (key == blacklisted) {
                return false;
            }
        }
        return true;
    }
}
