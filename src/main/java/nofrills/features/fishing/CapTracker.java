package nofrills.features.fishing;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingInt;
import nofrills.config.SettingString;
import nofrills.events.EntityNamedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.SeaCreatureData;
import nofrills.misc.Utils;

public class CapTracker {
    public static final Feature instance = new Feature("capTracker");

    public static final SettingInt target = new SettingInt(50, "target", instance.key());
    public static final SettingBool title = new SettingBool(false, "title", instance.key());
    public static final SettingBool sound = new SettingBool(false, "sound", instance.key());
    public static final SettingBool sendMsg = new SettingBool(false, "send", instance.key());
    public static final SettingString msg = new SettingString("/pc SEA CREATURE CAP REACHED", "msg", instance.key());
    public static final SettingInt delay = new SettingInt(30, "delay", instance.key());

    public static final EntityCache seaCreatures = new EntityCache();
    private static int notifyTicks = 0;

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            seaCreatures.removeDead();
            int count = seaCreatures.size();
            if (count >= target.value() && notifyTicks == 0) {
                if (sendMsg.value() && !msg.value().isEmpty()) {
                    Utils.sendMessage(msg.value());
                }
                if (sound.value()) {
                    Utils.playSound(SoundEvents.ITEM_TRIDENT_RETURN, SoundCategory.MASTER, 3, 1);
                }
                if (title.value()) {
                    Utils.showTitle("§4§lCAP REACHED", "§8§l" + count + " Sea Creatures", 5, 20, 5);
                }
                notifyTicks = delay.value() * 20;
            }
            if (notifyTicks > 0) {
                notifyTicks--;
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && !Utils.isInDungeons() && SeaCreatureData.isSeaCreature(event.namePlain)) {
            seaCreatures.add(event.entity);
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        seaCreatures.clear();
    }
}
