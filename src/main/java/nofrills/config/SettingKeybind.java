package nofrills.config;

import com.mojang.blaze3d.platform.InputConstants;
import nofrills.events.InputEvent;
import org.lwjgl.sdl.SDLMouse;

public class SettingKeybind extends SettingInt {
    public static final int UNKNOWN_KEY = InputConstants.UNKNOWN.getValue();

    public SettingKeybind(int defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingKeybind(int defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public static InputConstants.Key asInputConstant(int key) {
        if (key < 0) {
            return InputConstants.Type.MOUSE.map.getOrDefault(Math.abs(key), InputConstants.UNKNOWN);
        }
        return InputConstants.Type.KEYBOARD.map.getOrDefault(key, InputConstants.UNKNOWN);
    }

    public int key() {
        return this.value();
    }

    public int mouseKey() {
        return Math.abs(this.key());
    }

    public boolean bound() {
        return this.value() != UNKNOWN_KEY;
    }

    public boolean isKey(int key) {
        return this.bound() && key == this.value();
    }

    public boolean isKey(InputEvent event) {
        if (this.bound()) {
            return (event.isMouse && this.isMouse() && event.key == this.mouseKey()) || (event.isKeyboard && !this.isMouse() && event.key == this.key());
        }
        return false;
    }

    public InputConstants.Key asInputConstant() {
        return asInputConstant(this.key());
    }

    public boolean isMouse() {
        return this.key() < 0; // store mouse buttons as negative since they now share values with keyboard keys
    }

    public boolean isDown() {
        if (this.isMouse()) {
            return this.bound() && (SDLMouse.SDL_GetMouseState(null, null) & (1 << (this.mouseKey() - 1))) > 0;
        }
        return this.bound() && InputConstants.isKeyDown(this.key());
    }
}