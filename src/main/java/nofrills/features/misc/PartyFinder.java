package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
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
            String name = Utils.toLower(event.messagePlain.replace("Party Finder >", "").trim().split(" ", 2)[0]);
            if (name.equalsIgnoreCase(mc.getUser().getName())) {
                return;
            }
            MutableComponent message = Component.literal("§aOptions for §6" + name + "§a: ")
                    .append(Component.literal("§b§l[COPY NAME]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard(name))))
                    .append(Component.literal(" "))
                    .append(Component.literal("§a§l[PROFILE VIEWER]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/pv " + name))))
                    .append(Component.literal(" "))
                    .append(Component.literal("§c§l[KICK]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/party kick " + name))))
                    .append(Component.literal(" "))
                    .append(Component.literal("§e§l[BLOCK]").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/ignore add " + name))));
            Utils.infoRaw(message);
        }
    }
}
