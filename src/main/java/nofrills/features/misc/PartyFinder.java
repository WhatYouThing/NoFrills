package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ChatMsgEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class PartyFinder {
    public static final Feature instance = new Feature("partyFinder");

    public static final SettingBool buttons = new SettingBool(false, "buttons", instance.key());

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && buttons.value() && event.messagePlain.startsWith("Party Finder >") && event.messagePlain.contains("joined")) {
            String name = event.messagePlain.replace("Party Finder >", "").trim().split(" ", 2)[0].toLowerCase();
            if (name.equalsIgnoreCase(mc.getSession().getUsername())) {
                return;
            }
            MutableText message = Text.literal("§aOptions for §6" + name + "§a: ")
                    .append(Text.literal("§b§l[COPY NAME]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard(name))))
                    .append(Text.literal(" "))
                    .append(Text.literal("§a§l[PROFILE VIEWER]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/pv " + name))))
                    .append(Text.literal(" "))
                    .append(Text.literal("§c§l[KICK]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/party kick " + name))))
                    .append(Text.literal(" "))
                    .append(Text.literal("§e§l[BLOCK]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/ignore add " + name))));
            Utils.infoRaw(message);
        }
    }
}
