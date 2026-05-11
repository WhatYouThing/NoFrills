package nofrills.events;

import net.minecraft.client.network.PlayerListEntry;

import java.util.UUID;

public class PlayerJoinedEvent {
    public UUID uuid;
    public PlayerListEntry entry;

    public PlayerJoinedEvent(UUID uuid, PlayerListEntry entry) {
        this.uuid = uuid;
        this.entry = entry;
    }

    public boolean isRealPlayer() {
        if (this.uuid.version() == 4 && this.entry.getDisplayName() == null) {
            String name = this.entry.getProfile().name();
            return !name.contains(" ") && !name.contains("§") && !name.startsWith("!") && !name.isBlank();
        }
        return false;
    }
}
