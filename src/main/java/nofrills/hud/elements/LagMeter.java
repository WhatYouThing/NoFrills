package nofrills.hud.elements;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import static nofrills.Main.Config;

public class LagMeter extends SimpleTextElement {
    public long lastTick = 0;

    public LagMeter(Text text, RenderColor color) {
        super(0, 0, text, color);
        this.hidden = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.setProperties(Config.lagMeterEnabled(), this.hidden, Config.lagMeterLeftHand(), Config.lagMeterPosX(), Config.lagMeterPosY());
        if (lastTick != 0) {
            long sinceTick = Util.getMeasuringTimeMs() - lastTick;
            if (sinceTick >= Config.lagMeterMinTime()) {
                this.hidden = false;
                this.setText(Utils.format("§cLast server tick was {}s ago", Utils.formatDecimal(sinceTick * 0.001)));
            } else {
                this.hidden = true;
            }
        } else {
            this.hidden = true;
        }
        super.render(context, mouseX, mouseY, delta);
        Config.lagMeterPosX(this.posX);
        Config.lagMeterPosY(this.posY);
    }

    public void setTickTime(long time) {
        this.lastTick = time;
    }
}
