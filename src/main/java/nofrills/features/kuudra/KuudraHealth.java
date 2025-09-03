package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.mob.MagmaCubeEntity;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.KuudraUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import nofrills.mixin.BossBarHudAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static nofrills.Main.mc;

public class KuudraHealth {
    public static final Feature instance = new Feature("kuudraHealth");

    public static final SettingBool dps = new SettingBool(false, "dps", instance.key());
    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0xffff00), "color", instance.key());

    private static final List<Float> dpsData = new ArrayList<>();
    private static float previousHealth = 0.0f;

    private static float getTrueHealth(float health) {
        return (health - 1024.0f) * 10000.0f;
    }

    private static float calculateDPS() {
        float total = 0.0f;
        for (float damage : dpsData) {
            total += damage;
        }
        return total / dpsData.size();
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInKuudra()) {
            MagmaCubeEntity kuudra = KuudraUtil.getKuudraEntity();
            if (kuudra == null || !kuudra.isAlive()) {
                if (KuudraUtil.getCurrentPhase() == KuudraUtil.phase.DPS) {
                    Collection<ClientBossBar> bossBars = ((BossBarHudAccessor) mc.inGameHud.getBossBarHud()).getBossBars().values();
                    if (!bossBars.isEmpty()) {
                        float health = ((ClientBossBar) bossBars.toArray()[0]).getPercent();
                        Utils.showTitleCustom(Utils.format("KUUDRA: {}% HP", Utils.formatDecimal(health * 100)), 1, 25, 2.5f, color.value());
                    }
                }
            } else {
                if (KuudraUtil.getCurrentPhase() == KuudraUtil.phase.DPS) {
                    float health = kuudra.getHealth() / kuudra.getMaxHealth();
                    Utils.showTitleCustom(Utils.format("KUUDRA: {}% HP", Utils.formatDecimal(health)), 1, 25, 2.5f, color.value());
                }
                if (dps.value() && KuudraUtil.getCurrentPhase() == KuudraUtil.phase.Lair && !Utils.isInstanceOver()) {
                    Utils.showTitleCustom(Utils.format("DPS: {}M", Utils.formatDecimal(calculateDPS() * 20 * 0.000001)), 1, 25, 2.5f, color.value());
                }
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && dps.value()) {
            if (KuudraUtil.getCurrentPhase() == KuudraUtil.phase.Lair && !Utils.isInstanceOver()) {
                float health = getTrueHealth(KuudraUtil.getKuudraEntity().getHealth());
                float damage = Math.clamp(previousHealth - health, 0, 240_000_000);
                dpsData.add(damage);
                if (dpsData.size() > 20) {
                    dpsData.removeFirst();
                }
                previousHealth = health;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        previousHealth = 0.0f;
        dpsData.clear();
    }
}
