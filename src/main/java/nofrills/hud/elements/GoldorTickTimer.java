package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TickTimerElement;

public final class GoldorTickTimer extends TickTimerElement implements ListeningHudElement {

    public GoldorTickTimer() {
        super("Goldor Tick: {}", new Feature("goldorTickTimerElement"), "Goldor Tick Timer");
        this.setStartTicks(60);
        this.setRepeating(true);
        this.options = this.getBaseSettings();
        this.setDesc("Displays a tick timer for the death tick in the F7/M7 Goldor phase.");
        this.setAutoPause();
        this.setCategory(Category.Dungeons);
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        if (event.msg().equals("[BOSS] Goldor: Who dares trespass into my domain?")) {
            this.start();
        } else if (event.msg().equals("The Core entrance is opening!")) {
            this.pause();
        }
    }
}
