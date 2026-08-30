package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TickTimerElement;

public final class TerminalStartTimer extends TickTimerElement implements ListeningHudElement {

    public TerminalStartTimer() {
        super("Terminal Start: {}", new Feature("terminalStartTimerElement"), "Terminal Start Timer");
        this.setStartTicks(104);
        this.options = this.getBaseSettings();
        this.setDesc("Displays a tick timer for the start of the Goldor phase in F7/M7.");
        this.setAutoPause();
        this.setCategory(Category.Dungeons);
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        if (event.msg().equals("[BOSS] Storm: I should have known that I stood no chance.")) {
            this.start();
        }
    }
}
