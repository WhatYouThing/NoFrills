package nofrills.events;

import static nofrills.Main.mc;

public class PartyChatMsgEvent extends Cancellable {

    public String message;
    public String sender;
    public boolean self;

    public PartyChatMsgEvent(String message, String sender) {
        this.setCancelled(false);
        this.message = message;
        this.sender = sender;
        this.self = sender.equalsIgnoreCase(mc.getSession().getUsername());
    }
}
