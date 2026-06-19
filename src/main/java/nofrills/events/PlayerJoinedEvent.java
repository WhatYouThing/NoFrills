package nofrills.events;

import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.UUID;

public class PlayerJoinedEvent {
    public UUID uuid;
    public PlayerInfo entry;

    public PlayerJoinedEvent(UUID uuid, PlayerInfo entry) {
        this.uuid = uuid;
        this.entry = entry;
    }

    public boolean isRealPlayer() {
        if (this.uuid.version() == 4 && this.entry.getTabListDisplayName() == null) {
            String name = this.entry.getProfile().name();
            return !name.contains(" ") && !name.contains("§") && !name.startsWith("!") && !name.isBlank();
        }
        return false;
    }
}
