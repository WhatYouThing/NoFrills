package nofrills.events;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;

public class InputEvent extends Cancellable {
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
}
