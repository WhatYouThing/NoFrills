package nofrills.events;

import net.minecraft.network.chat.Component;

public class ChatMsgEvent extends Cancellable {
    public Component message;
    public String messagePlain;

    public ChatMsgEvent(Component message, String messagePlain) {
        this.setCancelled(false);
        this.message = message;
        this.messagePlain = messagePlain;
    }

    public Component getMessage() {
        return this.message;
    }

    public String msg() {
        return this.messagePlain;
    }
}
