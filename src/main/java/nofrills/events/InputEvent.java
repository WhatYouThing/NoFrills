package nofrills.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import nofrills.config.SettingKeybind;

public final class InputEvent extends Cancellable {
    public int key, modifiers, action;
    public boolean isKeyboard = false, isMouse = false;
    public KeyEvent keyInput = null;
    public MouseButtonInfo mouseInput = null;

    public InputEvent(KeyEvent input, int action) {
        this.setCancelled(false);
        this.key = input.key();
        this.modifiers = input.modifiers();
        this.action = action;
        this.isKeyboard = true;
        this.keyInput = input;
    }

    public InputEvent(MouseButtonInfo input, int action) {
        this.setCancelled(false);
        this.key = input.button();
        this.modifiers = input.modifiers();
        this.action = action;
        this.isMouse = true;
        this.mouseInput = input;
    }

    public boolean isPress() {
        return this.action == InputConstants.PRESS;
    }

    public boolean isRepeat() {
        return this.action == InputConstants.REPEAT;
    }

    public boolean isRelease() {
        return this.action == InputConstants.RELEASE;
    }

    public boolean isKey(int key) {
        return (this.isMouse && key < 0 && this.key == Math.abs(key)) || (this.isKeyboard && key >= 0 && this.key == key);
    }

    public boolean isMouse(int key) {
        return this.isKey(-key);
    }

    public boolean isLeftClick() {
        return this.isMouse(InputConstants.MOUSE_BUTTON_LEFT);
    }

    public boolean isRightClick() {
        return this.isMouse(InputConstants.MOUSE_BUTTON_RIGHT);
    }

    public boolean isKey(SettingKeybind keybind) {
        return keybind.isKey(this);
    }

    public boolean isKey(KeyMapping binding) {
        return (this.keyInput != null && binding.matches(this.keyInput))
                || (this.mouseInput != null && binding.matchesMouse(new MouseButtonEvent(0, 0, this.mouseInput)));
    }

    /**
     * Cancels the input and runs the provided callback if the input is a press.
     */
    public void consume(Runnable onPress) {
        if (this.isPress()) {
            onPress.run();
        }
        this.cancel();
    }
}
