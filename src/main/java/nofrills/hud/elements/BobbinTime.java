package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.projectile.FishingHook;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EntityUpdatedEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.settings.Toggle;
import nofrills.misc.EntityCache;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public final class BobbinTime extends SimpleTextElement implements ListeningHudElement {
    public final SettingBool none = new SettingBool(false, "none", instance);
    public final EntityCache cache = new EntityCache();

    public BobbinTime() {
        super(Component.literal("Bobbin Time: §70"), new Feature("bobbinTimeElement"), "Bobbin Time");
        this.options = this.getBaseSettings(List.of(
                new Toggle("Hide If None", this.none, "Hides the element if there are no bobbers nearby.")
        ));
        this.setDesc("Displays the amount of bobbers counted towards the Bobbin Time enchant.");
        this.setCategory(Category.Fishing);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && this.none.value() && this.getBobbers().isEmpty()) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public void onEntityUpdated(EntityUpdatedEvent event) {
        if (event.entity instanceof FishingHook) {
            cache.add(event.entity);
        }
    }

    @Override
    public void onClientTick() {
        List<FishingHook> bobbers = this.getBobbers();
        MutableComponent text = Component.literal("Bobbin Time: ")
                .append(Component.literal(String.valueOf(bobbers.size())).withStyle(bobbers.isEmpty() ? ChatFormatting.GRAY : ChatFormatting.GREEN));
        this.setText(text);
    }

    private List<FishingHook> getBobbers() {
        if (mc.player != null) {
            return this.cache.get().stream()
                    .map(e -> (FishingHook) e)
                    .filter(e -> e.getPlayerOwner() != null && Utils.isPlayer(e.getPlayerOwner()) && e.distanceTo(mc.player) < 30.0)
                    .toList();
        }
        return List.of();
    }
}
