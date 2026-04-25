package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TimerElement;

import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class QueueCooldownTimer extends TimerElement {
    public final String name = mc.getSession().getUsername();
    public final List<Pattern> patterns = List.of(
            Pattern.compile("-*\\n.*" + name + " entered .*!\\n-*"),
            Pattern.compile("-*\\n.*" + name + " queued for .*!\\nThe party is in position #.* of the queue!\\n-*")
    );

    public QueueCooldownTimer() {
        super("Queue Cooldown: {}", new Feature("queueCooldownTimerElement"), "Queue Cooldown Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the cooldown for queueing for instances (Dungeons, Kuudra etc.).");
        this.setCategory(Category.Info);
    }
}
