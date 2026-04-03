package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.DungeonUtil;
import nofrills.misc.KuudraUtil;
import nofrills.misc.Utils;

import java.util.List;

public class BossHealth extends SimpleTextElement {
    private final SettingBool dungeon = new SettingBool(true, "dungeon", this.instance);
    private final SettingBool kuudra = new SettingBool(true, "kuudra", this.instance);
    private boolean visible = false;
    private int kuudraTicks = 0;
    private float kuudraHealth = 0.0f;
    private float kuudraDPS = 0.0f;

    public BossHealth() {
        super(Text.literal("Boss Health"), new Feature("bossHealthElement"), "Boss Health");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Dungeon", this.dungeon, "If enabled, the health of the dungeon bosses is displayed."),
                new Settings.Toggle("Kuudra", this.kuudra, "If enabled, the health of Kuudra is displayed.")
        ));
        this.setDesc("Displays the health of Kuudra and/or the Catacombs bosses.");
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && !this.visible) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void update() {
        List<ClientBossBar> bossBars = Utils.getBossBars();
        if (bossBars.isEmpty()) {
            this.visible = false;
            return;
        }
        ClientBossBar bar = bossBars.getFirst();
        if (dungeon.value() && Utils.isInDungeons() && !Utils.isInstanceOver()) {
            String name = Utils.toPlain(bar.getName());
            if ((DungeonUtil.isInBossRoom() && !DungeonUtil.isInDragonPhase()) || name.equals("The Watcher")) {
                this.setHealth("§l" + name, bar.getPercent());
                return;
            }
        }
        if (kuudra.value() && Utils.isInKuudra() && !Utils.isInstanceOver()) {
            MagmaCubeEntity kuudra = KuudraUtil.getKuudraEntity();
            KuudraUtil.Phase phase = KuudraUtil.getCurrentPhase();
            if (phase.equals(KuudraUtil.Phase.DPS)) {
                if (kuudra == null) {
                    this.setHealth("§lKuudra", bar.getPercent());
                } else {
                    this.setHealth("§lKuudra", kuudra.getHealth() / kuudra.getMaxHealth());
                }
                return;
            }
            if (phase.equals(KuudraUtil.Phase.Lair)) {
                float currentHealth = kuudra == null ? 0.0f : (kuudra.getHealth() - 1024.0f) * 10000.0f;
                this.kuudraTicks++;
                if (this.kuudraTicks >= 20) {
                    this.kuudraDPS = Math.max(0, this.kuudraHealth - currentHealth);
                    this.kuudraHealth = currentHealth;
                    this.kuudraTicks = 0;
                }
                this.setHealth("§lKuudra", Utils.format("§e{}M §7({}M DPS)",
                        Utils.formatDecimal(currentHealth * 0.000001),
                        Utils.formatDecimal(this.kuudraDPS * 0.000001)
                ));
                return;
            }
        }
        this.visible = false;
    }

    public void setHealth(String label, String health) {
        this.setText(Utils.format("{}§r: {}", label, health));
        this.visible = true;
    }

    public void setHealth(String label, float percent) {
        this.setHealth(label, Utils.getPercentageColor(percent, true) + Utils.formatDecimal(percent * 100) + "%");
    }

    public void reset() {
        this.visible = false;
        this.kuudraTicks = 0;
        this.kuudraHealth = 0.0f;
        this.kuudraDPS = 0.0f;
    }
}
