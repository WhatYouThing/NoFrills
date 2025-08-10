package nofrills.features.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.*;
import nofrills.events.PartyChatMsgEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

public class PartyCommands {
    public static final Feature instance = new Feature("partyCommands");

    public static final SettingString prefixes = new SettingString("! ?", "prefixes", instance.key());
    public static final SettingBool self = new SettingBool(false, "self", instance.key());
    public static final SettingJson lists = new SettingJson(new JsonObject(), "lists", instance.key());
    public static final SettingEnum<behavior> warp = new SettingEnum<>(behavior.Disabled, behavior.class, "warp", instance.key());
    public static final SettingEnum<behavior> transfer = new SettingEnum<>(behavior.Disabled, behavior.class, "transfer", instance.key());
    public static final SettingEnum<behavior> allinv = new SettingEnum<>(behavior.Disabled, behavior.class, "allinv", instance.key());
    public static final SettingEnum<behavior> downtime = new SettingEnum<>(behavior.Disabled, behavior.class, "downtime", instance.key());
    public static final SettingEnum<behavior> queue = new SettingEnum<>(behavior.Disabled, behavior.class, "queue", instance.key());
    public static final SettingEnum<behavior> coords = new SettingEnum<>(behavior.Disabled, behavior.class, "coords", instance.key());

    private static boolean downtimeNeeded = false;

    private static void showDowntimeReminder() {
        Utils.showTitle("§6§lDOWNTIME", "", 5, 60, 5);
        Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.MASTER, 1.0f, 0.0f);
        downtimeNeeded = false;
    }

    public static String listInstancesFormatted() {
        StringBuilder builder = new StringBuilder();
        for (SkyblockData.InstanceType instanceType : SkyblockData.instances) {
            builder.append(Utils.format("\n!{} - {}", instanceType.name, Utils.uppercaseFirst(instanceType.type.toLowerCase(), true)));
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
        if (!lists.value().has(list)) {
            lists.value().add(list, new JsonArray());
        }
        lists.value().get(list).getAsJsonArray().add(name);
    }

    public static void removeFromList(String name, String list) {
        if (lists.value().has(list)) {
            lists.value().get(list).getAsJsonArray().remove(new JsonPrimitive(name));
        }
    }

    @EventHandler
    private static void onPartyMessage(PartyChatMsgEvent event) {
        if (instance.isActive() && !prefixes.value().isEmpty()) {
            if (!self.value() && event.self) {
                return;
            }
            String msg = event.message.toLowerCase();
            String author = event.sender.toLowerCase();
            for (String prefix : prefixes.value().split(" ")) {
                if (msg.startsWith(prefix.toLowerCase())) {
                    if (isOnList(author, "blacklist")) {
                        return;
                    }
                    boolean whitelisted = event.self || isOnList(author, "whitelist");
                    String command = msg.replace(prefix, "");
                    if (!warp.value().equals(behavior.Disabled) && command.startsWith("warp")) {
                        if (whitelisted || warp.value().equals(behavior.Automatic)) {
                            Utils.sendMessage("/party warp");
                        } else if (!warp.value().equals(behavior.Ignore)) {
                            Utils.infoButton("§aClick here to warp your party.", "/party warp");
                        }
                    }
                    if (!transfer.value().equals(behavior.Disabled) && command.startsWith("pt")) {
                        if (whitelisted || transfer.value().equals(behavior.Automatic)) {
                            Utils.sendMessage("/party transfer " + author);
                        } else if (!transfer.value().equals(behavior.Ignore)) {
                            Utils.infoButton("§aClick here to promote §6" + event.sender + " §aas leader.", "/party transfer " + author);
                        }
                    }
                    if (!allinv.value().equals(behavior.Disabled) && command.startsWith("allinv")) {
                        if (whitelisted || allinv.value().equals(behavior.Automatic)) {
                            Utils.sendMessage("/party settings allinvite");
                        } else if (!allinv.value().equals(behavior.Ignore)) {
                            Utils.infoButton("§aClick here to toggle all invite.", "/party settings allinvite");
                        }
                    }
                    if (!downtime.value().equals(behavior.Disabled) && command.startsWith("dt")) {
                        if (whitelisted || downtime.value().equals(behavior.Automatic)) {
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
                    if (!queue.value().equals(behavior.Disabled)) {
                        for (SkyblockData.InstanceType instance : SkyblockData.instances) {
                            if (command.equals(instance.name)) {
                                if (whitelisted || queue.value().equals(behavior.Automatic)) {
                                    Utils.sendMessage("/joininstance " + instance.type);
                                } else if (!queue.value().equals(behavior.Ignore)) {
                                    Utils.infoButton("§aClick to queue for §6" + Utils.uppercaseFirst(instance.type.toLowerCase(), true) + "§a.", "/joininstance " + instance.type);
                                }
                                break;
                            }
                        }
                    }
                    if (!coords.value().equals(behavior.Disabled) && command.startsWith("coords")) {
                        if (whitelisted || coords.value().equals(behavior.Automatic)) {
                            Utils.sendMessage("/pc " + Utils.getCoordsFormatted("x: {x}, y: {y}, z: {z}"));
                        } else if (!coords.value().equals(behavior.Ignore)) {
                            Utils.infoButton("§aClick here to send your coordinates.", "/pc " + Utils.getCoordsFormatted("x: {x}, y: {y}, z: {z}"));
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

    public enum behavior {
        Automatic,
        Manual,
        Ignore,
        Disabled
    }
}
