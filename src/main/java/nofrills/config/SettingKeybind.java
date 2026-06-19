package nofrills.config;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class SettingKeybind extends SettingInt {
    public SettingKeybind(int defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingKeybind(int defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public static InputConstants.Key asInputConstant(int key) {
        InputConstants.Key keyboard = InputConstants.Type.KEYSYM.getOrCreate(key);
        if (keyboard.getDisplayName().getString().equals(keyboard.getName())) { // fall back to a mouse key if the keyboard key has no translation
            return InputConstants.Type.MOUSE.getOrCreate(key);
        } else {
            return keyboard;
        }
    }

    public int key() {
        return this.value();
    }

    public boolean bound() {
        return this.value() != GLFW.GLFW_KEY_UNKNOWN;
    }

    public boolean isKey(int key) {
        return key != GLFW.GLFW_KEY_UNKNOWN && key == this.value();
    }

    public InputConstants.Key asInputConstant() {
        return asInputConstant(this.key());
    }
}