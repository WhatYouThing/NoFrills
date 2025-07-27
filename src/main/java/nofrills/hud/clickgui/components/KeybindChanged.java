package nofrills.hud.clickgui.components;

import io.wispforest.owo.util.EventStream;

public interface KeybindChanged {
    static EventStream<KeybindChanged> newStream() {
        return new EventStream<>(subscribers -> (keycode) -> {
            for (var subscriber : subscribers) {
                subscriber.onBind(keycode);
            }
        });
    }

    void onBind(int keycode);
}
