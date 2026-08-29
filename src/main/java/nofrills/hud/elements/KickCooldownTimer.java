package nofrills.hud.elements;

import com.google.common.collect.Sets;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TimerElement;

import java.util.HashSet;

public final class KickCooldownTimer extends TimerElement implements ListeningHudElement {
    private final HashSet<String> messages = Sets.newHashSet(
            "A kick occurred in your connection, so you were put in the SkyBlock lobby!",
            "You were kicked while joining that server!"
    );

    public KickCooldownTimer() {
        super("Kick Cooldown: {}", new Feature("kickCooldownTimerElement"), "Kick Cooldown Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the SkyBlock join cooldown after you get kicked to the lobby.");
        this.setCategory(Category.Misc);
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        if (!this.started() && this.messages.contains(event.msg())) {
            this.start(60000);
        }
    }
}
