package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import nofrills.config.Feature;
import nofrills.events.ReceivePacketEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public final class Day extends SimpleTextElement implements ListeningHudElement {
    private long day = 0;
    private boolean dirty = false;

    public Day() {
        super(Component.literal("Day: §f0"), new Feature("dayElement"), "Day Display");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the day that the server world is on.");
        this.setCategory(Category.Info);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            if (this.dirty) {
                this.setText(Utils.format("Day: §f{}", this.day));
                this.dirty = false;
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof ClientboundSetTimePacket timePacket && mc.level != null) {
            mc.level.dimensionType().defaultClock().ifPresent(clock -> {
                if (timePacket.clockUpdates().containsKey(clock)) {
                    this.day = timePacket.clockUpdates().get(clock).totalTicks() / 24000L;
                    this.dirty = true;
                }
            });
        }
    }
}
