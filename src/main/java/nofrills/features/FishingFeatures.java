package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.*;
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
            new SeaCreature("Alligator", "A long snout breaks the surface of the water. It's an Alligator!", "§2", true),
            new SeaCreature("Titanoboa", "A massive Titanoboa surfaces. It's body stretches as far as the eye can see.", "§e", true),
            new SeaCreature("Blue Ringed Octopus", "A garish set of tentacles arise. It's a Blue Ringed Octopus!", "§9", true),
            new SeaCreature("Fiery Scuttler", "A Fiery Scuttler inconspicuously waddles up to you, friends in tow.", "§6", true),
            new SeaCreature("Wiki Tiki", "The water bubbles and froths. A massive form emerges- you have disturbed the Wiki Tiki! You shall pay the price.", "§d", true),
            new SeaCreature("Ragnarok", "The sky darkens and the air thickens. The end times are upon us: Ragnarok is here.", "§c", true),
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
            SeaCreature.plain("Frog Man"),
            SeaCreature.plain("Trash Gobbler"),
            SeaCreature.plain("Dumpster Diver"),
            SeaCreature.plain("Banshee"),
            SeaCreature.plain("Snapping Turtle"),
            SeaCreature.plain("Fried Chicken"),
            SeaCreature.plain("Bayou Sludge"),
            SeaCreature.plain("Fireproof Witch"),
    };
    private static final RenderColor rareColor = new RenderColor(255, 170, 0, 0);
    private static int notifyTicks = 0;

    private static boolean isValidMob(Entity ent) {
        return !Rendering.Entities.isDrawingGlow(ent) && Utils.isMob(ent);
    }

    private static boolean isHoldingRod() {
        if (mc.player != null) {
            ItemStack stack = mc.player.getMainHandStack();
            if (stack != null && !stack.isEmpty() && stack.getItem().equals(Items.FISHING_ROD)) {
                LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
                if (lore != null) {
                    for (Text line : lore.lines()) {
                        String lineClean = Formatting.strip(line.getString());
                        if (lineClean.startsWith("Sea Creature Chance:")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public static void tick(WorldTickEvent event) {
        if (Config.capEnabled) {
            List<Entity> creatures = new ArrayList<>(seaCreatures);
            for (Entity ent : creatures) {
                if (ent == null || ent.isRemoved()) {
                    seaCreatures.remove(ent);
                }
            }
            int count = seaCreatures.size();
            if (count >= Config.capTarget && notifyTicks == 0) {
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
            if (notifyTicks > 0) {
                notifyTicks--;
            }
        }
        if (Config.capRender && !seaCreatures.isEmpty() && isHoldingRod()) {
            Utils.showTitleCustom(String.valueOf(seaCreatures.size()), 1, 5, 2.0f, 0x00aaaa);
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (!Utils.isInDungeons() && event.namePlain.contains(Utils.Symbols.heart)) {
            for (SeaCreature creature : seaCreatureData) {
                String name = event.namePlain.toLowerCase();
                if (Config.capEnabled && name.contains(creature.name.toLowerCase())) {
                    if (!seaCreatures.contains(event.entity)) {
                        seaCreatures.add(event.entity);
                    }
                }
                if (Config.rareGlow && creature.rare && event.entity.age <= 20 && name.contains(creature.name.toLowerCase())) {
                    Entity owner = Utils.findNametagOwner(event.entity, Utils.getNearbyEntities(event.entity, 0.5, 2, 0.5, FishingFeatures::isValidMob));
                    if (owner != null) {
                        Rendering.Entities.drawGlow(owner, true, rareColor);
                        if (owner.hasVehicle()) {
                            Rendering.Entities.drawGlow(owner.getVehicle(), true, rareColor);
                        } else if (owner.hasPassengers()) {
                            Rendering.Entities.drawGlow(owner.getFirstPassenger(), true, rareColor);
                        }
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

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        seaCreatures.clear();
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
