package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import nofrills.config.Feature;
import nofrills.events.WorldTickEvent;
import nofrills.hud.HudManager;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

public class SlayerBossHealth {
    public static final Feature instance = new Feature("slayerBossHealth");

    @EventHandler
    private static void onRender(WorldTickEvent event) {
        if (instance.isActive()) {
            if (!SlayerUtil.bossAlive) {
                if (HudManager.bossHealthElement.isVisible()) {
                    HudManager.bossHealthElement.setHidden();
                }
                return;
            }
            Entity nameEntity = SlayerUtil.getNameEntity();
            if (nameEntity == null) return;
            String name = Utils.toPlain(nameEntity.getName()).replaceAll(Utils.Symbols.vampLow, "").trim();
            String[] parts = name.split(" ");
            if (name.endsWith("Hits") || name.endsWith("Hit")) {
                HudManager.bossHealthElement.setHealth(Utils.format("§d{} {}", parts[parts.length - 2], parts[parts.length - 1]));
            } else {
                String health = parts[parts.length - 1].replaceAll(Utils.Symbols.heart, "").trim();
                HudManager.bossHealthElement.setHealth(Utils.format("§a{}", health));
            }
        }
    }
}