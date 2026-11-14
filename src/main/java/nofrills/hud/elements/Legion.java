package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EntityUpdatedEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.EntityCache;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public final class Legion extends SimpleTextElement implements ListeningHudElement {
    public final SettingBool none = new SettingBool(false, "none", instance);
    public final EntityCache cache = new EntityCache();

    public Legion() {
        super(Component.literal("Legion: §70"), new Feature("legionElement"), "Legion");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Hide If None", this.none, "Hides the element if there are no players nearby.")
        ));
        this.setDesc("Displays the amount of players counted towards the Legion enchant.");
        this.setCategory(Category.Misc);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && this.none.value() && this.getPlayers().isEmpty()) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public void onEntityUpdated(EntityUpdatedEvent event) {
        if (event.entity instanceof Player player && Utils.isPlayer(player)) {
            cache.add(event.entity);
        }
    }

    @Override
    public void onClientTick() {
        List<Entity> players = this.getPlayers();
        MutableComponent text = Component.literal("Legion: ")
                .append(Component.literal(String.valueOf(players.size())).withStyle(players.isEmpty() ? ChatFormatting.GRAY : ChatFormatting.GREEN));
        this.setText(text);
    }

    private List<Entity> getPlayers() {
        if (mc.player != null) {
            return this.cache.get().stream()
                    .filter(e -> !e.equals(mc.player) && e.distanceTo(mc.player) < 30.0)
                    .toList();
        }
        return List.of();
    }
}
