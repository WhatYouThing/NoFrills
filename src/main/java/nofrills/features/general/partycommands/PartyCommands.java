package nofrills.features.general.partycommands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundEvents;
import nofrills.config.*;
import nofrills.events.PartyChatMsgEvent;
import nofrills.events.WorldTickEvent;
import nofrills.hud.HudManager;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.List;

public class PartyCommands {
    public static final Feature instance = new Feature("partyCommands");

    public static final SettingString prefixes = new SettingString("! ?", "prefixes", instance.key());
    public static final SettingBool self = new SettingBool(false, "self", instance.key());
    public static final SettingJson lists = new SettingJson(new JsonObject(), "lists", instance.key());
    public static final SettingEnum<Behavior> warp = new SettingEnum<>(Behavior.Disabled, Behavior.class, "warp", instance.key());
    public static final SettingEnum<Behavior> transfer = new SettingEnum<>(Behavior.Disabled, Behavior.class, "transfer", instance.key());
    public static final SettingEnum<Behavior> allinv = new SettingEnum<>(Behavior.Disabled, Behavior.class, "allinv", instance.key());
    public static final SettingBool downtime = new SettingBool(false, "dt", instance.key());
    public static final SettingEnum<Behavior> queue = new SettingEnum<>(Behavior.Disabled, Behavior.class, "queue", instance.key());
    public static final SettingEnum<Behavior> coords = new SettingEnum<>(Behavior.Disabled, Behavior.class, "coords", instance.key());
    public static final SettingEnum<Behavior> kick = new SettingEnum<>(Behavior.Disabled, Behavior.class, "kick", instance.key());

    private static final List<Command> commands = List.of(
            new WarpCommand(),
            new TransferCommand(),
            new AllInviteCommand(),
            new DowntimeCommand(),
            new QueueCommand(),
            new CoordsCommand(),
            new KickCommand()
    );
    private static boolean downtimeNeeded = false;

    public static void setDowntimeNeeded() {
        downtimeNeeded = true;
    }

    public static String listInstancesFormatted() {
        StringBuilder builder = new StringBuilder();
        for (SkyblockData.InstanceType instanceType : SkyblockData.instances) {
            builder.append(Utils.format("\n!{} - {}", instanceType.name, Utils.uppercaseFirst(Utils.toLower(instanceType.type), true)));
        }
        return builder.toString();
    }

    public static boolean isOnList(String sender, String list) {
        if (lists.value().has(list)) {
            return lists.value().getAsJsonArray(list).contains(new JsonPrimitive(sender));
        }
        return false;
    }

    public static boolean isListEmpty(String list) {
        if (!lists.value().has(list)) {
            lists.value().add(list, new JsonArray());
        }
        return lists.value().get(list).getAsJsonArray().isEmpty();
    }

    public static void addToList(String name, String list) {
        lists.edit(object -> {
            if (!object.has(list)) {
                object.add(list, new JsonArray());
            }
            object.get(list).getAsJsonArray().add(name);
        });
    }

    public static void removeFromList(String name, String list) {
        lists.edit(object -> {
            if (object.has(list)) {
                object.get(list).getAsJsonArray().remove(new JsonPrimitive(name));
            }
        });
    }

    @EventHandler
    private static void onPartyMessage(PartyChatMsgEvent event) {
        if (instance.isActive() && !prefixes.value().isEmpty()) {
            String msg = Utils.toLower(event.message);
            String author = Utils.toLower(event.sender);
            if ((!self.value() && event.self) || isOnList(author, "blacklist")) {
                return;
            }
            for (String prefix : Utils.toLower(prefixes.value()).split(" ")) {
                if (msg.startsWith(prefix)) {
                    String content = msg.replace(prefix, "");
                    String name = content.split(" ")[0];
                    for (Command command : commands) {
                        if (command.isActive() && command.names.contains(name)) {
                            if (command.process(author, content, event.self || isOnList(author, "whitelist"))) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && downtimeNeeded && Utils.isInstanceOver()) {
            HudManager.setCustomTitle("§6Downtime", 60);
            Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.0f);
            downtimeNeeded = false;
        }
    }

    public enum Behavior {
        Automatic,
        Manual,
        Ignore,
        Disabled
    }
}
