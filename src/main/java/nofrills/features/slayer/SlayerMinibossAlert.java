package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EventListener;
import nofrills.hud.HudManager;
import nofrills.misc.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventListener
public class SlayerMinibossAlert {
    public static final Feature instance = new Feature("slayerMinibossAlert");

    private static final Pattern pattern = Pattern.compile("SLAYER MINIBOSS! The (?<name>.*) spawned!");

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive()) {
            Matcher matcher = pattern.matcher(event.msg());
            if (!matcher.matches()) return;
            String name = matcher.group("name");
            Utils.getStyle(event.message, s -> s.trim().equals(name)).ifPresent(style -> {
                HudManager.setCustomTitle(Component.literal(name).setStyle(style), 40);
                Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 0.0f);
            });
        }
    }
}
