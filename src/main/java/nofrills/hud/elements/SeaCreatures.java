package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EntityNamedEvent;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.EntityCache;
import nofrills.misc.SeaCreatureData;
import nofrills.misc.Utils;

import java.util.List;

public final class SeaCreatures extends SimpleTextElement {
    public final SettingBool zero = new SettingBool(false, "zero", instance);
    private final EntityCache cache = new EntityCache();

    public SeaCreatures(String text) {
        super(Text.literal(text), new Feature("seaCreaturesElement"), "Sea Creatures");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Hide If Zero", zero, "Hides the element if there are 0 sea creatures nearby.")
        ));
        this.setDesc("Displays the amount of nearby sea creatures.");
        this.setCategory(Category.Fishing);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud()) {
            if (zero.value() && this.cache.empty()) return;
        }
        if (this.cache.size() > 0) {
            this.setText(Utils.format("Sea Creatures: §f{}", this.cache.size()));
        } else {
            this.setText("Sea Creatures: §70");
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public void onNamed(EntityNamedEvent event) {
        if (SeaCreatureData.isSeaCreature(event.namePlain) && !Utils.isInDungeons()) {
            this.cache.add(event.entity);
        }
    }
}
