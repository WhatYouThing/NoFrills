package nofrills.events;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import nofrills.config.SettingKeybind;
import nofrills.compat.LegacyInput;

public final class InputEvent extends Cancellable {
    public int key, modifiers, action;
    public boolean isKeyboard = false, isMouse = false;
    public KeyEvent keyInput = null;
    public MouseButtonInfo mouseInput = null;

    public InputEvent(KeyEvent input, int action) {
        this.setCancelled(false);
        this.key = LegacyInput.fromKeyboard(input.key());
        this.modifiers = LegacyInput.fromModifiers(input.modifiers());
        this.action = LegacyInput.fromAction(action);
        this.isKeyboard = true;
        this.keyInput = input;
    }

    public InputEvent(MouseButtonInfo input, int action) {
        this.setCancelled(false);
        this.key = LegacyInput.fromMouseButton(input.button());
        this.modifiers = LegacyInput.fromModifiers(input.modifiers());
        this.action = LegacyInput.fromAction(action);
        this.isMouse = true;
        this.mouseInput = input;
    }

    public boolean isPress() {
        return this.action == LegacyInput.PRESS;
    }

    public boolean isRepeat() {
        return this.action == LegacyInput.REPEAT;
    }

    public boolean isRelease() {
        return this.action == LegacyInput.RELEASE;
    }

    public boolean isKey(int key) {
        return this.key == key;
    }

    public boolean isKey(SettingKeybind keybind) {
        return keybind.isKey(this.key);
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
