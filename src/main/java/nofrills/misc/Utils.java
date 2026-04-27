package nofrills.misc;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import nofrills.mixin.AbstractContainerScreenAccessor;
import nofrills.mixin.BossHealthOverlayAccessor;
import nofrills.mixin.PlayerTabOverlayAccessor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static nofrills.Main.*;

public class Utils {
    public static final GuiMessageTag noFrillsIndicator = new GuiMessageTag(0x5ca0bf, null, Component.nullToEmpty("Message from NoFrills mod."), "NoFrills Mod");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final HashSet<String> modernIslands = Sets.newHashSet(
            "Hub",
            "Galatea",
            "Gold Mine",
            "Spider's Den",
            "The Barn",
            "The End",
            "The Park",
            "Crimson Isle",
            "Jerry's Workshop"
    );
    private static final HashSet<String> lootIslands = Sets.newHashSet(
            "Catacombs",
            "Kuudra",
            "Dungeon Hub",
            "Crimson Isle"
    );

    public static void showTitle(MutableComponent title, MutableComponent subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        mc.gui.setTitle(title);
        mc.gui.setSubtitle(subtitle);
        mc.gui.setTimes(fadeInTicks, stayTicks, fadeOutTicks);
    }

    public static void showTitle(String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        showTitle(Component.literal(title), Component.literal(subtitle), fadeInTicks, stayTicks, fadeOutTicks);
    }

    public static boolean isNearlyEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    public static boolean isNearlyEqual(double a, double b) {
        return isNearlyEqual(a, b, 1e-9);
    }

    public static boolean isNearlyEqual(float a, float b) {
        return isNearlyEqual(a, b, 1e-5);
    }

    public static void playSound(SoundEvent event, float volume, float pitch) {
        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, volume));
    }

    public static void playSound(Holder.Reference<SoundEvent> event, float volume, float pitch) {
        playSound(event.value(), volume, pitch);
    }

    public static void playSound(String event, float volume, float pitch) {
        playSound(SoundEvent.createVariableRangeEvent(Identifier.parse(event)), volume, pitch);
    }

    public static void sendMessage(String message) {
        if (mc.player != null && !message.isEmpty()) {
            if (message.startsWith("/")) {
                mc.player.connection.sendCommand(message.substring(1));
            } else {
                mc.player.connection.sendChat(message);
            }
        }
    }

    public static void refillItem(String refill_query, int amount) {
        int total = 0;
        Inventory inv = mc.player.getInventory();
        String query = refill_query.replaceAll("_", " ");
        for (int i = 0; i <= 35; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            String id = getSkyblockId(stack).replaceAll("_", " ");
            String name = toPlain(stack.getHoverName());
            if (query.equalsIgnoreCase(id) || query.equalsIgnoreCase(name)) {
                total += stack.getCount();
            }
        }
        if (total < amount) {
            sendMessage(format("/gfs {} {}", refill_query, amount - total));
        }
    }

    public static MutableComponent getTag() {
        return Component.literal("[NoFrills] ").withColor(0x5ca0bf);
    }

    public static MutableComponent getShortTag() {
        return Component.literal("[NF] ").withColor(0x5ca0bf);
    }

    public static void info(String message) {
        infoRaw(Component.literal(message));
    }

    public static void infoButton(String message, String command) {
        ClickEvent click = new ClickEvent.RunCommand(command);
        infoRaw(Component.literal(message).setStyle(Style.EMPTY.withClickEvent(click)));
    }

    public static void infoLink(String message, String url) {
        ClickEvent click = new ClickEvent.OpenUrl(URI.create(url));
        infoRaw(Component.literal(message).setStyle(Style.EMPTY.withClickEvent(click)));
    }

    public static void infoRaw(MutableComponent message) {
        if (message.getStyle() == null || message.getStyle().getColor() == null) {
            message = message.withColor(0xffffff);
        }
        mc.gui.getChat().addMessage(getTag().append(message), null, GuiMessageSource.SYSTEM_CLIENT, noFrillsIndicator);
    }

    public static void infoFormat(String message, Object... values) {
        infoRaw(Component.literal(format(message, values)));
    }

    public static String getCoordsFormatted(String format) {
        BlockPos pos = mc.player.blockPosition();
        return format(format, pos.getX(), pos.getY(), pos.getZ());

    }

    public static boolean isInZone(String zone, boolean containsCheck) {
        if (containsCheck) {
            return SkyblockData.getLocation().contains(zone);
        }
        return SkyblockData.getLocation().startsWith(zone);
    }

    /**
     * Checks if the provided location matches with the current area on the tab list. For example, isInArea("Private Island") is true if "Area: Private Island" is on the tab list.
     */
    public static boolean isInArea(String area) {
        return SkyblockData.getArea().equals(area);
    }

    public static boolean isInDungeons() {
        return isInArea("Catacombs");
    }

    /**
     * Checks if the player is currently on the specific Dungeon floor. For example, "F7" checks for F7 only, "M7" checks for M7 only, and "7" checks for both of them.
     */
    public static boolean isOnDungeonFloor(String floor) {
        return DungeonUtil.getCurrentFloor().endsWith(floor);
    }

    /**
     * Returns true if the current island has either loot chests or the Croesus NPC.
     */
    public static boolean isInLootArea() {
        return lootIslands.contains(SkyblockData.getArea());
    }

    /**
     * Checks if the player is currently inside the boss room on the specific floor.
     */
    public static boolean isInDungeonBoss(String floor) {
        return isOnDungeonFloor(floor) && switch (floor) {
            case "1", "F1", "M1" -> isInZone(-72, 146, -40, -14, 55, 49);
            case "2", "F2", "M2" -> isInZone(-40, 99, -40, 24, 54, 54);
            case "3", "F3", "M3" -> isInZone(-40, 118, -40, 42, 64, 73);
            case "4", "F4", "M4" -> isInZone(50, 112, 81, -40, 53, -40);
            case "5", "F5", "M5" -> isInZone(50, 112, 118, -40, 53, -8);
            case "6", "F6", "M6" -> isInZone(22, 110, 134, -40, 51, -8);
            case "7", "F7", "M7" -> isInZone(134, 254, 147, -8, 0, -8);
            default -> false;
        };
    }

    public static boolean isInKuudra() {
        return isInArea("Kuudra");
    }

    public static boolean isInChateau() {
        return isInZone(Symbols.zoneRift + " Stillgore Château", false) || isInZone(Symbols.zoneRift + " Oubliette", false);
    }

    public static boolean isOnPrivateIsland() {
        return isInZone(Symbols.zone + " Your Island", false);
    }

    /**
     * Returns true if the player is on any of their garden plots, which doesn't count the barn.
     */
    public static boolean isOnGardenPlot() {
        return SkyblockData.getLines().stream().anyMatch(line -> line.contains("Plot -"));
    }

    /**
     * Returns true if the player is anywhere on their garden
     */
    public static boolean isInGarden() {
        return isInArea("Garden");
    }

    public static boolean isInHub() {
        return isInArea("Hub");
    }

    /**
     * Returns true if the current island is running on a modern Minecraft version and/or running under prediction-based Watchdog.
     */
    public static boolean isOnModernIsland() {
        return modernIslands.contains(SkyblockData.getArea());
    }

    public static boolean isInstanceOver() {
        return SkyblockData.isInstanceOver();
    }

    public static boolean isInSkyblock() {
        return SkyblockData.isInSkyblock();
    }

    public static String getServerAddress() {
        ServerData info = mc.getCurrentServer();
        if (info != null) {
            return toLower(info.ip).trim();
        }
        return "";
    }

    public static boolean isOnHypixel() {
        String address = getServerAddress();
        return address.equals("hypixel.net") || address.endsWith(".hypixel.net");
    }

    public static boolean isOnAlphaNetwork() {
        return getServerAddress().equals("alpha.hypixel.net");
    }

    /**
     * Checks if a PlayerEntity is a real player, and not an enemy or NPC.
     */
    public static boolean isPlayer(Player entity) {
        return entity.getUUID().version() == 4;
    }

    /**
     * Check if the provided entity is a living entity (and in the case of player entities, if it isn't a real player).
     */
    public static boolean isMob(Entity entity) {
        if (entity instanceof Player player) {
            return !isPlayer(player);
        }
        return entity instanceof LivingEntity;
    }

    public static boolean isBaseHealth(LivingEntity entity, float health) {
        float current = entity.getHealth();
        float difference = current - health;
        return current >= health && (current % health == 0 || (current - difference) % health == 0);
    }

    /**
     * Returns the entity's bounding box at their interpolated position.
     */
    public static AABB getLerpedBox(Entity entity, float tickProgress) {
        return entity.getDimensions(Pose.STANDING).makeBoundingBox(entity.getPosition(tickProgress));
    }

    public static List<Entity> getEntities() {
        if (mc.level != null) {
            LevelEntityGetterAdapter<Entity> lookup = (LevelEntityGetterAdapter<Entity>) mc.level.entityStorage.getEntityGetter();
            return new ArrayList<>(lookup.visibleEntities.byId.values());
        }
        return new ArrayList<>();
    }

    public static List<Entity> getOtherEntities(Entity except, AABB box, Predicate<? super Entity> filter) {
        List<Entity> entities = new ArrayList<>();
        for (Entity ent : getEntities()) {
            if (ent != null && ent != except && (filter == null || filter.test(ent)) && ent.getBoundingBox().intersects(box)) {
                entities.add(ent);
            }
        }
        return entities;
    }

    public static List<Entity> getOtherEntities(Entity from, double distX, double distY, double distZ, Predicate<? super Entity> filter) {
        return getOtherEntities(from, AABB.ofSize(from.position(), distX, distY, distZ), filter);
    }

    public static List<Entity> getOtherEntities(Entity from, double dist, Predicate<? super Entity> filter) {
        return getOtherEntities(from, AABB.ofSize(from.position(), dist, dist, dist), filter);
    }

    public static float getTextScale(double dist, float base, float scaling) {
        float distScale = (float) (1 + dist * scaling);
        return Math.max(base * distScale, base);
    }

    public static float getTextScale(double dist, float base) {
        return getTextScale(dist, base, 0.1f);
    }

    public static float getTextScale(Vec3 pos, float base, float scaling) {
        if (mc.player != null) {
            return getTextScale(mc.player.position().distanceTo(pos), base, scaling);
        }
        return 0.0f;
    }

    public static float getTextScale(Vec3 pos, float base) {
        return getTextScale(pos, base, 0.1f);
    }

    public static boolean matchesKey(KeyMapping binding, KeyEvent keyInput, MouseButtonInfo mouseInput) {
        return (keyInput != null && binding.matches(keyInput)) || (mouseInput != null && binding.matchesMouse(new MouseButtonEvent(0, 0, mouseInput)));
    }

    public static boolean matchesKey(KeyMapping binding, KeyEvent keyInput) {
        return matchesKey(binding, keyInput, null);
    }

    public static boolean matchesKey(KeyMapping binding, MouseButtonInfo mouseInput) {
        return matchesKey(binding, null, mouseInput);
    }

    public static void sendPingPacket() {
        ClientPacketListener handler = mc.getConnection();
        if (handler != null) {
            handler.send(new ServerboundPingRequestPacket(Util.getMillis()));
        }
    }

    /**
     * Returns the armor that the entity is wearing.
     */
    public static List<ItemStack> getEntityArmor(LivingEntity entity) {
        if (entity != null) {
            return List.of(
                    entity.getItemBySlot(EquipmentSlot.HEAD),
                    entity.getItemBySlot(EquipmentSlot.CHEST),
                    entity.getItemBySlot(EquipmentSlot.LEGS),
                    entity.getItemBySlot(EquipmentSlot.FEET)
            );
        }
        return List.of();
    }

    public static ItemStack getEntityHelmet(LivingEntity entity) {
        if (entity != null) {
            return entity.getItemBySlot(EquipmentSlot.HEAD);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns the custom data compound of the provided ItemStack, or else null.
     */
    public static CompoundTag getCustomData(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            if (data != null) {
                return data.tag;
            }
        }
        return null;
    }

    /**
     * Returns the Skyblock item ID from the provided NbtCompound, or else an empty string.
     */
    public static String getSkyblockId(CompoundTag customData) {
        if (customData != null && customData.contains("id")) {
            return customData.getString("id").orElse("");
        }
        return "";
    }

    /**
     * Returns the Skyblock item ID from the provided ItemStack, or else an empty string.
     */
    public static String getSkyblockId(ItemStack stack) {
        return getSkyblockId(getCustomData(stack));
    }

    /**
     * Tries to parse the Bazaar/Auction ID tied to the name of the item.
     */
    public static String getMarketId(Component text) {
        String name = toPlain(text);
        if (hasItemQuantity(name)) {
            name = name.substring(0, name.lastIndexOf(" ")).trim();
        }
        if (name.startsWith("Enchanted Book (") && name.endsWith(")")) {
            String enchant = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
            String enchantName = toID(enchant.substring(0, enchant.lastIndexOf(" ")));
            int enchantLevel = parseRoman(enchant.substring(enchant.lastIndexOf(" ") + 1));
            Optional<Style> style = getStyle(text, enchant::equals);
            if (style.isPresent() && hasColor(style.get(), ChatFormatting.LIGHT_PURPLE) && !enchantName.startsWith("ULTIMATE_")) {
                return format("ENCHANTMENT_ULTIMATE_{}_{}", enchantName, enchantLevel);
            }
            return format("ENCHANTMENT_{}_{}", enchantName, enchantLevel);
        }
        if (name.endsWith(" Essence")) {
            return format("ESSENCE_{}", toID(name.substring(0, name.lastIndexOf(" "))));
        }
        if (name.endsWith(" Dye")) {
            return format("DYE_{}", toID(name.substring(0, name.lastIndexOf(" "))));
        }
        if (name.startsWith("Master Skull - Tier ")) {
            return toID(name.replace(" - ", " "));
        }
        if (name.startsWith("[Lvl 1] ")) {
            String petName = name.substring(name.indexOf("]") + 2);
            Optional<Style> styleOptional = getStyle(text, petName::equals);
            String rarity = "COMMON";
            if (styleOptional.isPresent()) {
                Style style = styleOptional.get();
                if (hasColor(style, ChatFormatting.GOLD)) rarity = "LEGENDARY";
                if (hasColor(style, ChatFormatting.DARK_PURPLE)) rarity = "EPIC";
                if (hasColor(style, ChatFormatting.BLUE)) rarity = "RARE";
                if (hasColor(style, ChatFormatting.GREEN)) rarity = "UNCOMMON";
            }
            return format("{}_PET_{}", toID(petName), rarity);
        }
        if (name.endsWith(" Shard")) {
            return ShardData.getId(name);
        }
        return switch (name) {
            case "Shadow Warp" -> "SHADOW_WARP_SCROLL";
            case "Wither Shield" -> "WITHER_SHIELD_SCROLL";
            case "Implosion" -> "IMPLOSION_SCROLL";
            case "Giant's Sword" -> "GIANTS_SWORD";
            case "Warped Stone" -> "AOTE_STONE";
            case "Spirit Boots" -> "THORNS_BOOTS";
            case "Spirit Shortbow" -> "ITEM_SPIRIT_BOW";
            case "Spirit Stone" -> "SPIRIT_DECOY";
            case "Adaptive Blade" -> "STONE_BLADE";
            default -> toID(name);
        };
    }

    /**
     * Returns the Bazaar/Auction ID tied to the item.
     */
    public static String getMarketId(ItemStack stack) {
        CompoundTag data = getCustomData(stack);
        String id = getSkyblockId(data);
        String shardId = ShardData.getId(stack);
        if (!shardId.isEmpty()) {
            return shardId;
        }
        switch (id) {
            case "PET" -> {
                String petInfo = data.getString("petInfo").orElse("");
                if (!petInfo.isEmpty()) {
                    JsonObject petData = JsonParser.parseString(petInfo).getAsJsonObject();
                    return format("{}_PET_{}", petData.get("type").getAsString(), petData.get("tier").getAsString());
                }
                return "UNKNOWN_PET";
            }
            case "RUNE", "UNIQUE_RUNE" -> {
                CompoundTag runeData = data.getCompound("runes").orElse(null);
                if (runeData != null) {
                    String runeId = (String) runeData.keySet().toArray()[0];
                    return format("{}_{}_RUNE", runeId, runeData.getInt(runeId).orElse(0));
                }
                return "EMPTY_RUNE";
            }
            case "ENCHANTED_BOOK" -> {
                CompoundTag enchantData = data.getCompound("enchantments").orElse(null);
                if (enchantData != null) {
                    Set<String> enchants = enchantData.keySet();
                    if (enchants.size() == 1) {
                        String enchantId = (String) enchantData.keySet().toArray()[0];
                        int enchantLevel = enchantData.getInt(enchantId).orElse(0);
                        return format("ENCHANTMENT_{}_{}", toUpper(enchantId), enchantLevel);
                    }
                }
                return "ENCHANTMENT_UNKNOWN";
            }
            case "POTION" -> {
                String potion = data.getString("potion").orElse("");
                if (!potion.isEmpty()) {
                    return format("{}_{}_POTION",
                            toUpper(potion),
                            data.getInt("potion_level").orElse(0)
                    );
                }
                return "UNKNOWN_POTION";
            }
        }
        return id;
    }

    public static boolean hasItemQuantity(String name) {
        return Pattern.matches(".* x[0-9]*", name);
    }

    public static GameProfile getTextures(ItemStack stack) {
        ResolvableProfile profile = stack.getComponents().get(DataComponents.PROFILE);
        if (!stack.isEmpty() && profile != null) {
            return profile.partialProfile();
        }
        return null;
    }

    public static String getTextureUrl(GameProfile profile) {
        if (profile != null) {
            MinecraftSessionService service = mc.services().sessionService();
            Property property = service.getPackedTextures(profile);
            MinecraftProfileTextures textures = service.unpackTextures(property);
            if (textures.skin() != null) {
                return textures.skin().getUrl();
            }
        }
        return "";
    }

    public static String getTextureUrl(ItemStack stack) {
        return getTextureUrl(getTextures(stack));
    }

    public static boolean isTextureEqual(GameProfile profile, String textureId) {
        String url = getTextureUrl(profile);
        if (url != null) {
            return url.endsWith("texture/" + textureId);
        }
        return false;
    }

    public static List<Component> getLoreText(ItemStack stack) {
        ItemLore lore = stack.getComponents().get(DataComponents.LORE);
        if (lore != null) {
            return lore.lines();
        }
        return new ArrayList<>();
    }

    /**
     * Returns every line of the stack's lore with no formatting, or else an empty list.
     */
    public static List<String> getLoreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        for (Component line : getLoreText(stack)) {
            lines.add(toPlain(line).trim());
        }
        return lines;
    }

    /**
     * Returns the right click ability line if found in the item's lore, or else an empty string.
     */
    public static String getRightClickAbility(ItemStack stack) {
        for (String line : getLoreLines(stack)) {
            if (line.contains("Ability: ") && line.endsWith("RIGHT CLICK")) {
                return line;
            }
        }
        return "";
    }

    public static boolean hasRightClickAbility(ItemStack stack) {
        return !getRightClickAbility(stack).isEmpty();
    }

    public static boolean hasEitherStat(ItemStack stack, String... stats) {
        List<String> lines = getLoreLines(stack);
        Iterator<String> iterator = Arrays.stream(stats).iterator();
        while (iterator.hasNext()) {
            String stat = iterator.next();
            for (String line : lines) {
                if (line.startsWith(stat + ":")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tries to find ground (any block that isn't air) below the specified BlockPos, and returns the BlockPos of that block if found. Otherwise, returns the same BlockPos.
     *
     * @param maxDistance The maximum downward Y distance the check will travel
     */
    public static BlockPos findGround(BlockPos pos, int maxDistance) {
        int dist = Math.clamp(maxDistance, 0, 256);
        for (int i = 0; i <= dist; i++) {
            BlockPos below = pos.below(i);
            if (!mc.level.getBlockState(below).isAir()) {
                return below;
            }
        }
        return pos;
    }

    /**
     * Makes the 1st letter of each word in the string uppercase.
     *
     * @param replaceUnderscores if true, automatically replace all underscores with spaces
     */
    public static String uppercaseFirst(String text, boolean replaceUnderscores) {
        return Arrays.stream(replaceUnderscores ? text.replaceAll("_", " ").split("\\s") : text.split("\\s"))
                .map(word -> Character.toTitleCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" ")).trim();
    }

    public static void atomicWrite(Path path, String content) throws IOException {
        Path parent = path.getParent();
        String fileName = path.getFileName().toString();
        Path tempPath = parent.resolve(format("{}-Temp-{}.{}",
                fileName.substring(0, fileName.indexOf(".")),
                Util.getMillis(),
                fileName.substring(fileName.indexOf(".") + 1)
        ));
        if (!Files.exists(parent)) {
            Files.createDirectory(parent);
        }
        Files.writeString(tempPath, content);
        try {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.deleteIfExists(tempPath);
    }

    public static void atomicWrite(Path path, JsonObject content) throws IOException {
        atomicWrite(path, GSON.toJson(content));
    }

    private static int getVersionNumber(String version) {
        String[] numbers = version.split("\\.");
        if (numbers.length >= 3) {
            return parseInt(numbers[0]).orElse(0) * 1000 + parseInt(numbers[1]).orElse(0) * 100 + parseInt(numbers[2]).orElse(0);
        }
        return 0;
    }

    public static void checkUpdate(boolean notifyIfMatch) {
        Thread.startVirtualThread(() -> {
            try {
                String version = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
                InputStream connection = URI.create("https://raw.githubusercontent.com/WhatYouThing/NoFrills/refs/heads/main/gradle.properties").toURL().openStream();
                for (String line : IOUtils.toString(connection, StandardCharsets.UTF_8).split("\n")) {
                    if (line.startsWith("mod_version=")) {
                        String newest = line.replace("mod_version=", "");
                        if (getVersionNumber(newest) > getVersionNumber(version)) {
                            infoLink(format("§a§lNew version available! §aClick here to open the Modrinth releases page. §7Current: {}, Newest: {}", version, newest), "https://modrinth.com/mod/nofrills/versions");
                            return;
                        }
                    }
                }
                if (notifyIfMatch) {
                    info("§aNoFrills is up to date.");
                }
            } catch (IOException exception) {
                info("§cAn error occurred while checking for an update. Additional information can be found in the log.");
                LOGGER.error("NoFrills update check failed.", exception);
            }
        });
    }

    /**
     * Checks if our player entity is currently within an area, made from 2 sets of coordinates.
     */
    public static boolean isInZone(double x1, double y1, double z1, double x2, double y2, double z2) {
        AABB area = new AABB(x1, y1, z1, x2, y2, z2);
        return area.contains(mc.player.position());
    }

    /**
     * Used for Entity mixins to check if the mixin is being applied to our own player entity.
     * <p>
     * <code>
     * if (isSelf(this)) {
     * do stuff...
     * }
     * </code>
     */
    public static boolean isSelf(Object entity) {
        return entity == mc.player;
    }

    public static float horizontalDistance(Vec3 from, Vec3 to) {
        float x = (float) (from.x() - to.x());
        float z = (float) (from.z() - to.z());
        return Mth.sqrt(x * x + z * z);
    }

    public static float horizontalDistance(Entity from, Entity to) {
        return horizontalDistance(from.position(), to.position());
    }

    /**
     * Modified version of Minecraft's raycast function, which considers every block hit as a 1x1 cube, matching how Hypixel performs their raycast for the Ether Transmission ability.
     */
    public static HitResult raycastFullBlock(Entity entity, double maxDistance, float tickDelta) {
        Vec3 height = entity.getPosition(tickDelta).add(0, isOnModernIsland() ? 1.27 : 1.54, 0);
        Vec3 camPos = entity.getEyePosition(tickDelta);
        Vec3 rot = entity.getViewVector(tickDelta);
        Vec3 pos = new Vec3(camPos.x(), height.y(), camPos.z());
        Vec3 end = pos.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);
        EtherwarpRaycastContext context = new EtherwarpRaycastContext(pos, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entity);
        return entity.level().clip(context);
    }

    /**
     * Tries to find the entity that the provided Armor Stand belongs to, based on horizontal distance.
     */
    public static Entity findNametagOwner(Entity armorStand, List<Entity> otherEntities) {
        Entity entity = null;
        float lowestDist = 2.0f;
        double maxY = armorStand.position().y();
        for (Entity ent : otherEntities) {
            float dist = horizontalDistance(ent.position(), armorStand.position());
            if (!(ent instanceof ArmorStand) && ent.position().y() < maxY && dist < lowestDist) {
                entity = ent;
                lowestDist = dist;
            }
        }
        return entity;
    }

    /**
     * Checks if the provided ItemStack has a glint override flag. Ignores the default flag to work correctly with items such as Nether Stars.
     */
    public static boolean hasGlint(ItemStack stack) {
        Boolean component = stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        return component != null && component;
    }

    public static List<String> getTabListLines() {
        return SkyblockData.getTabListLines();
    }

    /**
     * Returns every line of text from the tab list footer, otherwise an empty list.
     */
    public static List<String> getFooterLines() {
        List<String> list = new ArrayList<>();
        Component footer = ((PlayerTabOverlayAccessor) mc.gui.getTabList()).getFooter();
        if (footer != null) {
            String[] lines = footer.getString().split("\n");
            for (String line : lines) {
                String l = line.trim();
                if (!l.isEmpty()) {
                    list.add(l);
                }
            }
        }
        return list;
    }

    public static List<LerpingBossEvent> getBossBars() {
        return ((BossHealthOverlayAccessor) mc.gui.getBossOverlay()).getEvents().values().stream().toList();
    }

    /**
     * Returns every slot that is part of the container screen handler, excluding the player inventory slots.
     *
     * @param inverse if true, returns the slots that are part of the player inventory instead of the container itself.
     */
    public static List<Slot> getContainerSlots(ChestMenu handler, boolean inverse) {
        if (inverse) {
            return handler.slots.stream().filter(slot -> slot.index >= handler.getRowCount() * 9).toList();
        }
        return handler.slots.stream().filter(slot -> slot.index < handler.getRowCount() * 9).toList();
    }

    public static List<Slot> getContainerSlots(ChestMenu handler) {
        return getContainerSlots(handler, false);
    }

    public static List<Slot> getContainerSlots(AbstractContainerMenu handler, boolean inverse) {
        if (handler instanceof ChestMenu containerHandler) {
            return getContainerSlots(containerHandler, inverse);
        }
        return List.of();
    }

    public static List<Slot> getContainerSlots(AbstractContainerMenu handler) {
        return getContainerSlots(handler, false);
    }

    public static ItemStack getHeldItem() {
        return mc.player != null ? mc.player.getMainHandItem() : ItemStack.EMPTY;
    }

    public static Slot getFocusedSlot() {
        return mc.screen != null ? ((AbstractContainerScreenAccessor) mc.screen).getHoveredSlot() : null;
    }

    private static int romanToInt(Character roman) {
        return switch (Character.toUpperCase(roman)) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            case 'L' -> 50;
            case 'C' -> 100;
            case 'D' -> 500;
            case 'M' -> 1000;
            default -> 0;
        };
    }

    /**
     * Converts a roman numeral to an integer. Returns 0 if the numeral couldn't be parsed.
     */
    public static int parseRoman(String roman) {
        int result = 0;
        for (int i = 0; i < roman.length(); i++) {
            int number = romanToInt(roman.charAt(i));
            if (number == 0) {
                return 0;
            }
            if (i != roman.length() - 1) {
                int nextNumber = romanToInt(roman.charAt(i + 1));
                if (number < nextNumber) {
                    result -= number;
                } else {
                    result += number;
                }
            } else {
                result += number;
            }
        }
        return result;
    }

    public static String toLower(String string) {
        return string.toLowerCase(Locale.ROOT);
    }

    public static String toUpper(String string) {
        return string.toUpperCase(Locale.ROOT);
    }

    public static String toID(String string) {
        return toUpper(string.replace("'s", "").replaceAll(" ", "_"));
    }

    /**
     * Gets the string out of a Text object and removes any formatting codes.
     */
    public static String toPlain(Component text) {
        if (text != null) {
            return ChatFormatting.stripFormatting(text.getString());
        }
        return "";
    }

    public static String toAscii(String string) {
        return string.chars().filter(c -> c <= 127).mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining());
    }

    public static Optional<Style> getStyle(Component text, Predicate<String> predicate) {
        return text.visit((textStyle, textString) -> {
            if (predicate.test(textString)) {
                return Optional.of(textStyle);
            }
            return Optional.empty();
        }, Style.EMPTY);
    }

    public static boolean hasColor(Style style, ChatFormatting color) {
        return color.getColor() != null && hasColor(style, color.getColor());
    }

    public static boolean hasColor(Style style, int hex) {
        return style != null && style.getColor() != null && style.getColor().getValue() == hex;
    }

    public static Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> parseHex(String value) {
        try {
            return Optional.of((int) Long.parseLong(value.replace("0x", ""), 16));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Double> parseDouble(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static String parseDate(Calendar calendar) {
        return format("{} {}",
                calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()),
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(calendar.getTime())
        );
    }

    public static int difference(int first, int second) {
        return Math.abs(Math.abs(first) - Math.abs(second));
    }

    /**
     * Formats the string by replacing each set of curly brackets "{}" with one of the values in order, similarly to Rust's format macro.
     */
    public static String format(String string, Object... values) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (String section : Splitter.on("{}").split(string)) {
            builder.append(section);
            if (index < values.length) {
                builder.append(values[index]);
            }
            index++;
        }
        return builder.toString();
    }

    public static String formatDecimal(double number) {
        return formatDecimal(number, 2);
    }

    public static String formatDecimal(float number) {
        return formatDecimal(number, 2);
    }

    public static String formatDecimal(double number, int spaces) {
        return new DecimalFormat("0." + "0".repeat(spaces)).format(number);
    }

    public static String formatDecimal(float number, int spaces) {
        return formatDecimal((double) number, spaces);
    }

    public static String formatSeparator(long number) {
        return new Formatter().format(Locale.ENGLISH, "%,d", number).toString();
    }

    public static String formatSeparator(int number) {
        return formatSeparator((long) number);
    }

    public static String formatSeparator(double number) {
        return new Formatter().format(Locale.ENGLISH, "%,.1f", number).toString();
    }

    public static String formatSeparator(float number) {
        return formatSeparator((double) number);
    }

    public static long getMeasuringTime() {
        return Util.getMillis();
    }

    public static long getTimestamp() {
        return Instant.now().toEpochMilli();
    }

    public static String ticksToTime(long ticks) {
        if (ticks < 20) {
            return "0s";
        }
        StringBuilder builder = new StringBuilder();
        long current = ticks;
        String[] units = new String[]{"h", "m", "s"};
        int[] durations = new int[]{72000, 1200, 20};
        for (int i = 0; i <= 2; i++) {
            int amount = 0;
            while (current >= durations[i]) {
                amount++;
                current -= durations[i];
            }
            if (amount > 0) {
                builder.append(amount).append(units[i]);
            }
        }
        return builder.toString();
    }

    public static String millisecondsToTime(long ms) {
        return ticksToTime(ms / 50);
    }

    public static String getPercentageColor(double percentage, boolean inverse) {
        if (percentage > 0.66) {
            return inverse ? "§a" : "§c";
        }
        if (percentage > 0.33) {
            return "§6";
        }
        return inverse ? "§c" : "§a";
    }

    public static String getPercentageColor(float percentage, boolean inverse) {
        return getPercentageColor((double) percentage, inverse);
    }

    public static String getPercentageColor(double percentage) {
        return getPercentageColor(percentage, false);
    }

    public static String getPercentageColor(float percentage) {
        return getPercentageColor((double) percentage, false);
    }

    public static void setScreen(Screen screen) {
        mc.schedule(() -> mc.setScreen(screen));
    }

    public static class Symbols {
        public static String zone = "⏣";
        public static String zoneRift = "ф";
        public static String star = "✯";
        public static String heart = "❤";
        public static String skull = "☠";
        public static String format = "§";
        public static String vampLow = "҉";
        public static String check = "✔";
        public static String cross = "✖";
        public static String bingo = "Ⓑ";
        public static String aquatic = "⚓";
        public static String magmatic = "♆";
    }
}
