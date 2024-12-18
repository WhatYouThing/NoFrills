package nofrills.events;

import net.minecraft.client.gui.screen.Screen;

public class ScreenOpenEvent {
    public Screen screen;

    public ScreenOpenEvent(Screen screen) {
        this.screen = screen;
    }
}
