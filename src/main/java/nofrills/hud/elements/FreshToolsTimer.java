package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TickTimerElement;
import nofrills.misc.Utils;

public final class FreshToolsTimer extends TickTimerElement implements ListeningHudElement {

    public FreshToolsTimer() {
        super("Fresh Tools: {}", new Feature("freshToolsTimerElement"), "Fresh Tools Timer");
        this.setStartTicks(200);
        this.options = this.getBaseSettings();
        this.setDesc("Displays a timer for the Fresh Tools perk shop ability in Kuudra.");
        this.setAutoPause();
        this.setCategory(Category.Kuudra);
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        if (event.msg().equals("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!") && Utils.isInKuudra()) {
            this.start();
        }
    }
}
