package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.hud.ClientBossBar;
import nofrills.config.Feature;
import nofrills.events.WorldTickEvent;
import nofrills.hud.HudManager;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;

import java.util.List;

public class DungeonBossHealth {
    public static final Feature instance = new Feature("dungeonBossHealth");

    private static boolean isInBoss() {
        for (int i = 1; i <= 7; i++) {
            if (Utils.isInDungeonBoss(String.valueOf(i))) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    private static void onRender(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (Utils.isInstanceOver() || !isInBoss() || DungeonUtil.isInDragonPhase()) {
                if (HudManager.bossHealthElement.isVisible()) {
                    HudManager.bossHealthElement.setHidden();
                }
                return;
            }
            List<ClientBossBar> bossBars = Utils.getBossBars();
            if (!bossBars.isEmpty()) {
                String health = Utils.format("§c{}%", Utils.formatDecimal(bossBars.getFirst().getPercent() * 100));
                HudManager.bossHealthElement.setHealth(health);
            } else {
                HudManager.bossHealthElement.setHidden();
            }
        }
    }
}