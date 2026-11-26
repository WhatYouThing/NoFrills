package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TickTimerElement;
import nofrills.misc.SlayerUtil;

public final class SlayerCocoonTimer extends TickTimerElement implements ListeningHudElement {

    public SlayerCocoonTimer() {
        super("Cocoon: {}", new Feature("slayerCocoonTimerElement"), "Slayer Cocoon Timer");
        this.setStartTicks(127);
        this.options = this.getBaseSettings();
        this.setDesc("Displays a timer for the cocoon animation of your slayer boss.");
        this.setAutoPause();
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        if (SlayerUtil.isBossCocoonedMessage(event.msg())) {
            this.start();
        }
    }
}
