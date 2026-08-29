package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TimerElement;

public final class PhoenixPetTimer extends TimerElement implements ListeningHudElement {

    public PhoenixPetTimer() {
        super("Phoenix Pet: {}", new Feature("phoenixPetTimerElement"), "Phoenix Pet Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the cooldown of the Phoenix Pet Rekindle ability.");
        this.setCategory(Category.Dungeons);
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        if (event.msg().equals("Your Phoenix Pet saved you from certain death!")) {
            this.start(60000);
        }
    }
}
