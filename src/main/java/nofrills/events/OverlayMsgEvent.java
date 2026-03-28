package nofrills.events;

import net.minecraft.network.chat.Component;

public class OverlayMsgEvent extends Cancellable {
    public Component message;
    public String messagePlain;

    public OverlayMsgEvent(Component message, String messagePlain) {
        this.setCancelled(false);
        this.message = message;
        this.messagePlain = messagePlain;
    }

    public Component getMessage() {
        return this.message;
    }

    public String getPlainMessage() {
        return this.messagePlain;
    }
}
