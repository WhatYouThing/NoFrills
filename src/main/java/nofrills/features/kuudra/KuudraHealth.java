package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.mob.MagmaCubeEntity;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.WorldTickEvent;
import nofrills.hud.HudManager;
import nofrills.misc.KuudraUtil;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

public class KuudraHealth {
    public static final Feature instance = new Feature("kuudraHealth");

    public static final SettingBool dps = new SettingBool(false, "dps", instance.key());

    private static final List<Float> dpsData = new ArrayList<>();
    private static float previousHealth = 0.0f;

    private static float getTrueHealth(float health) {
        return (health - 1024.0f) * 10000.0f;
    }

    private static float calculateDPS() {
        float total = 0.0f;
        List<Float> list = new ArrayList<>(dpsData);
        for (float damage : list) {
            total += damage;
        }
        return total / list.size();
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInKuudra()) {
            MagmaCubeEntity kuudra = KuudraUtil.getKuudraEntity();
            KuudraUtil.phase phase = KuudraUtil.getCurrentPhase();
            if (kuudra == null) {
                if (phase.equals(KuudraUtil.phase.DPS)) {
                    List<ClientBossBar> bossBars = Utils.getBossBars();
                    if (!bossBars.isEmpty()) {
                        String health = Utils.format("§e{}%", Utils.formatDecimal(bossBars.getFirst().getPercent() * 100));
                        HudManager.bossHealthElement.setHealth(health);
                    }
                }
                return;
            }
            if (phase.equals(KuudraUtil.phase.DPS)) {
                String health = Utils.format("§e{}%", Utils.formatDecimal(kuudra.getHealth() / kuudra.getMaxHealth()));
                HudManager.bossHealthElement.setHealth(health);
            }
            if (phase.equals(KuudraUtil.phase.Lair)) {
                String health = Utils.format("§e{}M", Utils.formatDecimal(getTrueHealth(kuudra.getHealth()) * 0.000001));
                if (dps.value()) {
                    health = Utils.format("{} ({}M DPS)", health, Utils.formatDecimal(calculateDPS() * 20 * 0.000001));
                }
                HudManager.bossHealthElement.setHealth(health);
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.phase.Lair) && dps.value()) {
            MagmaCubeEntity kuudra = KuudraUtil.getKuudraEntity();
            if (kuudra == null || Utils.isInstanceOver()) return;
            float health = getTrueHealth(kuudra.getHealth());
            float damage = Math.max(0, previousHealth - health);
            dpsData.add(damage);
            if (dpsData.size() > 20) {
                dpsData.removeFirst();
            }
            previousHealth = health;
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        previousHealth = 0.0f;
        dpsData.clear();
    }
}
