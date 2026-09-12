package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.DungeonUtil;
import nofrills.misc.KuudraUtil;
import nofrills.misc.Utils;

import java.util.List;
import java.util.Optional;

public final class BossHealth extends SimpleTextElement implements ListeningHudElement {
    private final SettingBool dungeon = new SettingBool(true, "dungeon", this.instance);
    private final SettingBool kuudra = new SettingBool(true, "kuudra", this.instance);
    private int kuudraTicks = 0;
    private float kuudraHealth = 0.0f;
    private float kuudraDPS = 0.0f;

    public BossHealth() {
        super(Component.literal("Boss Health: N/A"), new Feature("bossHealthElement"), "Boss Health");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Dungeon", this.dungeon, "If enabled, the health of the dungeon bosses is displayed."),
                new Settings.Toggle("Kuudra", this.kuudra, "If enabled, the health of Kuudra is displayed.")
        ));
        this.setDesc("Displays the health of Kuudra and/or the Catacombs bosses.");
        this.setCategory(Category.Info);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && this.text == this.defaultText) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public void onClientTick() {
        List<LerpingBossEvent> bossBars = Utils.getBossBars();
        if (bossBars.isEmpty()) {
            this.setDefaultText();
            return;
        }
        LerpingBossEvent bar = bossBars.getFirst();
        if (dungeon.value() && Utils.isInDungeons() && !Utils.isInstanceOver()) {
            String name = Utils.toPlain(bar.getName());
            if (DungeonUtil.isInBossRoom() || name.contains("The Watcher")) {
                this.setText(Component.literal(name)
                        .append(": ")
                        .append(Component.literal(Utils.formatDecimal(bar.getProgress() * 100.0) + "%")
                                .withColor(Utils.getPercentageColor(bar.getProgress()).getHex())
                        ));
                return;
            }
        }
        if (kuudra.value() && Utils.isInKuudra() && !Utils.isInstanceOver()) {
            Optional<Float> health = KuudraUtil.getKuudraHealth();
            KuudraUtil.Phase phase = KuudraUtil.getCurrentPhase();
            if (phase.equals(KuudraUtil.Phase.DPS)) {
                float percent = health.map(value -> value / 100000.0f).orElseGet(bar::getProgress);
                this.setText(Component.literal("Kuudra")
                        .append(": ")
                        .append(Component.literal(Utils.formatDecimal(percent * 100.0) + "%")
                                .withColor(Utils.getPercentageColor(percent).getHex())
                        ));
                return;
            }
            if (phase.equals(KuudraUtil.Phase.Lair) && health.isPresent()) {
                float currentHealth = health.get();
                this.kuudraTicks++;
                if (this.kuudraTicks >= 20) {
                    this.kuudraDPS = Math.max(0, this.kuudraHealth - currentHealth);
                    this.kuudraHealth = currentHealth;
                    this.kuudraTicks = 0;
                }
                this.setText(Component.literal("Kuudra")
                        .append(": ")
                        .append(Component.literal(Utils.formatDecimal(currentHealth * 0.000001) + "M")
                                .withColor(Utils.getPercentageColor(currentHealth / 240_000_000.0f).getHex())
                        )
                        .append(" ")
                        .append(Component.literal("(" + Utils.formatDecimal(this.kuudraDPS * 0.000001) + "M DPS)")
                                .withStyle(ChatFormatting.GRAY)
                        )
                );
                return;
            }
        }
        this.setDefaultText();
    }

    @Override
    public void onServerJoin() {
        this.setDefaultText();
        this.kuudraTicks = 0;
        this.kuudraHealth = 0.0f;
        this.kuudraDPS = 0.0f;
    }
}
