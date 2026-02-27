package nofrills.events;

import net.minecraft.text.Text;

public class OverlayMsgEvent extends Cancellable {
    public Text message;
    public String messagePlain;

    public OverlayMsgEvent(Text message, String messagePlain) {
        this.setCancelled(false);
        this.message = message;
        this.messagePlain = messagePlain;
    }

    public Text getMessage() {
        return this.message;
    }

    public String getPlainMessage() {
        return this.messagePlain;
    }
}
