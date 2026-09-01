package nofrills.hud.elements;

import net.minecraft.world.level.block.Blocks;
import nofrills.config.Feature;
import nofrills.events.BlockUpdateEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TickTimerElement;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;

public final class SpiritBearTimer extends TickTimerElement implements ListeningHudElement {
    private int count = 0;

    public SpiritBearTimer() {
        super("Spirit Bear: {}", new Feature("spiritBearTimerElement"), "Spirit Bear Timer");
        this.setStartTicks(68);
        this.options = this.getBaseSettings();
        this.setDesc("Displays a timer for the Spirit Bear spawning in F4/M4.");
        this.setAutoPause();
        this.setCategory(Category.Dungeons);
    }

    @Override
    public void onBlockUpdate(BlockUpdateEvent event) {
        if (event.newState.getBlock().equals(Blocks.SEA_LANTERN) && DungeonUtil.isInBossRoom("4")) {
            this.count++;
            if (this.count == 165) {
                this.start();
                this.count = 0;
            }
        }
    }

    @Override
    public void onServerJoin() {
        this.count = 0;
    }

    @Override
    public void updateTimer() {
        if (super.isTicking()) {
            super.updateTimer();
        } else {
            int needed = DungeonUtil.isOnFloor("M4") ? 30 : 25;
            int kills = (int) Math.floor(needed * (this.count / 165.0));
            this.setText(Utils.format(this.timerText,
                    Utils.getPercentageColor(kills / (double) needed, true) + kills + "/" + needed)
            );
        }
    }

    @Override
    public boolean isTicking() {
        return DungeonUtil.isInBossRoom("4");
    }
}
