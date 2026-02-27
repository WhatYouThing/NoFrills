package nofrills.hud.elements;

import nofrills.config.Feature;
import nofrills.hud.TickTimerElement;

public class TerminalStartTimer extends TickTimerElement {

    public TerminalStartTimer() {
        super("Terminal Start: §f{}", new Feature("terminalStartTimerElement"), "Terminal Start Timer Element");
        this.setStartTicks(104);
        this.options = this.getBaseSettings();
        this.setDesc("Displays a tick timer for the start of the Goldor phase in F7/M7.");
    }
}
