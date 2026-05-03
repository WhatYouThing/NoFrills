package nofrills.config;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SettingKeybind extends SettingInt {
    public SettingKeybind(int defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingKeybind(int defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public static InputUtil.Key asInputConstant(int key) {
        InputUtil.Key keyboard = InputUtil.Type.KEYSYM.createFromCode(key);
        if (keyboard.getLocalizedText().getString().equals(keyboard.getTranslationKey())) { // fall back to a mouse key if the keyboard key has no translation
            return InputUtil.Type.MOUSE.createFromCode(key);
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

    public InputUtil.Key asInputConstant() {
        return asInputConstant(this.key());
    }
}