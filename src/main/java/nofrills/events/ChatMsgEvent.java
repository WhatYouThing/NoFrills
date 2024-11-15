package nofrills.events;

import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

public class ChatMsgEvent extends Cancellable {
    public Text message;
    public String messagePlain;
    public MessageSignatureData signatureData;
    public MessageIndicator messageIndicator;

    public ChatMsgEvent(Text message, String messagePlain, MessageSignatureData signatureData, MessageIndicator messageIndicator) {
        this.setCancelled(false);
        this.message = message;
        this.messagePlain = messagePlain;
        this.signatureData = signatureData;
        this.messageIndicator = messageIndicator;
    }

    public Text getMessage() {
        return this.message;
    }

    public String getPlainMessage() {
        return this.messagePlain;
    }
}
