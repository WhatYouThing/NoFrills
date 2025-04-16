package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EntityNamedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

public class ScathaMining {
    private static final String spawnMsg = "You hear the sound of something approaching...";
    private static int spawnWait = 0;
    private static int spawnCooldown = 0;

    private static boolean isInHollows() {
        return SkyblockData.getArea().equals("Crystal Hollows");
    }

    private static void alertSpawn(boolean scatha) {
        if (Config.wormAlert) {
            if (scatha) {
                Utils.showTitle("§cScatha", "§7GOLD GOLD GOLD!", 5, 20, 5);
                Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0f, 1.0f);
            } else {
                Utils.showTitle("§eWorm", "§7Drops Half Life 3", 5, 20, 5);
                Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.MASTER, 1.0f, 0.0f);
            }
        }
        spawnCooldown = 600;
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (isInHollows() && spawnWait > 0) {
            if (event.namePlain.contains("Scatha") || (event.namePlain.contains("Worm") && event.namePlain.contains("[Lv5]"))) {
                alertSpawn(event.namePlain.contains("Scatha"));
                spawnWait = 0;
            }
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (isInHollows() && event.messagePlain.equals(spawnMsg)) {
            spawnWait = 100;
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (spawnWait > 0) {
            spawnWait--;
        }
        if (spawnCooldown > 0) {
            spawnCooldown--;
            if (spawnCooldown == 0 && Config.wormCooldown) {
                Utils.showTitle("§aSpawn Cooldown Ended", "", 5, 20, 5);
                Utils.info("§a§lWorm spawn cooldown ended!");
                Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.MASTER, 1.0f, 0.0f);
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        spawnWait = 0;
        spawnCooldown = 0;
    }
}
