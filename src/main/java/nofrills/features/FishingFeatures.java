package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EntityNamedEvent;
import nofrills.events.PlaySoundEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class FishingFeatures {
    private static final List<Entity> seaCreatures = new ArrayList<>();
    private static final SeaCreature[] seaCreatureData = {
            new SeaCreature("Plhlegblast", "WOAH! A Plhlegblast appeared.", "§9", true),
            new SeaCreature("Thunder", "You hear a massive rumble as Thunder emerges.", "§b", true),
            new SeaCreature("Lord Jawbus", "You have angered a legendary creature... Lord Jawbus has arrived.", "§d", true),
            new SeaCreature("Great White Shark", "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!", "§9", true),
            new SeaCreature("Yeti", "What is this creature!?", "§3", true),
            new SeaCreature("Reindrake", "A Reindrake forms from the depths.", "§c", true),
            new SeaCreature("Phantom Fisher", "The spirit of a long lost Phantom Fisher has come to haunt you.", "§3", true),
            new SeaCreature("Grim Reaper", "This can't be! The manifestation of death himself!", "§5", true),
            new SeaCreature("Carrot King", "Is this even a fish? It's the Carrot King!", "§a", true),
            new SeaCreature("Water Hydra", "The Water Hydra has come to test your strength.", "§1", true),
            new SeaCreature("Sea Emperor", "The Sea Emperor arises from the depths.", "§4", true),
            new SeaCreature("Abyssal Miner", "An Abyssal Miner breaks out of the water!", "§2", true),
            SeaCreature.plain("Squid"),
            SeaCreature.plain("Sea Walker"),
            SeaCreature.plain("Night Squid"),
            SeaCreature.plain("Sea Guardian"),
            SeaCreature.plain("Sea Archer"),
            SeaCreature.plain("Sea Witch"),
            SeaCreature.plain("Rider of the Deep"),
            SeaCreature.plain("Mithril Grubber"),
            SeaCreature.plain("Catfish"),
            SeaCreature.plain("Sea Leech"),
            SeaCreature.plain("Guardian Defender"),
            SeaCreature.plain("Agarimoo"),
            SeaCreature.plain("Deep Sea Protector"),
            SeaCreature.plain("Oasis Rabbit"),
            SeaCreature.plain("Oasis Sheep"),
            SeaCreature.plain("Water Worm"),
            SeaCreature.plain("Poisoned Water Worm"),
            SeaCreature.plain("Scarecrow"),
            SeaCreature.plain("Nightmare"),
            SeaCreature.plain("Werewolf"),
            SeaCreature.plain("Frozen Steve"),
            SeaCreature.plain("Frosty"),
            SeaCreature.plain("Grinch"),
            SeaCreature.plain("Nutcracker"),
            SeaCreature.plain("Nurse Shark"),
            SeaCreature.plain("Blue Shark"),
            SeaCreature.plain("Tiger Shark"),
            SeaCreature.plain("Magma Slug"),
            SeaCreature.plain("Moogma"),
            SeaCreature.plain("Lava Leech"),
            SeaCreature.plain("Pyroclastic Worm"),
            SeaCreature.plain("Lava Flame"),
            SeaCreature.plain("Fire Eel"),
            SeaCreature.plain("Taurus"),
            SeaCreature.plain("Flaming Worm"),
            SeaCreature.plain("Lava Blaze"),
            SeaCreature.plain("Lava Pigman"),
    };
    private static int notifyTicks = 0;

    private static boolean isHoldingRod() {
        LoreComponent lore = mc.player.getMainHandStack().getComponents().get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                String lineClean = Formatting.strip(line.getString());
                if (lineClean.startsWith("Sea Creature Chance:")) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public static void tick(WorldTickEvent event) {
        if (Config.capEnabled) {
            if (notifyTicks > 0) {
                notifyTicks--;
            } else {
                List<Entity> creatures = new ArrayList<>(seaCreatures);
                for (Entity ent : creatures) {
                    if (!ent.isAlive()) {
                        seaCreatures.remove(ent);
                    }
                }
                int count = seaCreatures.size();
                if (count >= Config.capTarget) {
                    if (Config.capSendMsg && !Config.capMsg.isEmpty()) {
                        Utils.sendMessage(Config.capMsg);
                    }
                    if (Config.capSound) {
                        Utils.playSound(SoundEvents.ITEM_TRIDENT_RETURN, SoundCategory.MASTER, 3, 1);
                    }
                    if (Config.capTitle) {
                        Utils.showTitle("§4§lCAP REACHED!", "§8§l" + count + " SEA CREATURES", 5, 20, 5);
                    }
                    notifyTicks = Config.capDelay * 20;
                }
            }
        }
        if (Config.capRender && isHoldingRod()) {
            Utils.showTitleCustom(String.valueOf(seaCreatures.size()), 1, 5, 2.0f, 0x00aaaa);
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if ((Config.capEnabled || Config.rareGlow) && event.namePlain.contains(Utils.Symbols.heart)) {
            for (SeaCreature creature : seaCreatureData) {
                String name = event.namePlain.toLowerCase();
                if (Config.capEnabled && name.contains(creature.name.toLowerCase())) {
                    if (!seaCreatures.contains(event.entity)) {
                        seaCreatures.add(event.entity);
                    }
                }
                if (Config.rareGlow && creature.rare && event.entity.age < 10 && name.contains(creature.name.toLowerCase())) {
                    Entity owner = Utils.findNametagOwner(event.entity, Utils.getNearbyEntities(event.entity, 0.5, 2, 0.5, ent -> ent instanceof LivingEntity));
                    if (owner != null) {
                        Rendering.Entities.drawGlow(owner, true, new RenderColor(255, 170, 0, 0));
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onChatMsg(ChatMsgEvent event) {
        if (Config.rareSound || Config.rareTitle) {
            for (SeaCreature creature : seaCreatureData) {
                if (creature.rare && event.messagePlain.equals(creature.spawnMsg)) {
                    if (Config.rareTitle) {
                        Utils.showTitle(creature.color + "§l" + creature.name.toUpperCase(), "", 5, 20, 5);
                    }
                    if (Config.rareSound) {
                        Utils.playSound(SoundEvents.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED,
                                SoundCategory.MASTER, 1, 1);
                    }
                    if (Config.rareSendMsg && !Config.rareMsg.isEmpty()) {
                        Utils.sendMessage(Config.rareMsg.replace("{name}", creature.name).replace("{spawnmsg}", creature.spawnMsg));
                    }
                    if (Config.rareReplace) {
                        Utils.infoNoPrefix(creature.color + "§l" + creature.spawnMsg + "§r");
                        event.cancel();
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (Config.muteDrake && Utils.isInArea("Jerry's Workshop")) {
            if (event.isSound(SoundEvents.ITEM_TOTEM_USE)) {
                event.cancel();
            }
        }
    }

    private static class SeaCreature {
        public String name;
        public String spawnMsg;
        public String color;
        public boolean rare;

        public SeaCreature(String name, String spawnMsg, String color, boolean rare) {
            this.name = name;
            this.spawnMsg = spawnMsg;
            this.color = color;
            this.rare = rare;
        }

        public static SeaCreature plain(String name) {
            return new SeaCreature(name, "", "", false);
        }
    }
}
