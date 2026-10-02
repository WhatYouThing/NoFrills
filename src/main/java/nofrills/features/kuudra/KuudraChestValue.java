package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import nofrills.config.*;
import nofrills.events.EventListener;
import nofrills.events.ScreenOpenEvent;
import nofrills.events.ScreenRenderEvent;
import nofrills.events.SlotUpdateEvent;
import nofrills.misc.NoFrillsAPI;
import nofrills.misc.RenderColor;
import nofrills.misc.ShardData;
import nofrills.misc.Utils;

import java.util.HashMap;
import java.util.Optional;

import static nofrills.Main.mc;
import static nofrills.misc.NoFrillsAPI.auctionPricing;
import static nofrills.misc.NoFrillsAPI.bazaarPricing;

@EventListener
public class KuudraChestValue {
    public static final Feature instance = new Feature("kuudraChestValue", Feature.Flags.UsePricingAPI);

    public static final SettingInt petBonus = new SettingInt(0, "petBonus", instance);
    public static final SettingEnum<Factions> faction = new SettingEnum<>(Factions.Unknown, Factions.class, "faction", instance);
    public static final SettingEnum<ReputationTiers> reputation = new SettingEnum<>(ReputationTiers.Zero, ReputationTiers.class, "reputation", instance);
    public static final SettingEnum<DiscountItems> discountItem = new SettingEnum<>(DiscountItems.None, DiscountItems.class, "discountItem", instance);
    public static final SettingBool salvageValue = new SettingBool(false, "salvageValue", instance);
    public static final SettingColor background = new SettingColor(RenderColor.fromHex(0x202020, 0.8f), "background", instance);

    public static final HashMap<String, Integer> salvageAmounts = buildSalvageAmounts();
    private static double currentValue = 0.0;

    public static double getKeyPrice(String tier) {
        String resource = switch (faction.value()) {
            case Barbarian -> "ENCHANTED_RED_SAND";
            case Mage -> "ENCHANTED_MYCELIUM";
            default -> "";
        };
        double resourceCost = resource.isEmpty()
                ? 0.0
                : bazaarPricing.getOrDefault(resource, NoFrillsAPI.BazaarPrice.ZERO).buy() * getKeyResourceCost(tier);
        double starCost = bazaarPricing.getOrDefault("CORRUPTED_NETHER_STAR", NoFrillsAPI.BazaarPrice.ZERO).buy() * 2.0;
        return (getKeyBaseCost(tier) * getReputationDiscount() * getAccessoryDiscount()) + resourceCost + starCost;
    }

    public static boolean isLootChest(String title) {
        return (title.startsWith("Free ") || title.startsWith("Paid ")) && title.endsWith(" Chest");
    }

    public static int getStarCost(int starCount) {
        int cost = 0;
        for (int i = 1; i <= starCount; i++) {
            cost += i > 7 ? i * 10 - 10 : i * 5 + 25; // simple formula for the price of each star on a basic tier piece
        }
        return cost;
    }

    public static double getEssenceMultiplier() {
        ShardData.CachedShard lavaLeech = ShardData.getFromCache("Lava Leech").orElse(ShardData.CachedShard.EMPTY);
        ShardData.CachedShard komodoDragon = ShardData.getFromCache("Komodo Dragon").orElse(ShardData.CachedShard.EMPTY);
        ShardData.CachedShard tiamat = ShardData.getFromCache("Tiamat").orElse(ShardData.CachedShard.EMPTY);
        int lavaLeechLevel = lavaLeech.enabled() ? lavaLeech.level() : 0;
        int komodoDragonLevel = komodoDragon.enabled() ? komodoDragon.level() : 0;
        int tiamatLevel = tiamat.enabled() ? tiamat.level() : 0;
        return 1 + petBonus.value() * 0.01 + (lavaLeechLevel * 0.01) * (1 + (komodoDragonLevel * 0.02) * (1 + tiamatLevel * 0.05));
    }

    public static String getKeyTier(String keyName) {
        return switch (keyName) {
            case "Hot Kuudra Key" -> "T2";
            case "Burning Kuudra Key" -> "T3";
            case "Fiery Kuudra Key" -> "T4";
            case "Infernal Kuudra Key" -> "T5";
            default -> "T1";
        };
    }

    private static int getKeyBaseCost(String tier) {
        return switch (tier) {
            case "T2" -> 400_000;
            case "T3" -> 750_000;
            case "T4" -> 1_500_000;
            case "T5" -> 3_000_000;
            default -> 200_000;
        };
    }

    private static int getKeyResourceCost(String tier) {
        return switch (tier) {
            case "T2" -> 4;
            case "T3" -> 16;
            case "T4" -> 40;
            case "T5" -> 80;
            default -> 2;
        };
    }

    private static double getReputationDiscount() {
        return switch (reputation.value()) {
            case One -> 0.95;
            case Three -> 0.9;
            case Seven -> 0.85;
            case Twelve -> 0.8;
            default -> 1.0;
        };
    }

    private static double getAccessoryDiscount() {
        return switch (discountItem.value()) {
            case ShadyRing -> 0.99;
            case CrookedArtifact -> 0.98;
            case SealOfTheFamily -> 0.97;
            default -> 1.0;
        };
    }

    private static HashMap<String, Integer> buildSalvageAmounts() {
        HashMap<String, Integer> map = new HashMap<>();
        for (String equipment : new String[]{"NECKLACE", "CLOAK", "BELT", "BRACELET"}) {
            map.put("MOLTEN_" + equipment, 600);
        }
        for (String armor : new String[]{"AURORA", "CRIMSON", "TERROR", "FERVOR", "HOLLOW"}) {
            for (String piece : new String[]{"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"}) {
                map.put(armor + "_" + piece, 120);
            }
        }
        map.put("RUNIC_STAFF", 600);
        map.put("HOLLOW_WAND", 600);
        map.put("KUUDRA_MANDIBLE", 600);
        return map;
    }

    private static int getLootQuantity(ItemStack stack, String name, String id) {
        if (salvageValue.value() && salvageAmounts.containsKey(id)) {
            CompoundTag data = Utils.getCustomData(stack);
            int stars = data != null ? data.getIntOr("upgrade_level", 0) : 0;
            return (int) Math.floor(salvageAmounts.get(id) + getStarCost(stars) * 0.6);
        }
        String[] parts = name.split(" ");
        String last = parts[parts.length - 1];
        if (last.startsWith("x")) {
            Optional<Integer> quantity = Utils.parseInt(last.replaceAll("x", "").replaceAll(",", ""));
            if (quantity.isPresent()) {
                if (name.startsWith("Crimson Essence")) {
                    return (int) Math.floor(quantity.get() * getEssenceMultiplier());
                }
                return quantity.get();
            }
        }
        return stack.getCount();
    }

    @EventHandler
    private static void onSlot(SlotUpdateEvent event) {
        if (instance.isActive() && !event.isInventory && isLootChest(event.title) && Utils.isInLootArea()) {
            if (event.stack.getItem().equals(Items.STAINED_GLASS_PANE.black())) {
                return;
            }
            String name = Utils.toPlain(event.stack.getHoverName());
            if (name.equals("Reroll Shard")) return;
            String id = name.startsWith("Crimson Essence") ? Utils.getMarketId(event.stack.getHoverName()) : Utils.getMarketId(event.stack);
            int quantity = getLootQuantity(event.stack, name, id);
            if (id.isEmpty()) {
                if (name.equals("Open Reward Chest")) {
                    for (String line : Utils.getLoreLines(event.stack)) {
                        if (line.endsWith(" Kuudra Key")) {
                            currentValue -= getKeyPrice(getKeyTier(line));
                            break;
                        }
                    }
                }
                return;
            } else if (salvageValue.value() && salvageAmounts.containsKey(id)) {
                id = "ESSENCE_CRIMSON";
            }
            if (auctionPricing.containsKey(id)) {
                currentValue += auctionPricing.get(id) * quantity;
            } else if (bazaarPricing.containsKey(id)) {
                currentValue += bazaarPricing.get(id).sell() * quantity;
            }
        }
    }

    @EventHandler
    private static void onRender(ScreenRenderEvent.After event) {
        if (instance.isActive() && currentValue != 0.0) {
            Slot targetSlot = event.handler.getSlot(4);
            String value = Utils.format("Chest Value: {}", Utils.formatSeparator(currentValue));
            int width = mc.font.width(value);
            int baseX = targetSlot.x + 8;
            int baseY = targetSlot.y + 8;
            event.context.fill((int) Math.floor(baseX - 2 - width * 0.5), baseY - 6, (int) Math.ceil(baseX + 2 + width * 0.5), baseY + 6, background.value().argb);
            event.context.centeredText(mc.font, value, baseX, baseY - 4, currentValue > 0 ? RenderColor.GREEN.argb : RenderColor.RED.argb);
        }
    }

    @EventHandler
    private static void onScreen(ScreenOpenEvent event) {
        currentValue = 0.0;
    }

    public enum Factions {
        Barbarian,
        Mage,
        Unknown
    }

    public enum ReputationTiers {
        Zero("0 - 1,000"),
        One("1,000 - 3,000"),
        Three("3,000 - 7,000"),
        Seven("7,000 - 12,000"),
        Twelve("12,000+");

        private final String displayName;

        ReputationTiers(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum DiscountItems {
        ShadyRing("Shady Ring"),
        CrookedArtifact("Crooked Artifact"),
        SealOfTheFamily("Seal of the Family"),
        None("None");

        private final String displayName;

        DiscountItems(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
