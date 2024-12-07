package nofrills.events;

import net.minecraft.client.gui.screen.Screen;

public class ScreenOpenedEvent {
    public Screen screen;

    public ScreenOpenedEvent(Screen screen) {
        this.screen = screen;
    }
}
