package nofrills.events;

import meteordevelopment.orbit.ICancellable;

public class Cancellable implements ICancellable {
    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}