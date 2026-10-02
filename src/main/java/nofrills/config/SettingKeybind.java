package nofrills.config;

import com.mojang.blaze3d.platform.InputConstants;
import nofrills.compat.LegacyInput;

import static nofrills.Main.mc;

public class SettingKeybind extends SettingInt {
    public static final int UNKNOWN_KEY = LegacyInput.KEY_UNKNOWN;

    public SettingKeybind(int defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingKeybind(int defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public static InputConstants.Key asInputConstant(int key) {
        return LegacyInput.asInputConstant(key);
    }

    public int key() {
        return this.value();
    }

    public boolean bound() {
        return this.value() != -1;
    }

    public boolean isKey(int key) {
        return key != -1 && key == this.value();
    }

    public InputConstants.Key asInputConstant() {
        return asInputConstant(this.key());
    }

    public boolean isMouse() {
        return this.asInputConstant().getType().equals(InputConstants.Type.MOUSE);
    }

    public boolean isDown() {
        return this.bound() && LegacyInput.isDown(this.key());
    }
}