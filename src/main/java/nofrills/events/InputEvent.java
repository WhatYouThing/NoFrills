package nofrills.events;

public class InputEvent extends Cancellable {
    private static final InputEvent INSTANCE = new InputEvent();

    public int key, modifiers, action;

    public static InputEvent get(int key, int modifiers, int action) {
        INSTANCE.setCancelled(false);
        INSTANCE.key = key;
        INSTANCE.modifiers = modifiers;
        INSTANCE.action = action;
        return INSTANCE;
    }
}
