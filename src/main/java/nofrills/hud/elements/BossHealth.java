package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.DungeonUtil;
import nofrills.misc.KuudraUtil;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.List;

public class BossHealth extends SimpleTextElement {
    private final SettingBool slayer = new SettingBool(true, "slayer", this.instance);
    private final SettingBool dungeon = new SettingBool(true, "dungeon", this.instance);
    private final SettingBool kuudra = new SettingBool(true, "kuudra", this.instance);
    private boolean visible = false;
    private int kuudraTicks = 0;
    private float kuudraHealth = 0.0f;
    private float kuudraDPS = 0.0f;

    public BossHealth() {
        super(Text.literal("Boss Health: §fN/A"), new Feature("bossHealthElement"), "Boss Health Element");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Slayer", this.slayer, "If enabled, the health of your slayer boss is displayed."),
                new Settings.Toggle("Dungeon", this.dungeon, "If enabled, the health of the dungeon bosses is displayed."),
                new Settings.Toggle("Kuudra", this.kuudra, "If enabled, the health of Kuudra is displayed.")
        ));
        this.setDesc("Allows you to display the health of your own slayer boss, the dungeon bosses, and Kuudra.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && !this.visible) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void update() {
        if (slayer.value() && SlayerUtil.bossAlive) {
            Entity nameEntity = SlayerUtil.getNameEntity();
            if (nameEntity != null) {
                String name = Utils.toPlain(nameEntity.getName()).replaceAll(Utils.Symbols.vampLow, "").trim();
                String[] parts = name.split(" ");
                if (name.endsWith("Hits") || name.endsWith("Hit")) {
                    this.setHealth(Utils.format("§d{} {}", parts[parts.length - 2], parts[parts.length - 1]));
                } else {
                    this.setHealth(Utils.format("§a{}", parts[parts.length - 1].replaceAll(Utils.Symbols.heart, "").trim()));
                }
                return;
            }
        }
        if (dungeon.value() && Utils.isInDungeons() && !Utils.isInstanceOver()) {
            if (DungeonUtil.isInBossRoom() && !DungeonUtil.isInDragonPhase()) {
                List<ClientBossBar> bossBars = Utils.getBossBars();
                if (!bossBars.isEmpty()) {
                    this.setHealth(Utils.format("§a{}%", Utils.formatDecimal(bossBars.getFirst().getPercent() * 100)));
                    return;
                }
            }
        }
        if (kuudra.value() && Utils.isInKuudra() && !Utils.isInstanceOver()) {
            MagmaCubeEntity kuudra = KuudraUtil.getKuudraEntity();
            KuudraUtil.Phase phase = KuudraUtil.getCurrentPhase();
            if (phase.equals(KuudraUtil.Phase.DPS)) {
                if (kuudra == null) {
                    List<ClientBossBar> bossBars = Utils.getBossBars();
                    if (!bossBars.isEmpty()) {
                        this.setHealth(Utils.format("§e{}%", Utils.formatDecimal(bossBars.getFirst().getPercent() * 100)));
                    }
                    return;
                }
                this.setHealth(Utils.format("§e{}%", Utils.formatDecimal(kuudra.getHealth() / kuudra.getMaxHealth())));
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
                this.setHealth(Utils.format("§e{}M ({}M DPS)",
                        Utils.formatDecimal(currentHealth * 0.000001),
                        Utils.formatDecimal(this.kuudraDPS * 0.000001)
                ));
                return;
            }
        }
        this.visible = false;
    }

    public void setHealth(String health) {
        this.setText(Utils.format("Boss Health: {}", health));
        this.visible = true;
    }

    public void reset() {
        this.visible = false;
        this.kuudraTicks = 0;
        this.kuudraHealth = 0.0f;
        this.kuudraDPS = 0.0f;
    }
}
