package nofrills.events;

import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;

public class InputEvent extends Cancellable {
    public int key, modifiers, action;
    public boolean isKeyboard = false, isMouse = false;
    public KeyInput keyInput = null;
    public MouseInput mouseInput = null;

    public InputEvent(KeyInput input, int action) {
        this.setCancelled(false);
        this.key = input.key();
        this.modifiers = input.modifiers();
        this.action = action;
        this.isKeyboard = true;
        this.keyInput = input;
    }

    public InputEvent(MouseInput input, int action) {
        this.setCancelled(false);
        this.key = input.button();
        this.modifiers = input.modifiers();
        this.action = action;
        this.isMouse = true;
        this.mouseInput = input;
    }
}
