package nofrills.hud.elements;

import net.minecraft.world.level.block.Blocks;
import nofrills.config.Feature;
import nofrills.events.BlockUpdateEvent;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TickTimerElement;
import nofrills.misc.DungeonUtil;

public final class TerracottaGyroTimer extends TickTimerElement implements ListeningHudElement {

    public TerracottaGyroTimer() {
        super("Gyro: {}", new Feature("terracottaGyroTimerElement"), "Terracotta Gyro Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays a timer for the first terracotta phase and the final giant phase in F6/M6.");
        this.setAutoPause();
        this.setCategory(Category.Dungeons);
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        if (event.msg().equals("[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!") && DungeonUtil.isOnFloor("6")) {
            this.setStartTicks(267);
            this.start();
        }
    }

    @Override
    public void onBlockUpdate(BlockUpdateEvent event) {
        if (!this.isTicking() && event.newState.getBlock().equals(Blocks.NETHER_BRICK_FENCE)) {
            this.setStartTicks(235);
            this.start();
        }
    }
}
