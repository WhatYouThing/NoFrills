package nofrills.config;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import static nofrills.Main.mc;

public class SettingKeybind extends SettingInt {
    public static final int UNKNOWN_KEY = InputConstants.UNKNOWN.getValue();

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
        return this.value() != UNKNOWN_KEY;
    }

    public boolean isKey(int key) {
        return key != UNKNOWN_KEY && key == this.value();
    }

    public InputConstants.Key asInputConstant() {
        return asInputConstant(this.key());
    }

    public boolean isMouse() {
        return this.asInputConstant().getType().equals(InputConstants.Type.MOUSE);
    }

    public boolean isDown() {
        if (this.isMouse()) {
            return this.bound() && GLFW.glfwGetMouseButton(mc.getWindow().handle(), this.key()) == 1;
        }
        return this.bound() && InputConstants.isKeyDown(mc.getWindow(), this.key());
    }
}