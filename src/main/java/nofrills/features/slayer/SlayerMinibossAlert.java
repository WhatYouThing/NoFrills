package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingSound;
import nofrills.config.SettingTitle;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EventListener;
import nofrills.misc.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventListener
public class SlayerMinibossAlert {
    public static final Feature instance = new Feature("slayerMinibossAlert");

    public static final SettingTitle title = new SettingTitle("{name}", 0, 40, 0, false, "title", instance);
    public static final SettingSound sound = new SettingSound(SoundEvents.NOTE_BLOCK_PLING, 1.0, 0.0, "sound", instance);

    private static final Pattern pattern = Pattern.compile("SLAYER MINIBOSS! The (?<name>.*) spawned!");

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive()) {
            Matcher matcher = pattern.matcher(event.msg());
            if (!matcher.matches()) return;
            String name = matcher.group("name");
            Utils.getStyle(event.message, s -> s.trim().equals(name)).ifPresent(style -> {
                Utils.showTitle(
                        Utils.ofUserFormatted(title.title().value().replace("{name}", name)).setStyle(style),
                        Component.empty(),
                        title.fadeIn().value(),
                        title.stay().value(),
                        title.fadeOut().value(),
                        title.vanilla().value()
                );
                Utils.playSound(sound);
            });
        }
    }
}
