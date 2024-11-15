package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Config;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class SeaCreatureCap {
    private static final String[] seaCreatureNames = {
            "Squid",
            "Sea Walker",
            "Night Squid",
            "Sea Guardian",
            "Sea Archer",
            "Sea Witch",
            "Rider of the Deep",
            "Catfish",
            "Sea Leech",
            "Guardian Defender",
            "Agarimoo",
            "Deep Sea Protector",
            "Oasis Rabbit",
            "Oasis Sheep",
            "Water Worm",
            "Poisoned Water Worm",
            "Scarecrow",
            "Nightmare",
            "Werewolf",
            "Frozen Steve",
            "Frosty",
            "Grinch",
            "Nutcracker",
            "Nurse Shark",
            "Blue Shark",
            "Tiger Shark",
            "Magma Slug",
            "Moogma",
            "Lava Leech",
            "Pyroclastic Worm",
            "Lava Flame",
            "Fire Eel",
            "Taurus",
            "Flaming Worm",
            "Lava Blaze",
            "Lava Pigman",
            "Plhlegblast",
            "Thunder",
            "Lord Jawbus",
            "Great White Shark",
            "Yeti",
            "Reindrake",
            "Phantom Fisher",
            "Grim Reaper",
            "Carrot King",
            "Water Hydra",
            "The Sea Emperor",
            "Abyssal Miner"
    };
    private static int notifyTicks = 0;

    @EventHandler
    public static void tick(WorldTickEvent event) {
        if (Config.fishCapEnabled) {
            if (notifyTicks > 0) {
                notifyTicks--;
            } else {
                int count = 0;
                for (Entity ent : mc.world.getEntities()) {
                    if (ent.getType() == EntityType.ARMOR_STAND && ent.hasCustomName()) {
                        String creatureName = ent.getCustomName().getString().toLowerCase();
                        for (String name : seaCreatureNames) {
                            if (creatureName.contains(name.toLowerCase()) && creatureName.contains("❤")) {
                                count++;
                            }
                        }
                    }
                }
                if (count >= Config.fishCap) {
                    if (Config.fishCapSendMsg && !Config.fishCapMsg.isEmpty()) {
                        Utils.sendMessage(Config.fishCapMsg);
                    }
                    if (Config.fishCapSound) {
                        Utils.playSound(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 1, 1);
                    }
                    if (Config.fishCapTitle) {
                        Utils.showTitle("§4§lCAP REACHED!", "§8§l" + count + " SEA CREATURES", 5, 20, 5);
                    }
                    notifyTicks = Config.fishCapDelay * 20;
                }
            }
        }
    }
}
