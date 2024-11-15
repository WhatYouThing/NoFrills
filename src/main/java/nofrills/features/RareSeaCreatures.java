package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ReceivePacketEvent;
import nofrills.misc.Utils;

public class RareSeaCreatures {
    @EventHandler
    private static void onChatMsg(ChatMsgEvent event) {
        if (Config.fishRareSound || Config.fishRareTitle) {
            String msg = event.getMessage().getString();
            String[] creatureData = switch (msg) {
                case "WOAH! A Plhlegblast appeared." -> new String[]{"Plhlegblast", "§9"};
                case "You hear a massive rumble as Thunder emerges." -> new String[]{"Thunder", "§b"};
                case "You have angered a legendary creature... Lord Jawbus has arrived." ->
                        new String[]{"Lord Jawbus", "§d"};
                case "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!" ->
                        new String[]{"Great White Shark", "§9"};
                case "What is this creature!?" -> new String[]{"Yeti", "§3"};
                case "A Reindrake forms from the depths." -> new String[]{"Reindrake", "§c"};
                case "The spirit of a long lost Phantom Fisher has come to haunt you." ->
                        new String[]{"Phantom Fisher", "§3"};
                case "This can't be! The manifestation of death himself!" -> new String[]{"Grim Reaper", "§5"};
                case "Is this even a fish? It's the Carrot King!" -> new String[]{"Carrot King", "§a"};
                case "The Water Hydra has come to test your strength." -> new String[]{"Water Hydra", "§1"};
                case "The Sea Emperor arises from the depths." -> new String[]{"Sea Emperor", "§4"};
                case "An Abyssal Miner breaks out of the water!" -> new String[]{"Abyssal Miner", "§2"};
                default -> null;
            };
            if (creatureData != null) {
                if (Config.fishRareTitle) {
                    Utils.showTitle(creatureData[1] + "§l" + creatureData[0].toUpperCase(), "", 5, 20, 5);
                }
                if (Config.fishRareSound) {
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                            SoundCategory.MASTER, 1, 0);
                }
                if (Config.fishRareMsgSend && !Config.fishRareMsg.isEmpty()) {
                    Utils.sendMessage(Config.fishRareMsg.replace("{name}", creatureData[0]).replace("{spawnmsg}", msg));
                }
                if (Config.fishRareReplace) {
                    Utils.infoNoPrefix(creatureData[1] + "§l" + msg + "§r");
                    event.cancel();
                }
            }
        }
    }

    @EventHandler
    private static void onPacket(ReceivePacketEvent event) {
        if (Config.fishMuteDrake) {
            if (event.packet instanceof PlaySoundS2CPacket soundPacket) {
                if (soundPacket.getSound().value().getId().toString().equalsIgnoreCase("minecraft:item.totem.use")) {
                    if (soundPacket.getPitch() < 0.75) {
                        event.cancel();
                    }
                }
            }
        }
    }
}
