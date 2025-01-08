package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.PartyChatMsgEvent;
import nofrills.events.ScoreboardUpdateEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class PartyFeatures {
    private static final Pattern partyFinderPattern = Pattern.compile("Party Finder > .* joined .*");
    private static boolean downtimeNeeded = false;

    @EventHandler
    public static void onMessage(ChatMsgEvent event) {
        String msg = event.getPlainMessage();
        if (Config.partyFinderOptions && msg.startsWith("Party Finder >") && partyFinderPattern.matcher(msg).matches()) {
            String name = msg.replace("Party Finder >", "").trim().split(" ", 2)[0].toLowerCase();
            if (name.equalsIgnoreCase(mc.getSession().getUsername())) {
                return;
            }
            ClickEvent copyName = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, name);
            ClickEvent kick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party kick " + name);
            Text message = Text.literal("§aOptions for §6" + name + "§a: ")
                    .append(Text.literal("§b[COPY NAME]").setStyle(Style.EMPTY.withClickEvent(copyName)))
                    .append(Text.literal(" "))
                    .append(Text.literal("§c[KICK]").setStyle(Style.EMPTY.withClickEvent(kick)));
            Utils.infoRaw(message);
        }
    }

    @EventHandler
    public static void onPartyMessage(PartyChatMsgEvent event) {
        if (!Config.partyPrefixes.isEmpty() && !event.self) {
            String msg = event.message.toLowerCase();
            String author = event.sender.toLowerCase();
            for (String prefix : Config.partyPrefixes.split(" ")) {
                if (msg.startsWith(prefix.toLowerCase())) {
                    Config.partyBehaviorList privilege = Config.partyBehavior;
                    if (Config.partyWhitelist.contains(author)) {
                        privilege = Config.partyBehaviorList.Automatic;
                    }
                    if (Config.partyBlacklist.contains(author) || privilege == Config.partyBehaviorList.Ignore) {
                        return;
                    }
                    String command = msg.replace(prefix, "");
                    if (Config.partyCmdWarp && command.startsWith("warp")) {
                        if (privilege == Config.partyBehaviorList.Manual) {
                            Utils.infoButton("§aClick here to warp your party.", "/party warp");
                        } else {
                            Utils.sendMessage("/party warp");
                        }
                    }
                    if (Config.partyCmdTransfer && command.startsWith("ptme")) {
                        if (privilege == Config.partyBehaviorList.Manual) {
                            Utils.infoButton("§aClick here to promote §6" + event.sender + " §aas leader.", "/party transfer " + author);
                        } else {
                            Utils.sendMessage("/party transfer " + author);
                        }
                    }
                    if (Config.partyCmdTransfer && command.startsWith("allinv")) {
                        if (privilege == Config.partyBehaviorList.Manual) {
                            Utils.infoButton("§aClick here to toggle all invite.", "/party settings allinvite");
                        } else {
                            Utils.sendMessage("/party settings allinvite");
                        }
                    }
                    if (Config.partyCmdDowntime && command.startsWith("dt")) {
                        if (Utils.isInDungeons() || Utils.isInKuudra()) {
                            Utils.info("§aScheduled downtime reminder for §6" + event.sender + "§a.");
                            downtimeNeeded = true;
                        } else {
                            Utils.showTitle("§6DOWNTIME NEEDED", "", 5, 20, 5);
                        }
                    }
                    if (Config.partyCmdQueue) {
                        for (SkyblockData.InstanceType instance : SkyblockData.instances) {
                            if (command.equals(instance.name)) {
                                if (privilege == Config.partyBehaviorList.Manual) {
                                    Utils.infoButton("§aClick here to queue for §6" + instance.type + "§a.", "/joininstance " + instance.type);
                                } else {
                                    Utils.sendMessage("/joininstance " + instance.type);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public static void onScoreboard(ScoreboardUpdateEvent event) {
        if (downtimeNeeded && Utils.isInstanceClosing()) {
            if (Utils.isInDungeons() || Utils.isInKuudra()) {
                Utils.showTitle("§6DOWNTIME NEEDED", "", 5, 60, 5);
            }
            downtimeNeeded = false;
        }
    }
}
