package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.*;
import nofrills.misc.EntityCache;
import nofrills.misc.Utils;

import java.util.regex.Pattern;

import static nofrills.Main.mc;

@EventListener
public class ScathaMining {
    public static final Feature instance = new Feature("scathaMining");

    public static final SettingBool alert = new SettingBool(false, "alert", instance.key());
    public static final SettingBool cooldown = new SettingBool(false, "cooldown", instance.key());

    private static final Pattern stonewormPattern = Pattern.compile("\\[Lv[0-9]*] .* Stoneworm [0-9]*" + Utils.Symbols.heart);
    private static final Pattern scathaPattern = Pattern.compile("\\[Lv[0-9]*] .* Scatha [0-9]*" + Utils.Symbols.heart);
    private static final EntityCache wormsCache = new EntityCache();
    private static int spawnCooldown = 0;

    private static WormType getWormType(String name) {
        if (stonewormPattern.matcher(name).matches()) return WormType.Worm;
        if (scathaPattern.matcher(name).matches()) return WormType.Scatha;
        return WormType.None;
    }

    private static boolean isWithinRadius(BlockPos wormPos) {
        if (mc.player != null) {
            BlockPos playerPos = mc.player.blockPosition();
            return Utils.difference(wormPos.getY(), playerPos.getY()) <= 4
                    && (Utils.difference(wormPos.getX(), playerPos.getX()) <= 2 || Utils.difference(wormPos.getZ(), playerPos.getZ()) <= 2);
        }
        return false;
    }

    private static void alertSpawn(WormType type) {
        if (type.equals(WormType.Scatha)) {
            Utils.showTitle("§cScatha", "", 5, 20, 5);
            Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
        } else {
            Utils.showTitle("§eWorm", "", 5, 20, 5);
            Utils.playSound(SoundEvents.NOTE_BLOCK_BASS, 1.0f, 0.0f);
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInArea("Crystal Hollows")) {
            WormType type = getWormType(event.namePlain);
            if (!type.equals(WormType.None) && !wormsCache.has(event.entity) && isWithinRadius(event.entity.blockPosition())) {
                if (alert.value()) {
                    alertSpawn(type);
                }
                wormsCache.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isInArea("Crystal Hollows") && event.messagePlain.equals("You hear the sound of something approaching...")) {
            spawnCooldown = 620; // 30s + the 1 second it takes the worm to spawn after the message
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInArea("Crystal Hollows") && spawnCooldown > 0) {
            spawnCooldown--;
            if (spawnCooldown == 0 && cooldown.value()) {
                Utils.showTitle("§a§lCOOLDOWN ENDED", "", 5, 20, 5);
                Utils.info("§aWorm spawn cooldown ended!");
                Utils.playSound(SoundEvents.NOTE_BLOCK_HARP, 1.0f, 0.0f);
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        spawnCooldown = 0;
    }

    private enum WormType {
        Scatha,
        Worm,
        None
    }
}
