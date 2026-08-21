package nofrills.hud.clickgui.components;

import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.network.chat.Component;
import nofrills.config.SettingKeybind;
import nofrills.misc.Rendering;

import java.util.List;

public final class KeybindButton extends ButtonComponent {
    private final EventStream<KeybindChanged> changedEvents = KeybindChanged.newStream();
    private final List<Integer> keybindBlacklist = List.of(
            -1,
            InputConstants.KEY_ESCAPE
    );
    public Component unbound = Component.literal("Not Bound").withColor(0xffffff);
    public Component binding = Component.literal("Press Key...").withColor(0xffffff);
    public boolean isBinding = false;

    public KeybindButton() {
        super(Component.empty(), button -> {
        });
        this.onPress(button -> {
            if (this.isBinding) {
                this.bind(-1);
            } else {
                this.setMessage(this.binding);
                this.isBinding = true;
            }
        });
        this.renderer((context, btn, delta) -> {
            context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0xff101010);
            Rendering.drawBorder(context, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), 0xff5ca0bf);
        });
        this.horizontalSizing(Sizing.fixed(80));
        this.setMessage(this.unbound);
    }

    public Component getKeyLabel(int keycode) {
        return SettingKeybind.asInputConstant(keycode).getDisplayName();
    }

    public void bind(int key) {
        if (!this.valid(key)) {
            this.setMessage(this.unbound);
            changedEvents.sink().onBind(-1);
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

    public interface KeybindChanged {
        static EventStream<KeybindChanged> newStream() {
            return new EventStream<>(subscribers -> (keycode) -> {
                for (var subscriber : subscribers) {
                    subscriber.onBind(keycode);
                }
            });
        }

        void onBind(int keycode);
    }
}
