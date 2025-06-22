package nofrills.features;

import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.events.*;
import nofrills.hud.HudManager;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.Config;
import static nofrills.Main.mc;

public class FishingFeatures {
    private static final EntityCache seaCreatures = new EntityCache();
    private static final SeaCreature[] seaCreatureData = {
            new SeaCreature("Plhlegblast", "WOAH! A Plhlegblast appeared.", "§9", true, true),
            new SeaCreature("Thunder", "You hear a massive rumble as Thunder emerges.", "§b", true, true),
            new SeaCreature("Lord Jawbus", "You have angered a legendary creature... Lord Jawbus has arrived.", "§d", true, true),
            new SeaCreature("Great White Shark", "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!", "§9", true, true),
            new SeaCreature("Yeti", "What is this creature!?", "§3", true, true),
            new SeaCreature("Reindrake", "A Reindrake forms from the depths.", "§c", true, true),
            new SeaCreature("Phantom Fisher", "The spirit of a long lost Phantom Fisher has come to haunt you.", "§3", true, true),
            new SeaCreature("Grim Reaper", "This can't be! The manifestation of death himself!", "§5", true, true),
            new SeaCreature("Carrot King", "Is this even a fish? It's the Carrot King!", "§a", true, true),
            new SeaCreature("Water Hydra", "The Water Hydra has come to test your strength.", "§1", true, true),
            new SeaCreature("Sea Emperor", "The Sea Emperor arises from the depths.", "§4", true, true),
            new SeaCreature("Abyssal Miner", "An Abyssal Miner breaks out of the water!", "§2", true, true),
            new SeaCreature("Alligator", "A long snout breaks the surface of the water. It's an Alligator!", "§2", true, true),
            new SeaCreature(
                    "Titanoboa",
                    "A massive Titanoboa surfaces. It's body stretches as far as the eye can see.",
                    "§e",
                    true,
                    false)
                    .withTextures(List.of(
                            "b82086882b25e9e914362f2048c285c18c8d698a336f7e83f0a1964c760b11",
                            "645f2c0bbfe3b8b19b7452072db69a5f59da38ff61415545156e5701e1be756d"
                    )
            ),
            new SeaCreature(
                    "Blue Ringed Octopus",
                    "A garish set of tentacles arise. It's a Blue Ringed Octopus!",
                    "§9",
                    true,
                    false)
                    .withTextures(List.of(
                            "b2b6074d0c9d6b89a494cf4f74158282a64ee23ba8a0725633ad70932ada1a8f"
                    )
            ),
            new SeaCreature(
                    "Fiery Scuttler",
                    "A Fiery Scuttler inconspicuously waddles up to you, friends in tow.",
                    "§6",
                    true,
                    false)
                    .withTextures(List.of(
                            "55b194025806687642e2bc239895d646a6d8c193d9253b61bfce908f6ce1b84a"
                    )
            ),
            new SeaCreature(
                    "Wiki Tiki",
                    "The water bubbles and froths. A massive form emerges- you have disturbed the Wiki Tiki! You shall pay the price.",
                    "§d",
                    true,
                    false)
                    .withTextures(List.of(
                            "f3c802e580bfefc18c4af94cceb82968b5b4aeab0d832346a633a7473a41dfac",
                            "e64331c8fb750f9043334320c94580e7896955695156d80689e5d0a6c60a10e7",
                            "9122f7a19b3197766b381fb36bfeb6f442d62509e44cc7847c75c8e8c387225a",
                            "c5fd6b9a59ec5b97db8bdc158fbd5f91ef7b317b859fcebe6d09e7bd80eaca9d"
                    )
            ),
            new SeaCreature("Ragnarok", "The sky darkens and the air thickens. The end times are upon us: Ragnarok is here.", "§c", true, true),
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
    private static Entity bobberHologram = null;
    private static int notifyTicks = 0;

    private static boolean isValidMob(Entity ent) {
        return !Rendering.Entities.isDrawingGlow(ent) && Utils.isMob(ent) && !(ent instanceof GiantEntity);
    }

    @EventHandler
    public static void tick(WorldTickEvent event) {
        if (Config.capEnabled()) {
            seaCreatures.clearDropped();
            int count = seaCreatures.size();
            if (Config.seaCreaturesEnabled()) {
                HudManager.seaCreaturesElement.setCount(count);
            }
            if (count >= Config.capTarget() && notifyTicks == 0) {
                if (Config.capSendMsg() && !Config.capMsg().isEmpty()) {
                    Utils.sendMessage(Config.capMsg());
                }
                if (Config.capSound()) {
                    Utils.playSound(SoundEvents.ITEM_TRIDENT_RETURN, SoundCategory.MASTER, 3, 1);
                }
                if (Config.capTitle()) {
                    Utils.showTitle("§4§lCAP REACHED!", "§8§l" + count + " SEA CREATURES", 5, 20, 5);
                }
                notifyTicks = Config.capDelay() * 20;
            }
            if (notifyTicks > 0) {
                notifyTicks--;
            }
        }
        if (Config.bobberEnabled()) {
            if (mc.player.fishHook != null && (bobberHologram == null || !bobberHologram.isAlive())) {
                HudManager.bobberElement.setActive();
            } else if (mc.player.fishHook == null) {
                HudManager.bobberElement.setInactive();
            }
        }
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (Config.rareGlow() && !Utils.isInDungeons() && event.entity.age <= 10 && event.entity instanceof ArmorStandEntity armorStand) {
            GameProfile textures = Utils.getTextures(armorStand.getEquippedStack(EquipmentSlot.HEAD));
            if (textures != null) {
                for (SeaCreature creature : seaCreatureData) {
                    for (String texture : creature.textures) {
                        if (Utils.isTextureEqual(textures, texture)) {
                            Rendering.Entities.drawGlow(event.entity, true, rareColor);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (!Utils.isInDungeons() && event.namePlain.contains(Utils.Symbols.heart)) {
            for (SeaCreature creature : seaCreatureData) {
                String name = event.namePlain.toLowerCase();
                if (Config.capEnabled() && name.contains(creature.name.toLowerCase())) {
                    seaCreatures.add(event.entity);
                }
                if (Config.rareGlow() && creature.rare && creature.glow && event.entity.age <= 10 && name.contains(creature.name.toLowerCase())) {
                    Entity owner = Utils.findNametagOwner(event.entity, Utils.getNearbyEntities(event.entity, 0.5, 2, 0.5, FishingFeatures::isValidMob));
                    if (owner != null && !(owner instanceof GiantEntity)) {
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
        if (Config.bobberEnabled() && event.namePlain.length() == 3) {
            if (event.namePlain.equals("!!!") || event.namePlain.indexOf(".") == 1) {
                bobberHologram = event.entity;
                HudManager.bobberElement.setTimer(event.namePlain);
            }
        }
    }

    @EventHandler
    private static void onChatMsg(ChatMsgEvent event) {
        if (Config.rareSound() || Config.rareTitle()) {
            for (SeaCreature creature : seaCreatureData) {
                if (creature.rare && event.messagePlain.equals(creature.spawnMsg)) {
                    if (Config.rareTitle()) {
                        Utils.showTitle(creature.color + "§l" + creature.name.toUpperCase(), "", 5, 20, 5);
                    }
                    if (Config.rareSound()) {
                        Utils.playSound(SoundEvents.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED,
                                SoundCategory.MASTER, 1, 1);
                    }
                    if (Config.rareSendMsg() && !Config.rareMsg().isEmpty()) {
                        Utils.sendMessage(Config.rareMsg().replace("{name}", creature.name).replace("{spawnmsg}", creature.spawnMsg));
                    }
                    if (Config.rareReplace()) {
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
        if (Config.muteDrake() && Utils.isInArea("Jerry's Workshop")) {
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
        public boolean glow;
        public List<String> textures = List.of();

        public SeaCreature(String name, String spawnMsg, String color, boolean rare, boolean glow) {
            this.name = name;
            this.spawnMsg = spawnMsg;
            this.color = color;
            this.rare = rare;
            this.glow = glow;
        }

        public static SeaCreature plain(String name) {
            return new SeaCreature(name, "", "", false, false);
        }

        public SeaCreature withTextures(List<String> textures) {
            this.textures = textures;
            return this;
        }
    }
}
