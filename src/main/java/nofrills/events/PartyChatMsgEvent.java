package nofrills.events;

public class PartyChatMsgEvent extends Cancellable {

    public String message;
    public String sender;
    public boolean self;

    /**
     * Triggered when anyone in your party sends a message.
     *
     * @param message The raw message that was sent
     * @param sender  The name of the sender
     * @param self    Returns <code>true</code> if you sent the message, <code>false</code> otherwise.
     */
    public PartyChatMsgEvent(String message, String sender, boolean self) {
        this.setCancelled(false);
        this.message = message;
        this.sender = sender;
        this.self = self;
    }
}
