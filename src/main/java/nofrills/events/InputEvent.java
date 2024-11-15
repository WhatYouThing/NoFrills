package nofrills.events;

public class InputEvent extends Cancellable {
    public int key, modifiers, action;

    public InputEvent(int key, int modifiers, int action) {
        this.setCancelled(false);
        this.key = key;
        this.modifiers = modifiers;
        this.action = action;
    }
}
