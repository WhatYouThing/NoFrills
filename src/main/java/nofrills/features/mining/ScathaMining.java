package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EntityNamedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;


public class ScathaMining {
    public static final Feature instance = new Feature("scathaMining");

    public static final SettingBool alert = new SettingBool(false, "alert", instance.key());
    public static final SettingBool cooldown = new SettingBool(false, "cooldown", instance.key());

    private static int spawnCooldown = 0;

    private static boolean active() {
        return SkyblockData.getArea().equals("Crystal Hollows") && instance.isActive();
    }

    private static wormType getWormType(String name) {
        if (name.endsWith(Utils.Symbols.heart)) {
            if (name.contains("Scatha")) {
                return wormType.Scatha;
            }
            if (name.contains("Worm")) {
                return wormType.Worm;
            }
        }
        return wormType.None;
    }

    private static void alertSpawn(boolean scatha) {
        if (scatha) {
            Utils.showTitle("§cScatha", "§7GOLD GOLD GOLD!", 5, 20, 5);
            Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0f, 1.0f);
        } else {
            Utils.showTitle("§eWorm", "§7Drops Hytale", 5, 20, 5);
            Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.MASTER, 1.0f, 0.0f);
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (active()) {
            wormType type = getWormType(event.namePlain);
            if (!type.equals(wormType.None) && alert.value()) {
                alertSpawn(type.equals(wormType.Scatha));
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
            if (spawnCooldown == 0 && cooldown.value()) {
                Utils.showTitle("§aWORM COOLDOWN ENDED", "", 5, 20, 5);
                Utils.info("§a§lWorm spawn cooldown ended!");
                Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.MASTER, 1.0f, 0.0f);
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        spawnCooldown = 0;
    }

    private enum wormType {
        Scatha,
        Worm,
        None
    }
}
