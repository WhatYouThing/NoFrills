package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.features.dungeons.ScoreCalculator;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;

public class DungeonScore extends SimpleTextElement {

    public DungeonScore() {
        super(Text.literal("Score: §fN/A"), new Feature("dungeonScoreElement"), "Dungeon Score Element");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the current dungeon score. Used by the Score Calculator feature.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && (!Utils.isInDungeons() || !DungeonUtil.isDungeonStarted())) {
            return;
        }
        int score = ScoreCalculator.getScore();
        if (score > 0) {
            int color = this.getScoreColor(score);
            String label = this.getScoreLabel(score);
            MutableText text = Text.literal(Utils.format("{}{}§r §7({})", score >= 300 ? "§l" : "", score, label)).withColor(color);
            this.setText(Text.literal("Score: ").append(text));
        } else {
            this.setDefaultText();
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public int getScoreColor(int score) {
        if (score < 100) return 0xfc3e1c;
        if (score < 160) return 0x35a0fd;
        if (score < 230) return 0x90f35d;
        if (score < 270) return 0xbe1aff;
        return 0xffce1a;
    }

    public String getScoreLabel(int score) {
        if (score < 100) return "D";
        if (score < 160) return "C";
        if (score < 230) return "B";
        if (score < 270) return "A";
        if (score < 300) return "S";
        return "S+";
    }
}
