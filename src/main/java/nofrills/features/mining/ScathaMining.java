package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EntityNamedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.Utils;

import static nofrills.Main.mc;


public class ScathaMining {
    public static final Feature instance = new Feature("scathaMining");

    public static final SettingBool alert = new SettingBool(false, "alert", instance.key());
    public static final SettingBool cooldown = new SettingBool(false, "cooldown", instance.key());

    private static final EntityCache wormsCache = new EntityCache();
    private static int spawnCooldown = 0;

    private static boolean active() {
        return instance.isActive() && Utils.isInArea("Crystal Hollows");
    }

    private static WormType getWormType(String name) {
        if (name.endsWith(Utils.Symbols.heart)) {
            if (name.startsWith("[Lv10] Scatha ")) return WormType.Scatha;
            if (name.startsWith("[Lv5] Worm ")) return WormType.Worm;
        }
        return WormType.None;
    }

    private static boolean isWithinRadius(BlockPos wormPos) {
        if (mc.player != null) {
            BlockPos playerPos = mc.player.getBlockPos();
            return Math.abs(wormPos.getY() - playerPos.getY()) <= 4 &&
                    (Math.abs(wormPos.getX() - playerPos.getX()) <= 2 || Math.abs(wormPos.getZ() - playerPos.getZ()) <= 2);
        }
        return false;
    }

    private static void alertSpawn(boolean scatha) {
        if (scatha) {
            Utils.showTitle("§cScatha", "", 5, 20, 5);
            Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1.0f, 1.0f);
        } else {
            Utils.showTitle("§eWorm", "", 5, 20, 5);
            Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.MASTER, 1.0f, 0.0f);
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (active()) {
            WormType type = getWormType(event.namePlain);
            if (!type.equals(WormType.None) && !wormsCache.has(event.entity) && isWithinRadius(event.entity.getBlockPos())) {
                if (alert.value()) {
                    alertSpawn(type.equals(WormType.Scatha));
                }
                wormsCache.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (active() && event.messagePlain.equals("You hear the sound of something approaching...")) {
            spawnCooldown = 620; // 30s + the 1 second it takes the worm to spawn after the message
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (spawnCooldown > 0) {
            spawnCooldown--;
            if (spawnCooldown == 0) {
                if (active() && cooldown.value()) {
                    Utils.showTitle("§a§lCOOLDOWN ENDED", "", 5, 20, 5);
                    Utils.info("§aWorm spawn cooldown ended!");
                    Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.MASTER, 1.0f, 0.0f);
                }
                wormsCache.removeDead();
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        spawnCooldown = 0;
        wormsCache.clear();
    }

    private enum WormType {
        Scatha,
        Worm,
        None
    }
}
