package nofrills.features.general;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundEvents;
import nofrills.config.*;
import nofrills.events.PartyChatMsgEvent;
import nofrills.events.WorldTickEvent;
import nofrills.features.misc.AutoRequeue;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class PartyCommands {
    public static final Feature instance = new Feature("partyCommands");

    public static final SettingString prefixes = new SettingString("! ?", "prefixes", instance.key());
    public static final SettingBool self = new SettingBool(false, "self", instance.key());
    public static final SettingJson lists = new SettingJson(new JsonObject(), "lists", instance.key());
    public static final SettingEnum<Behavior> warp = new SettingEnum<>(Behavior.Disabled, Behavior.class, "warp", instance.key());
    public static final SettingEnum<Behavior> transfer = new SettingEnum<>(Behavior.Disabled, Behavior.class, "transfer", instance.key());
    public static final SettingEnum<Behavior> allinv = new SettingEnum<>(Behavior.Disabled, Behavior.class, "allinv", instance.key());
    public static final SettingEnum<Behavior> downtime = new SettingEnum<>(Behavior.Disabled, Behavior.class, "downtime", instance.key());
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

    private static void showDowntimeReminder() {
        Utils.showTitle("§6§lDOWNTIME", "", 5, 60, 5);
        Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.0f);
        downtimeNeeded = false;
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
            for (String prefix : prefixes.value().split(" ")) {
                if (msg.startsWith(Utils.toLower(prefix))) {
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
            showDowntimeReminder();
        }
    }

    public enum Behavior {
        Automatic,
        Manual,
        Ignore,
        Disabled
    }

    public static class Command {
        public SettingEnum<Behavior> behavior;
        public HashSet<String> names;

        public Command(SettingEnum<Behavior> behavior, List<String> names) {
            this.behavior = behavior;
            this.names = Sets.newHashSet(names);
        }

        public Command(SettingEnum<Behavior> behavior, String... names) {
            this(behavior, List.of(names));
        }

        public boolean isActive() {
            return !this.behavior.value().equals(Behavior.Disabled);
        }

        public boolean process(String author, String content, boolean whitelisted) {
            if (whitelisted || this.behavior.value().equals(Behavior.Automatic)) {
                this.onWhitelisted(author, content);
                return true;
            }
            if (this.behavior.value().equals(Behavior.Manual)) {
                this.onManual(author, content);
                return true;
            }
            if (this.behavior.value().equals(Behavior.Ignore)) {
                this.onIgnored(author, content);
                return true;
            }
            return false;
        }

        public void onWhitelisted(String author, String msg) {
        }

        public void onManual(String author, String msg) {
        }

        public void onIgnored(String author, String msg) {
        }
    }

    public static class WarpCommand extends Command {

        public WarpCommand() {
            super(warp, "warp", "w");
        }

        @Override
        public void onWhitelisted(String author, String msg) {
            Utils.sendMessage("/party warp");
        }

        @Override
        public void onManual(String author, String msg) {
            Utils.infoButton("§aClick here to warp your party.", "/party warp");
        }
    }

    public static class TransferCommand extends Command {

        public TransferCommand() {
            super(transfer, "pt", "ptme");
        }

        @Override
        public void onWhitelisted(String author, String msg) {
            Utils.sendMessage("/party transfer " + author);
        }

        @Override
        public void onManual(String author, String msg) {
            Utils.infoButton("§aClick here to promote " + author + " as leader.", "/party transfer " + author);
        }
    }

    public static class AllInviteCommand extends Command {

        public AllInviteCommand() {
            super(allinv, "allinv");
        }

        @Override
        public void onWhitelisted(String author, String msg) {
            Utils.sendMessage("/party settings allinvite");
        }

        @Override
        public void onManual(String author, String msg) {
            Utils.infoButton("§aClick here to toggle all invite.", "/party settings allinvite");
        }
    }

    public static class DowntimeCommand extends Command {

        public DowntimeCommand() {
            super(downtime, "dt");
        }

        @Override
        public void onWhitelisted(String author, String msg) {
            if (SkyblockData.isInInstance()) {
                if (!Utils.isInstanceOver()) {
                    Utils.info("§aScheduled downtime reminder.");
                    downtimeNeeded = true;
                }
                if (AutoRequeue.instance.isActive()) {
                    AutoRequeue.setPaused();
                }
            } else {
                showDowntimeReminder();
            }
        }
    }

    public static class QueueCommand extends Command {

        public QueueCommand() {
            super(queue, SkyblockData.instances.stream().map(type -> type.name).toList());
        }

        public Optional<SkyblockData.InstanceType> getType(String msg) {
            for (SkyblockData.InstanceType type : SkyblockData.instances) {
                if (msg.startsWith(type.name)) {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }

        @Override
        public void onWhitelisted(String author, String msg) {
            this.getType(msg).ifPresent(type -> Utils.sendMessage("/joininstance " + type.type));
        }

        @Override
        public void onManual(String author, String msg) {
            this.getType(msg).ifPresent(type -> Utils.infoButton(
                    "§aClick here to queue for " + Utils.uppercaseFirst(Utils.toLower(type.type), true) + ".",
                    "/joininstance " + type.type)
            );
        }
    }

    public static class CoordsCommand extends Command {

        public CoordsCommand() {
            super(coords, "coords");
        }

        @Override
        public void onWhitelisted(String author, String msg) {
            Utils.sendMessage("/pc " + Utils.getCoordsFormatted("x: {}, y: {}, z: {}"));
        }

        @Override
        public void onManual(String author, String msg) {
            Utils.infoButton("§aClick here to send your coordinates.", "/pc " + Utils.getCoordsFormatted("x: {}, y: {}, z: {}"));
        }
    }

    public static class KickCommand extends Command {

        public KickCommand() {
            super(kick, "kick", "k");
        }

        public Optional<String> getTarget(String msg) {
            String[] parts = msg.split(" ");
            if (parts.length > 1) {
                return Optional.of(parts[1]);
            }
            return Optional.empty();
        }

        @Override
        public void onWhitelisted(String author, String msg) {
            this.getTarget(msg).ifPresent(target -> Utils.sendMessage(Utils.format("/party kick {}", target)));
        }

        @Override
        public void onManual(String author, String msg) {
            this.getTarget(msg).ifPresent(target -> Utils.infoButton(
                    "§aClick here to kick " + target + ".",
                    Utils.format("/party kick {}", target))
            );
        }
    }
}
