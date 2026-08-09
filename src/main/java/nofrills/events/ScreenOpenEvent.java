package nofrills.events;

import net.minecraft.client.gui.screens.Screen;
import nofrills.misc.Utils;

public class ScreenOpenEvent {
    public Screen screen;

    public ScreenOpenEvent(Screen screen) {
        this.screen = screen;
    }

    public boolean isPaginatedMenu(String match) {
        return Utils.isPaginatedMenu(this.screen.getTitle().getString(), match);
    }
}
