package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TimerElement;

public final class SpiritMaskTimer extends TimerElement implements ListeningHudElement {

    public SpiritMaskTimer() {
        super("Spirit Mask: {}", new Feature("spiritMaskTimerElement"), "Spirit Mask Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the cooldown of the Spirit Mask Second Wind ability.");
        this.setAutoPause();
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        if (event.msg().equals("Second Wind Activated! Your Spirit Mask saved your life!")) {
            this.start(30000);
        }
    }
}
