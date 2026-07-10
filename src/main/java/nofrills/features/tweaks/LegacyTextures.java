package nofrills.features.tweaks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.NoFrillsAPI;
import nofrills.misc.Utils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static nofrills.Main.mc;

@EventListener
public class LegacyTextures {
    public static final Feature instance = new Feature("legacyTextures", Feature.Flags.UseItemTexturesAPI);

    public static final SettingBool unlockPackPos = new SettingBool(false, "unlockPackPos", instance);
    public static final SettingBool forcePackPos = new SettingBool(false, "forcePackPos", instance);
    public static final SettingBool noTooltipStyle = new SettingBool(false, "noTooltipStyle", instance);
    public static final SettingBool moreLegacy = new SettingBool(false, "moreLegacy", instance);

    private static final ConcurrentHashMap<String, ResolvableProfile> cache = new ConcurrentHashMap<>();

    public static ResolvableProfile getOrInitProfile(String id, String payload) {
        if (!cache.containsKey(id)) {
            Multimap<String, Property> properties = ImmutableMultimap.of("textures", new Property("textures", payload));
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "", new PropertyMap(properties));
            ResolvableProfile profile = ResolvableProfile.createResolved(gameProfile);
            cache.put(id, profile);
        }
        return cache.get(id);
    }

    public static Optional<Replacement> replaceIfNeeded(ItemStack stack) {
        Identifier model = stack.get(DataComponents.ITEM_MODEL);
        if (model != null && model.getNamespace().equals("hypixel_skyblock")) {
            CompoundTag data = Utils.getCustomData(stack);
            String id = Utils.getSkyblockId(data);
            if (id.isEmpty()) return Optional.empty();
            switch (id) {
                case "VOIDEDGE_KATANA", "VORPAL_KATANA", "ATOMSPLIT_KATANA" -> {
                    String path = mc.player.getCooldowns().isOnCooldown(stack) ? "golden_sword" : "diamond_sword";
                    return Optional.of(new Replacement(Identifier.withDefaultNamespace(path), null));
                }
                case "RAGNAROCK_AXE", "DAEDALUS_AXE", "STARRED_DAEDALUS_AXE" -> {
                    if (moreLegacy.value()) {
                        return Optional.of(new Replacement(Identifier.withDefaultNamespace("golden_axe"), null));
                    }
                }
                case "AXE_OF_THE_SHREDDED" -> {
                    if (moreLegacy.value()) {
                        return Optional.of(new Replacement(Identifier.withDefaultNamespace("diamond_axe"), null));
                    }
                }
                case "RAIDER_AXE" -> {
                    if (moreLegacy.value()) {
                        return Optional.of(new Replacement(Identifier.withDefaultNamespace("iron_axe"), null));
                    }
                }
            }
            if (data.contains("td_attune_mode")) {
                String path = switch (data.getIntOr("td_attune_mode", -1)) {
                    case 0 -> "stone_sword";
                    case 1 -> "golden_sword";
                    case 2 -> "iron_sword";
                    case 3 -> "diamond_sword";
                    default -> "";
                };
                if (!path.isEmpty()) return Optional.of(new Replacement(Identifier.withDefaultNamespace(path), null));
            }
            if (NoFrillsAPI.itemTextures.containsKey(id)) {
                NoFrillsAPI.ItemTexture textures = NoFrillsAPI.itemTextures.get(id);
                if (!textures.textures().isEmpty()) {
                    ItemStack clone = stack.copy();
                    clone.set(DataComponents.PROFILE, LegacyTextures.getOrInitProfile(id, textures.textures()));
                    return Optional.of(new Replacement(Identifier.parse(textures.model()), clone));
                }
                return Optional.of(new Replacement(Identifier.parse(textures.model()), null));
            }
        }
        return Optional.empty();
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (!instance.isActive()) {
            cache.clear();
        }
    }

    public record Replacement(Identifier identifier, ItemStack stack) {

    }
}
