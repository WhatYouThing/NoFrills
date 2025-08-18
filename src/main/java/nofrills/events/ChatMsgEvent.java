package nofrills.events;

import net.minecraft.text.Text;

public class ChatMsgEvent extends Cancellable {
    public Text message;
    public String messagePlain;

    public ChatMsgEvent(Text message, String messagePlain) {
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
