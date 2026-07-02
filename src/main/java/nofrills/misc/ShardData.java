package nofrills.misc;

import com.google.gson.JsonObject;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Config;
import nofrills.config.DataFile;
import nofrills.events.EventListener;
import nofrills.events.SlotUpdateEvent;

import java.util.List;

@EventListener
public class ShardData {
    private static final DataFile data = Config.getDataFile("ShardData.json");

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
        String shard = Utils.toLower(name);
        return switch (shard.contains(" shard") ? shard.substring(0, shard.indexOf(" shard")).trim() : shard.trim()) {
            case "prismarine", "enchanted prismarine" -> "";
            case "cinderbat" -> "SHARD_CINDER_BAT";
            case "abyssal lanternfish" -> "SHARD_ABYSSAL_LANTERN";
            case "stridersurfer" -> "SHARD_STRIDER_SURFER";
            case "bogged" -> "SHARD_SEA_ARCHER";
            case "loch emperor" -> "SHARD_SEA_EMPEROR";
            case "end stone protector" -> "SHARD_ENDSTONE_PROTECTOR";
            default -> Utils.format("SHARD_{}", Utils.toUpper(shard.replaceAll(" ", "_")));
        };
    }

    public static String getShardSkill(String name) {
        return data.get().has(name) ? data.get().get(name).getAsJsonObject().get("skill").getAsString() : "";
    }

    public static String getShardRarity(String name) {
        return data.get().has(name) ? data.get().get(name).getAsJsonObject().get("rarity").getAsString() : "";
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
            if (line.startsWith("Source: ") && line.contains(" Shard")) {
                return line.substring(line.indexOf(":") + 2, line.indexOf("Shard") - 1);
            }
        }
        return "";
    }

    private static void addToCache(String source, String skill, String rarity) {
        JsonObject object = new JsonObject();
        object.addProperty("skill", skill);
        object.addProperty("rarity", rarity);
        data.get().add(Utils.toLower(source), object);
    }

    public static String getColorPrefix(String shard) {
        String rarity = getShardRarity(shard);
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
        String rarity = getShardRarity(shard);
        return switch (rarity) {
            case "LEGENDARY" -> 0xffffaa00;
            case "EPIC" -> 0xffaa00aa;
            case "RARE" -> 0xff5555ff;
            case "UNCOMMON" -> 0xff55ff55;
            case "COMMON" -> 0xffffffff;
            default -> 0xffaaaaaa;
        };
    }

    @EventHandler
    private static void onSlotUpdate(SlotUpdateEvent event) {
        if (event.stack.isEmpty() || event.isInventory) return;
        if (event.title.equals("Hunting Box")) {
            List<String> lines = Utils.getLoreLines(event.stack);
            if (lines.stream().noneMatch(line -> line.startsWith("Owned: ") && line.contains(" Shard"))) return;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains(" (") && !line.contains(" (ID ") && line.endsWith(")")) {
                    addToCache(
                            Utils.toPlain(event.stack.getHoverName()),
                            line.substring(line.indexOf("(") + 1, line.indexOf(")")),
                            lines.getLast().substring(0, lines.getLast().indexOf(" "))
                    );
                    break;
                }
            }
        } else if (event.title.equals("Attribute Menu")) {
            List<String> lines = Utils.getLoreLines(event.stack);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("Source: ") && line.contains(" Shard") && line.endsWith(")")) {
                    addToCache(
                            line.substring(line.indexOf(":") + 2, line.indexOf("Shard") - 1),
                            lines.getFirst(),
                            lines.get(i + 1).substring(line.indexOf(":") + 2)
                    );
                    break;
                }
            }
        }
    }
}
