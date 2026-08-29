package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TimerElement;

import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public final class QueueCooldownTimer extends TimerElement implements ListeningHudElement {
    private final String name = mc.getUser().getName();
    private final List<Pattern> patterns = List.of(
            Pattern.compile("-*\\n.*" + name + " entered .*!\\n-*"),
            Pattern.compile("-*\\n.*" + name + " queued for .*!\\nThe party is in position #.* of the queue!\\n-*")
    );

    public QueueCooldownTimer() {
        super("Queue Cooldown: {}", new Feature("queueCooldownTimerElement"), "Queue Cooldown Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the cooldown for queueing for instances (Dungeons, Kuudra etc.).");
        this.setCategory(Category.Info);
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        for (Pattern pattern : this.patterns) {
            if (pattern.matcher(event.msg()).matches()) {
                this.start(30000);
                break;
            }
        }
    }
}
