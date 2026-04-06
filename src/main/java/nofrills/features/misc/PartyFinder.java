package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
            if (name.equalsIgnoreCase(mc.getSession().getUsername())) {
                return;
            }
            Optional<Style> style = Utils.getStyle(event.message, (string) -> string.equals(name));
            MutableText message = Text.literal("§7Options for ")
                    .append(Text.literal(name).setStyle(style.orElse(Style.EMPTY.withFormatting(Formatting.GRAY)))).append("§7: ")
                    .append(Text.literal("§b§l[COPY NAME]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard(name)))).append(" ")
                    .append(Text.literal("§a§l[PROFILE VIEWER]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/pv " + name)))).append(" ")
                    .append(Text.literal("§c§l[KICK]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/party kick " + name)))).append(" ")
                    .append(Text.literal("§e§l[BLOCK]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/ignore add " + name))));
            Utils.infoRaw(message);
        }
    }
}
