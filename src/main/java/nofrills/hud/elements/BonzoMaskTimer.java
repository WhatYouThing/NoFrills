package nofrills.hud.elements;

import net.minecraft.world.item.ItemStack;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.TimerElement;
import nofrills.misc.Utils;

import java.util.Optional;

import static nofrills.Main.mc;

public final class BonzoMaskTimer extends TimerElement implements ListeningHudElement {

    public BonzoMaskTimer() {
        super("Bonzo Mask: {}", new Feature("bonzoMaskTimerElement"), "Bonzo Mask Timer");
        this.options = this.getBaseSettings();
        this.setDesc("Displays the cooldown of the Bonzo's Mask Clownin' Around ability.");
        this.setAutoPause();
        this.setCategory(Category.Dungeons);
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        if (event.msg().replace(Utils.Symbols.starredItem + " ", "").equals("Your Bonzo's Mask saved your life!")) {
            ItemStack helmet = Utils.getEntityHelmet(mc.player);
            Optional<String> line = Utils.getLoreLines(helmet).stream().filter(l -> l.startsWith("Cooldown: ")).findFirst();
            if (line.isPresent()) {
                String cooldown = line.get();
                String duration = cooldown.substring(cooldown.indexOf(":") + 2).replace("s", "");
                this.start((long) Math.ceil(Utils.parseDouble(duration).orElse(180.0) * 1000));
            } else {
                this.start(180000);
            }
        }
    }
}
