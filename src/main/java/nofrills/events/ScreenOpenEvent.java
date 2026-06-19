package nofrills.events;

import net.minecraft.client.gui.screens.Screen;

public class ScreenOpenEvent {
    public Screen screen;

    public ScreenOpenEvent(Screen screen) {
        this.screen = screen;
    }
}
