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

    public boolean isPartyFinderJoin() {
        return this.messagePlain.startsWith("Party Finder >") && this.messagePlain.contains("joined");
    }

    public String getPartyFinderJoinName() {
        return this.messagePlain.replace("Party Finder >", "").trim().split(" ", 2)[0];
    }
}
