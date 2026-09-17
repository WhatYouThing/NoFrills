package nofrills.hud.elements;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TickTimerElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;

public final class PadTimer extends TickTimerElement implements ListeningHudElement {
    public SettingBool totalTime;
    private int totalTicks = 0;

    public PadTimer() {
        super("Pad: {}", new Feature("padTimerElement"), "Pad Timer");
        this.totalTime = new SettingBool(true, "totalTime", this.instance);
        this.setStartTicks(20);
        this.setRepeating(true);
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Total Time", this.totalTime, "Displays the total time spent in the Storm phase.")
        ));
        this.setDesc("Displays a tick timer for the pads in the F7/M7 Storm phase.");
        this.setAutoPause();
        this.setCategory(Category.Dungeons);
    }

    @Override
    public void updateTimer() {
        MutableComponent timer = Utils.formatText(this.timerText, Component.literal(this.ticksAsTime(this.ticks)).withColor(this.getTimeColor()));
        if (this.totalTime.value()) {
            timer.append(Component.literal(" (" + this.ticksAsTime(this.totalTicks) + ")").withStyle(ChatFormatting.GRAY));
        }
        this.setText(timer);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ticks > 0) {
            this.totalTicks++;
        }
    }

    @Override
    public void pause() {
        super.pause();
        this.totalTicks = 0;
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        if (event.msg().equals("[BOSS] Storm: Pathetic Maxor, just like expected.")) {
            this.start();
        } else if (event.msg().equals("[BOSS] Storm: I should have known that I stood no chance.")) {
            this.pause();
        }
    }
}
