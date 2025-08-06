package nofrills.misc;

import java.util.List;

public class SeaCreatureData {
    public static final List<SeaCreature> list = List.of(
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
                    "A massive Titanoboa surfaces. Its body stretches as far as the eye can see.",
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
            SeaCreature.plain("Tadgang"),
            SeaCreature.plain("Ent"),
            SeaCreature.plain("Bogged"),
            SeaCreature.plain("Wetwing"),
            SeaCreature.plain("Stridersurfer")
    );

    public static class SeaCreature {
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
