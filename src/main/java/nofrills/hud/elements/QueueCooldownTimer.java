package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TimerElement;

import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class QueueCooldownTimer extends TimerElement {
    public final Pattern pattern = Pattern.compile("-*\\n.*" + mc.getSession().getUsername() + " entered .*!\\n-*");

    public QueueCooldownTimer() {
        super("Queue Cooldown: {}", new Feature("queueCooldownTimerElement"), "Queue Cooldown Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the cooldown for queueing for instances (Dungeons, Kuudra etc.).");
        this.setCategory(Category.Info);
    }
}
