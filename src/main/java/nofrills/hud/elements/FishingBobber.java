package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EntityNamedEvent;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.EntityCache;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public class FishingBobber extends SimpleTextElement {
    public final SettingBool inactive = new SettingBool(false, "inactive", instance.key());
    public final SettingBool timer = new SettingBool(false, "timer", instance.key());
    public final EntityCache cache = new EntityCache();

    public int timerTicks = 0;

    public FishingBobber(String text) {
        super(Text.literal(text), new Feature("bobberElement"), "Fishing Bobber");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Hide If Inactive", this.inactive, "Hides the element if your fishing bobber is inactive."),
                new Settings.Toggle("Bobber Timer", this.timer, "Displays how long your fishing bobber has existed for, useful for Slugfish.")
        ));
        this.setDesc("Displays the fishing hologram timer, and optionally the existence time of your bobber.");
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && this.inactive.value() && !this.isBobberActive()) {
            return;
        }
        MutableText text = Text.literal("Bobber: ");
        if (this.isBobberActive()) {
            Entity hologram = this.cache.getFirst();
            if (hologram != null && hologram.hasCustomName()) {
                text.append(hologram.getName());
            } else {
                text.append("§aActive");
            }
        } else {
            text.append("§7Inactive");
        }
        if (timer.value() && this.timerTicks > 0) {
            text.append(Utils.format(" §7{}s", Utils.formatDecimal(this.timerTicks / 20.0, 1)));
        }
        this.setText(text);
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public boolean isBobberActive() {
        return mc.player != null && mc.player.fishHook != null;
    }

    public void onNamed(EntityNamedEvent event) {
        if (event.namePlain.length() != 3) return;
        if (event.namePlain.equals("!!!") || event.namePlain.indexOf(".") == 1) {
            this.cache.add(event.entity);
        }
    }

    public void onServerTick() {
        if (this.isBobberActive()) {
            this.timerTicks++;
        } else if (this.timerTicks != 0) {
            this.timerTicks = 0;
        }
    }
}
