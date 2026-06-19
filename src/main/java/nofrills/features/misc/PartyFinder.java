package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ChatMsgEvent;
import nofrills.misc.Utils;

import java.util.Optional;

import static nofrills.Main.mc;

public class PartyFinder {
    public static final Feature instance = new Feature("partyFinder");

    public static final SettingBool buttons = new SettingBool(false, "buttons", instance.key());

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && buttons.value() && event.msg().startsWith("Party Finder >") && event.msg().contains("joined")) {
            String name = event.msg().replace("Party Finder >", "").trim().split(" ", 2)[0];
            if (name.equalsIgnoreCase(mc.player.getName().getString())) {
                return;
            }
            Optional<Style> style = Utils.getStyle(event.message, (string) -> string.trim().startsWith(name));
            MutableComponent message = Component.literal("§7Options for ")
                    .append(Component.literal(name).setStyle(style.orElse(Style.EMPTY.applyFormat(ChatFormatting.GRAY)))).append("§7: ")
                    .append(Component.literal("§b§l[COPY NAME]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard(name)))).append(" ")
                    .append(Component.literal("§a§l[PROFILE VIEWER]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/pv " + name)))).append(" ")
                    .append(Component.literal("§c§l[KICK]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/party kick " + name)))).append(" ")
                    .append(Component.literal("§e§l[BLOCK]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/ignore add " + name))));
            Utils.infoRaw(message);
        }
    }
}
