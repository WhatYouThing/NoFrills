package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.HashMap;
import java.util.Map;

public class ShaftAnnounce {
    public static final Feature instance = new Feature("shaftAnnounce");

    public static final SettingString msg = new SettingString("/pc !pt Entered Mineshaft: {id}. Corpses: {corpses}.", "msg", instance);

    private static int ticks = 120;

    private static String getShaftId() {
        for (String line : SkyblockData.getLines()) {
            int index = line.lastIndexOf(" ");
            String id = index != -1 ? line.substring(index + 1) : "";
            if (id.length() == 6 && id.charAt(4) == '_') {
                return id;
            }
        }
        return "";
    }

    private static HashMap<String, Integer> getCorpses() {
        HashMap<String, Integer> map = new HashMap<>();
        for (String line : Utils.getTabListLines()) {
            if (line.endsWith(": LOOTED") || line.endsWith(": NOT LOOTED")) {
                String corpse = line.substring(0, line.indexOf(":"));
                map.put(corpse, map.getOrDefault(corpse, 0) + 1);
            }
        }
        return map;
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInArea("Mineshaft") && ticks == 0) {
            HashMap<String, Integer> corpses = getCorpses();
            String id = getShaftId();
            StringBuilder corpseList = new StringBuilder();
            for (Map.Entry<String, Integer> entry : corpses.entrySet()) {
                String count = Utils.format("{}x {}", entry.getValue(), entry.getKey());
                if (corpseList.isEmpty()) {
                    corpseList.append(count);
                } else {
                    corpseList.append(", ").append(count);
                }
            }
            Utils.sendMessage(msg.value()
                    .replaceAll("\\{id}", id.isEmpty() ? "Unknown ID" : id)
                    .replaceAll("\\{corpses}", corpseList.isEmpty() ? "None" : corpseList.toString())
            );
            ticks = -1;
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInArea("Mineshaft") && ticks > 0) {
            ticks--;
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        ticks = 120;
    }
}
