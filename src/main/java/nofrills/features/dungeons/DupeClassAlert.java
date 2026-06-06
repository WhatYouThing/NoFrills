package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.WorldTickEvent;
import nofrills.hud.HudManager;
import nofrills.misc.DungeonUtil;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.HashSet;
import java.util.regex.Pattern;

public class DupeClassAlert {
    public static final Feature instance = new Feature("dupeClassAlert");

    private static final Pattern pattern = Pattern.compile("\\[[A-Z]] .*");

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && !DungeonUtil.isDungeonStarted()) {
            HashSet<String> classes = new HashSet<>();
            for (String line : SkyblockData.getLines()) {
                if (pattern.matcher(line).matches()) {
                    String prefix = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                    if (classes.contains(prefix)) {
                        HudManager.setCustomTitle("§cDupe Class", 1);
                        break;
                    }
                    classes.add(prefix);
                }
            }
        }
    }
}
