package nofrills.features.fishing;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingString;
import nofrills.events.ChatMsgEvent;
import nofrills.misc.SeaCreatureData;
import nofrills.misc.Utils;

import static nofrills.Main.mc;
import static nofrills.misc.Utils.noFrillsIndicator;

public class RareAnnounce {
    public static final Feature instance = new Feature("rareAnnounce");

    public static final SettingBool title = new SettingBool(false, "title", instance.key());
    public static final SettingBool sound = new SettingBool(false, "sound", instance.key());
    public static final SettingBool replace = new SettingBool(false, "replace", instance.key());
    public static final SettingBool sendMsg = new SettingBool(false, "send", instance.key());
    public static final SettingString msg = new SettingString("/pc {spawnmsg}", "msg", instance.key());

    @EventHandler
    private static void onChatMsg(ChatMsgEvent event) {
        if (instance.isActive() && !event.messagePlain.isEmpty() && Utils.isInSkyblock()) {
            for (SeaCreatureData.SeaCreature creature : SeaCreatureData.list) {
                if (creature.rare && event.messagePlain.equals(creature.spawnMsg)) {
                    if (title.value()) {
                        Utils.showTitle(creature.color + "§l" + Utils.toUpper(creature.name), "", 5, 20, 5);
                    }
                    if (sound.value()) {
                        Utils.playSound(SoundEvents.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, SoundCategory.MASTER, 1, 1);
                    }
                    if (sendMsg.value() && !msg.value().isEmpty()) {
                        Utils.sendMessage(msg.value().replace("{name}", creature.name).replace("{spawnmsg}", creature.spawnMsg));
                    }
                    if (replace.value()) {
                        mc.inGameHud.getChatHud().addMessage(Text.literal(creature.color + "§l" + creature.spawnMsg + "§r"), null, noFrillsIndicator);
                        event.cancel();
                    }
                    return;
                }
            }
        }
    }
}
