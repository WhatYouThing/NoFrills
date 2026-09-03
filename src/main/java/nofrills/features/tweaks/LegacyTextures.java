package nofrills.features.tweaks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import nofrills.config.*;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.NoFrillsAPI;
import nofrills.misc.Utils;

import java.util.Optional;
import java.util.WeakHashMap;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

@EventListener
public class LegacyTextures {
    public static final Feature instance = new Feature("legacyTextures");

    public static final SettingBool revertItems = new SettingBool(true, "revertItems", instance);
    public static final SettingBool unlockPackPos = new SettingBool(false, "unlockPackPos", instance);
    public static final SettingBool forcePackPos = new SettingBool(false, "forcePackPos", instance);
    public static final SettingBool noTooltipStyle = new SettingBool(false, "noTooltipStyle", instance);
    public static final SettingBool noBowCooldown = new SettingBool(false, "noBowCooldown", instance);
    public static final SettingBool moreLegacy = new SettingBool(false, "moreLegacy", instance);
    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance);

    private static final DataFile textures = Config.getDataFile("LegacyTexturesCache.json");
    private static final WeakHashMap<ItemStack, Optional<Identifier>> identifierCache = new WeakHashMap<>();
    private static final WeakHashMap<DataComponentHolder, Optional<ResolvableProfile>> profileCache = new WeakHashMap<>();
    private static boolean texturesLoaded = false;

    public static Optional<Identifier> replaceIfNeeded(ItemStack stack) {
        return identifierCache.computeIfAbsent(stack, (_) -> {
            Identifier model = stack.get(DataComponents.ITEM_MODEL);
            if (model != null && model.getNamespace().equals("hypixel_skyblock")) {
                CompoundTag data = Utils.getCustomData(stack);
                String id = Utils.getSkyblockId(data);
                if (id.isEmpty() || isWhitelisted(id)) return Optional.empty();
                switch (id) {
                    case "VOIDEDGE_KATANA", "VORPAL_KATANA", "ATOMSPLIT_KATANA" -> {
                        String path = mc.player.getCooldowns().isOnCooldown(stack) ? "golden_sword" : "diamond_sword";
                        return Optional.of(Identifier.withDefaultNamespace(path));
                    }
                    case "RAGNAROCK_AXE", "DAEDALUS_AXE", "STARRED_DAEDALUS_AXE" -> {
                        if (moreLegacy.value()) {
                            return Optional.of(Identifier.withDefaultNamespace("golden_axe"));
                        }
                    }
                    case "AXE_OF_THE_SHREDDED" -> {
                        if (moreLegacy.value()) {
                            return Optional.of(Identifier.withDefaultNamespace("diamond_axe"));
                        }
                    }
                    case "RAIDER_AXE" -> {
                        if (moreLegacy.value()) {
                            return Optional.of(Identifier.withDefaultNamespace("iron_axe"));
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
                    if (!path.isEmpty()) return Optional.of(Identifier.withDefaultNamespace(path));
                }
                if (textures.get().has(id)) {
                    return Optional.of(Identifier.parse(textures.get().get(id).getAsJsonObject().get("model").getAsString()));
                }
            }
            return Optional.empty();
        });
    }

    public static Optional<ResolvableProfile> replaceProfileIfNeeded(DataComponentHolder holder) {
        return profileCache.computeIfAbsent(holder, (_) -> {
            DataComponentMap components = holder.getComponents();
            Identifier model = components.get(DataComponents.ITEM_MODEL);
            CustomData data = components.get(DataComponents.CUSTOM_DATA);
            if (data != null && model != null && model.getNamespace().equals("hypixel_skyblock")) {
                String id = data.tag.getStringOr("id", "");
                if (id.isEmpty() || isWhitelisted(id)) return Optional.empty();
                if (textures.get().has(id)) {
                    JsonObject object = textures.get().get(id).getAsJsonObject();
                    String payload = object.has("textures") ? object.get("textures").getAsString() : "";
                    if (!payload.isEmpty()) {
                        return Optional.of(Utils.toResolvableProfile(payload));
                    }
                }
            }
            return Optional.empty();
        });
    }

    public static boolean isWhitelisted(String id) {
        return data.value().has("whitelist") && data.value().get("whitelist").getAsJsonArray().contains(new JsonPrimitive(id));
    }

    public static void whitelistHeldItem() {
        String id = Utils.getSkyblockId(Utils.getHeldItem());
        if (id.isEmpty()) {
            Utils.infoRaw(Component.literal("Held item has no Skyblock ID, unable to whitelist.").withStyle(ChatFormatting.RED));
            return;
        }
        data.edit(obj -> {
            if (!obj.has("whitelist")) {
                obj.add("whitelist", new JsonArray());
            }
            JsonArray array = obj.get("whitelist").getAsJsonArray();
            if (array.remove(new JsonPrimitive(id))) {
                Utils.infoRaw(Component.literal("Held item removed from model revert whitelist.").withStyle(ChatFormatting.YELLOW));
            } else {
                Utils.infoRaw(Component.literal("Held item added to model revert whitelist.").withStyle(ChatFormatting.GREEN));
                array.add(id);
            }
        });
        identifierCache.clear();
        profileCache.clear();
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (instance.isActive()) {
            if (!texturesLoaded) {
                Thread.startVirtualThread(() -> {
                    try {
                        textures.set(NoFrillsAPI.makeRequestObject("v1/items/get-item-textures/"));
                    } catch (Exception exception) {
                        LOGGER.error("Failed to refresh item textures from NoFrills API.", exception);
                        texturesLoaded = false;
                    }
                });
            }
            texturesLoaded = true;
        }
    }
}
