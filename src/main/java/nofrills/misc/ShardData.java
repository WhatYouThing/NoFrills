package nofrills.misc;

import com.google.gson.JsonObject;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Config;
import nofrills.config.DataFile;
import nofrills.events.EventListener;
import nofrills.events.SlotUpdateEvent;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventListener
public class ShardData {
    private static final DataFile data = Config.getDataFile("ShardData.json");

    private static final Pattern sourcePattern = Pattern.compile("Source: (?<name>.*) Shard \\(.*[0-9]*\\)");
    private static final Pattern attributeLevelPattern = Pattern.compile("Attribute Level: (?<level>[0-9]*)(?:| \\(MAX!\\))");
    private static final Pattern rarityPattern = Pattern.compile("Rarity: (?<rarity>[A-Z]*)");
    private static final Pattern enabledPattern = Pattern.compile("Enabled: (?<value>Yes|No)");
    private static final Pattern skillPattern = Pattern.compile("(?<skill>Combat|Fishing|Farming|Foraging|Mining|Taming|Enchanting|Hunting|Global|Alchemy)");

    public static String getId(ItemStack stack) {
        if (isShard(stack)) {
            String source = getSource(stack);
            return getId(!source.isEmpty() ? source : Utils.toPlain(stack.getHoverName()));
        }
        return "";
    }

    public static String getId(String name) {
        return parseId(name);
    }

    public static String parseId(String name) {
        String shard = name.contains(" Shard") ? name.substring(0, name.indexOf(" Shard")).trim() : name.trim();
        return switch (Utils.toLower(shard)) {
            case "prismarine", "enchanted prismarine", "reroll" -> "";
            case "cinderbat" -> "SHARD_CINDER_BAT";
            case "abyssal lanternfish" -> "SHARD_ABYSSAL_LANTERN";
            case "stridersurfer" -> "SHARD_STRIDER_SURFER";
            case "bogged" -> "SHARD_SEA_ARCHER";
            case "loch emperor" -> "SHARD_SEA_EMPEROR";
            case "end stone protector" -> "SHARD_ENDSTONE_PROTECTOR";
            case "lotusfish" -> "SHARD_LOTUS_FISH";
            case "inkling" -> "SHARD_NIGHT_SQUID";
            case "field mouse" -> "SHARD_PEST";
            case "flipflopper" -> "SHARD_FLIP_FLOPPER";
            case "earthworm" -> "SHARD_TERMITE";
            case "zealot bruiser" -> "SHARD_BRUISER";
            case "seashine" -> "SHARD_SEA_SHINE";
            case "beetle" -> "SHARD_CROPEETLE";
            case "wither spectre" -> "SHARD_WITHER_SPECTER";
            case "inferno demonlord" -> "SHARD_BURNINGSOUL";
            default -> Utils.format("SHARD_{}", Utils.toUpper(shard.replaceAll(" ", "_")));
        };
    }

    public static Optional<CachedShard> getFromCache(String name) {
        JsonObject object = data.get();
        String shard = Utils.toLower(name);
        if (object.has(shard)) {
            JsonObject shardObject = object.get(shard).getAsJsonObject();
            return Optional.of(new CachedShard(
                    shardObject.has("level") ? shardObject.get("level").getAsInt() : 0,
                    shardObject.has("rarity") ? shardObject.get("rarity").getAsString() : "",
                    shardObject.has("enabled") && shardObject.get("enabled").getAsBoolean(),
                    shardObject.has("skill") ? shardObject.get("skill").getAsString() : ""
            ));
        }
        return Optional.empty();
    }

    public static String getColorPrefix(String shard) {
        String rarity = getFromCache(shard).map(cached -> cached.rarity).orElse("");
        return switch (rarity) {
            case "LEGENDARY" -> "§6";
            case "EPIC" -> "§5";
            case "RARE" -> "§9";
            case "UNCOMMON" -> "§a";
            case "COMMON" -> "§f";
            default -> "§7";
        };
    }

    public static int getColorHex(String shard) {
        String rarity = getFromCache(shard).map(cached -> cached.rarity).orElse("");
        return switch (rarity) {
            case "LEGENDARY" -> 0xffffaa00;
            case "EPIC" -> 0xffaa00aa;
            case "RARE" -> 0xff5555ff;
            case "UNCOMMON" -> 0xff55ff55;
            case "COMMON" -> 0xffffffff;
            default -> 0xffaaaaaa;
        };
    }

    private static boolean isShard(ItemStack stack) {
        String id = Utils.getSkyblockId(stack);
        String name = Utils.toPlain(stack.getHoverName());
        if (id.equals("ATTRIBUTE_SHARD") || name.contains(" Shard")) {
            return true;
        }
        if (id.isEmpty()) {
            boolean source = false, rarity = false;
            for (String line : Utils.getLoreLines(stack)) {
                if (line.contains(" SHARD (ID ")) {
                    return true;
                }
                if (line.startsWith("Source: ") && line.contains(" Shard")) {
                    source = true;
                }
                if (line.startsWith("Rarity: ")) {
                    rarity = true;
                }
            }
            return source && rarity;
        }
        return false;
    }

    private static String getSource(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            Matcher matcher = sourcePattern.matcher(line);
            if (!matcher.matches()) continue;
            String name = matcher.group("name");
            if (name != null) {
                return name;
            }
        }
        return "";
    }

    private static int getLevel(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            Matcher matcher = attributeLevelPattern.matcher(line);
            if (!matcher.matches()) continue;
            String level = matcher.group("level");
            if (level != null) {
                return Utils.parseInt(level).orElse(0);
            }
        }
        return 0;
    }

    private static String getRarity(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            Matcher matcher = rarityPattern.matcher(line);
            if (!matcher.matches()) continue;
            String rarity = matcher.group("rarity");
            if (rarity != null) {
                return rarity;
            }
        }
        return "";
    }

    private static boolean getEnabled(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            Matcher matcher = enabledPattern.matcher(line);
            if (!matcher.matches()) continue;
            String value = matcher.group("value");
            return value != null && value.equals("Yes");
        }
        return false;
    }

    private static String getSkill(ItemStack stack) {
        for (String line : Utils.getLoreLines(stack)) {
            Matcher matcher = skillPattern.matcher(line);
            if (!matcher.matches()) continue;
            String skill = matcher.group("skill");
            if (skill != null) {
                return skill;
            }
        }
        return "";
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (!event.stack.isEmpty() && !event.isInventory && event.isPaginatedMenu("Attribute Menu")) {
            String source = getSource(event.stack);
            if (source.isEmpty()) return;
            JsonObject object = new JsonObject();
            object.addProperty("level", getLevel(event.stack));
            object.addProperty("rarity", getRarity(event.stack));
            object.addProperty("enabled", getEnabled(event.stack));
            object.addProperty("skill", getSkill(event.stack));
            data.get().add(Utils.toLower(source), object);
        }
    }

    public record CachedShard(int level, String rarity, boolean enabled, String skill) {

        public static final CachedShard EMPTY = new CachedShard(0, "", false, "");
    }
}
