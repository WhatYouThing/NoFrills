package nofrills.hud.elements;

import net.minecraft.world.level.block.Blocks;
import nofrills.config.Feature;
import nofrills.events.BlockUpdateEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TickTimerElement;
import nofrills.misc.DungeonUtil;

public final class SpiritBearTimer extends TickTimerElement implements ListeningHudElement {

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
            if (event.pos.getX() == 7 && event.pos.getY() == 77 && event.pos.getZ() == 34) {
                this.start();
            }
        }
    }
}
