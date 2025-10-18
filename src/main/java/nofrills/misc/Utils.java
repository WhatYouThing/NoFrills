package nofrills.misc;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.entity.ClientEntityManager;
import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLookup;
import nofrills.events.WorldTickEvent;
import nofrills.mixin.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static nofrills.Main.*;

public class Utils {
    public static final MessageIndicator noFrillsIndicator = new MessageIndicator(0x5ca0bf, null, Text.of("Message from NoFrills mod."), "NoFrills Mod");
    private static final Random soundRandom = Random.create(0);
    private static final HashSet<String> abilityWhitelist = Sets.newHashSet(
            "ABINGOPHONE",
            "SUPERBOOM_TNT",
            "INFINITE_SUPERBOOM_TNT",
            "ARROW_SWAPPER",
            "PUMPKIN_LAUNCHER",
            "SNOW_CANNON",
            "SNOW_BLASTER",
            "SNOW_HOWITZER"
    );
    private static final HashSet<String> modernIslands = Sets.newHashSet(
            "The Park",
            "Galatea",
            "Catacombs"
    );
    private static Screen newScreen = null;

    public static void showTitle(String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        mc.inGameHud.setTitle(Text.of(title));
        mc.inGameHud.setSubtitle(Text.of(subtitle));
        mc.inGameHud.setTitleTicks(fadeInTicks, stayTicks, fadeOutTicks);
    }

    public static void showTitleCustom(String title, int stayTicks, int yOffset, float scale, RenderColor color) {
        ((TitleRendering) mc.inGameHud).nofrills_mod$setRenderTitle(title, stayTicks, yOffset, scale, color);
    }

    public static boolean isRenderingCustomTitle() {
        return ((TitleRendering) mc.inGameHud).nofrills_mod$isRenderingTitle();
    }

    public static void playSound(SoundEvent event, SoundCategory category, float volume, float pitch) {
        Vec3d coords = mc.cameraEntity.getPos();
        mc.getSoundManager().play(new PositionedSoundInstance(event, category, volume, pitch, soundRandom, coords.getX(), coords.getY(), coords.getZ()));
    }

    public static void playSound(RegistryEntry.Reference<SoundEvent> event, SoundCategory category, float volume, float pitch) {
        playSound(event.value(), category, volume, pitch);
    }

    public static void sendMessage(String message) {
        if (mc.player != null && !message.isEmpty()) {
            if (message.startsWith("/")) {
                mc.player.networkHandler.sendChatCommand(message.substring(1));
            } else {
                mc.player.networkHandler.sendChatMessage(message);
            }
        }
    }

    public static MutableText getTag() {
        return Text.literal("[NoFrills] ").withColor(0x5ca0bf);
    }

    public static MutableText getShortTag() {
        return Text.literal("[NF] ").withColor(0x5ca0bf);
    }

    public static void info(String message) {
        infoRaw(Text.literal(message));
    }

    public static void infoButton(String message, String command) {
        ClickEvent click = new ClickEvent.RunCommand(command);
        infoRaw(Text.literal(message).setStyle(Style.EMPTY.withClickEvent(click)));
    }

    public static void infoLink(String message, String url) {
        ClickEvent click = new ClickEvent.OpenUrl(URI.create(url));
        infoRaw(Text.literal(message).setStyle(Style.EMPTY.withClickEvent(click)));
    }

    public static void infoRaw(MutableText message) {
        mc.inGameHud.getChatHud().addMessage(getTag().append(message.withColor(0xffffff)).append("§r"), null, noFrillsIndicator);
    }

    public static void infoFormat(String message, Object... values) {
        infoRaw(Text.literal(format(message, values)));
    }

    public static String getCoordsFormatted(String format) {
        BlockPos pos = mc.player.getBlockPos();
        return Utils.format(format, pos.getX(), pos.getY(), pos.getZ());

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
        return isInZone(Symbols.zone + " The Catacombs", false);
    }

    /**
     * Checks if the player is currently on the specific Dungeon floor. For example, "F7" checks for F7 only, "M7" checks for M7 only, and "7" checks for both of them.
     */
    public static boolean isOnDungeonFloor(String floor) {
        return isInDungeons() && SkyblockData.getLocation().endsWith(floor + ")");
    }

    /**
     * Checks if the player is currently inside the boss room on the specific floor.
     */
    public static boolean isInDungeonBoss(String floor) {
        return isOnDungeonFloor(floor) && switch (floor) {
            case "4" -> isInZone(50, 112, 81, -40, 53, -40);
            case "5" -> isInZone(50, 112, 118, -40, 53, -8);
            case "6" -> isInZone(22, 110, 134, -40, 51, -8);
            case "7" -> isInZone(134, 254, 147, -8, 0, -8);
            default -> false;
        };
    }

    public static boolean isInKuudra() {
        return SkyblockData.getArea().equals("Kuudra");
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
        return isInZone(Symbols.zone + " The Garden", true) || isOnGardenPlot();
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

    /**
     * Checks if a PlayerEntity is a real player, and not an enemy or NPC. Some NPCs might falsely return true for a few seconds after spawning.
     */
    public static boolean isPlayer(PlayerEntity entity) {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler != null) {
            PlayerListEntry listEntry = handler.getPlayerListEntry(entity.getUuid());
            if (listEntry != null) {
                String displayName = listEntry.getProfile().getName();
                if (displayName != null) {
                    String name = Formatting.strip(displayName);
                    return !name.isEmpty() && !name.contains(" ");
                }
            }
        }
        return entity == mc.player;
    }

    /**
     * Check if the provided entity is a living entity (and in the case of player entities, if it isn't a real player).
     */
    public static boolean isMob(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            return !isPlayer(player);
        }
        return entity instanceof LivingEntity;
    }

    public static boolean isBaseHealth(LivingEntity entity, float health) {
        return entity.getHealth() >= health && entity.getHealth() % health == 0;
    }

    /**
     * Returns the entity's bounding box at their interpolated position.
     */
    public static Box getLerpedBox(Entity entity, float tickProgress) {
        return entity.getDimensions(EntityPose.STANDING).getBoxAt(entity.getLerpedPos(tickProgress));
    }

    @SuppressWarnings("unchecked")
    public static List<Entity> getEntities() {
        if (mc.world != null) { // only powerful wizards may cast such obscene spells
            ClientEntityManager<Entity> manager = ((ClientWorldAccessor) mc.world).getManager();
            EntityLookup<?> lookup = ((ClientEntityManagerAccessor<?>) manager).getLookup();
            EntityIndex<?> index = ((SimpleEntityLookupAccessor<?>) lookup).getIndex();
            Int2ObjectMap<?> map = ((EntityIndexAccessor<?>) index).getEntityMap();
            return (List<Entity>) new ArrayList<>(map.values());
        }
        return new ArrayList<>();
    }

    public static List<Entity> getOtherEntities(Entity except, Box box, Predicate<? super Entity> filter) {
        List<Entity> entities = new ArrayList<>();
        for (Entity ent : getEntities()) {
            if (ent != null && ent != except && (filter == null || filter.test(ent)) && ent.getBoundingBox().intersects(box)) {
                entities.add(ent);
            }
        }
        return entities;
    }

    public static List<Entity> getOtherEntities(Entity from, double distX, double distY, double distZ, Predicate<? super Entity> filter) {
        return getOtherEntities(from, Box.of(from.getPos(), distX, distY, distZ), filter);
    }

    public static List<Entity> getOtherEntities(Entity from, double dist, Predicate<? super Entity> filter) {
        return getOtherEntities(from, Box.of(from.getPos(), dist, dist, dist), filter);
    }

    public static void sendPingPacket() {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler != null) {
            handler.sendPacket(new QueryPingC2SPacket(Util.getMeasuringTimeMs()));
        }
    }

    /**
     * Returns the armor that the entity is wearing.
     */
    public static List<ItemStack> getEntityArmor(LivingEntity entity) {
        return List.of(
                entity.getEquippedStack(EquipmentSlot.HEAD),
                entity.getEquippedStack(EquipmentSlot.CHEST),
                entity.getEquippedStack(EquipmentSlot.LEGS),
                entity.getEquippedStack(EquipmentSlot.FEET)
        );
    }

    /**
     * Returns the custom data compound of the provided ItemStack, or else null.
     */
    public static NbtCompound getCustomData(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (data != null) {
                return ((NbtComponentAccessor) (Object) data).get(); // casting a spell
            }
        }
        return null;
    }

    /**
     * Returns the Skyblock item ID from the provided NbtCompound, or else an empty string.
     */
    public static String getSkyblockId(NbtCompound customData) {
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

    public static GameProfile getTextures(ItemStack stack) {
        ProfileComponent profile = stack.getComponents().get(DataComponentTypes.PROFILE);
        if (!stack.isEmpty() && profile != null) {
            return profile.gameProfile();
        }
        return null;
    }

    public static String getTextureUrl(GameProfile profile) {
        if (profile != null) {
            MinecraftSessionService service = mc.getSessionService();
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

    /**
     * Returns every line of the stack's lore with no formatting, or else an empty list.
     */
    public static List<String> getLoreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                lines.add(toPlain(line).trim());
            }
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
        String id = getSkyblockId(stack);
        if (!id.isEmpty()) {
            if (id.startsWith("ABIPHONE")) {
                return true;
            }
            if (abilityWhitelist.contains(id)) {
                return true;
            }
        }
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
            BlockPos below = pos.down(i);
            if (!mc.world.getBlockState(below).isAir()) {
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

    private static int getVersionNumber(String version) {
        try {
            String[] numbers = version.split("\\.");
            return Integer.parseInt(numbers[0]) * 1000 + Integer.parseInt(numbers[1]) * 100 + Integer.parseInt(numbers[2]);
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    public static void checkUpdate(boolean notifyIfMatch) {
        new Thread(() -> {
            try {
                String version = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
                InputStream connection = URI.create("https://raw.githubusercontent.com/WhatYouThing/NoFrills/refs/heads/main/gradle.properties").toURL().openStream();
                for (String line : IOUtils.toString(connection, StandardCharsets.UTF_8).split("\n")) {
                    if (line.startsWith("mod_version=")) {
                        String newest = line.replace("mod_version=", "");
                        if (getVersionNumber(newest) > getVersionNumber(version)) {
                            infoLink(Utils.format("§a§lNew version available! §aClick here to open the Modrinth releases page. §7Current: {}, Newest: {}", version, newest), "https://modrinth.com/mod/nofrills/versions");
                            return;
                        }
                    }
                }
                if (notifyIfMatch) {
                    info("§aNoFrills is up to date.");
                }
            } catch (IOException e) {
                info("§cAn error occurred while checking for an update. Additional information can be found in the log.");
                StringBuilder trace = new StringBuilder();
                for (StackTraceElement element : e.getStackTrace()) {
                    trace.append("\n\tat ").append(element.toString());
                }
                LOGGER.error("{}{}", e.getMessage(), trace);
            }
        }).start();
    }

    /**
     * Checks if our player entity is currently within an area, made from 2 sets of coordinates.
     */
    public static boolean isInZone(double x1, double y1, double z1, double x2, double y2, double z2) {
        Box area = new Box(x1, y1, z1, x2, y2, z2);
        return area.contains(mc.player.getPos());
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

    public static float horizontalDistance(Vec3d from, Vec3d to) {
        float x = (float) (from.getX() - to.getX());
        float z = (float) (from.getZ() - to.getZ());
        return MathHelper.sqrt(x * x + z * z);
    }

    /**
     * Modified version of Minecraft's raycast function, which considers every block hit as a 1x1 cube, matching how Hypixel performs their raycast for the Ether Transmission ability.
     */
    public static HitResult raycastFullBlock(Entity entity, double maxDistance, float tickDelta) {
        Vec3d height = entity.getLerpedPos(tickDelta).add(0, isOnModernIsland() && !isInDungeons() ? 1.27 : 1.54, 0);
        Vec3d camPos = entity.getCameraPosVec(tickDelta);
        Vec3d rot = entity.getRotationVec(tickDelta);
        Vec3d pos = new Vec3d(camPos.getX(), height.getY(), camPos.getZ());
        Vec3d end = pos.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);
        RaycastContext context = new RaycastContext(pos, end, RaycastContext.ShapeType.OUTLINE, net.minecraft.world.RaycastContext.FluidHandling.ANY, entity);
        ((RaycastOptions) context).nofrills_mod$setConsiderAllFull(true);
        return entity.getWorld().raycast(context);
    }

    /**
     * Tries to find the entity that the provided Armor Stand belongs to, based on horizontal distance.
     */
    public static Entity findNametagOwner(Entity armorStand, List<Entity> otherEntities) {
        Entity entity = null;
        float lowestDist = 2.0f;
        double maxY = armorStand.getPos().getY();
        for (Entity ent : otherEntities) {
            float dist = horizontalDistance(ent.getPos(), armorStand.getPos());
            if (!(ent instanceof ArmorStandEntity) && ent.getPos().getY() < maxY && dist < lowestDist) {
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
        Optional<? extends Boolean> component = stack.getComponentChanges().get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        return component != null && component.isPresent();
    }

    public static List<String> getTabListLines() {
        List<String> lines = new ArrayList<>();
        if (mc.getNetworkHandler() != null) {
            for (PlayerListEntry entry : new ArrayList<>(mc.getNetworkHandler().getPlayerList())) {
                if (entry.getDisplayName() != null) {
                    lines.add(toPlain(entry.getDisplayName()).trim());
                }
            }
        }
        return lines;
    }

    /**
     * Returns every line of text from the tab list footer, otherwise an empty list.
     */
    public static List<String> getFooterLines() {
        List<String> list = new ArrayList<>();
        Text footer = ((PlayerListHudAccessor) mc.inGameHud.getPlayerListHud()).getFooter();
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

    /**
     * Returns every slot that is part of the container screen handler, excluding the player inventory slots.
     */
    public static List<Slot> getContainerSlots(GenericContainerScreenHandler handler) {
        Inventory inventory = handler.getInventory();
        List<Slot> slots = new ArrayList<>(handler.slots);
        slots.removeIf(slot -> inventory.getStack(slot.id).equals(ItemStack.EMPTY));
        return slots;
    }

    public static ItemStack getHeldItem() {
        return mc.player != null ? mc.player.getMainHandStack() : ItemStack.EMPTY;
    }

    public static Slot getFocusedSlot() {
        return mc.currentScreen != null ? ((HandledScreenAccessor) mc.currentScreen).getFocusedSlot() : null;
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

    /**
     * Gets the string out of a Text object and removes any formatting codes.
     */
    public static String toPlain(Text text) {
        if (text != null) {
            return Formatting.strip(text.getString());
        }
        return "";
    }

    public static Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
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

    /**
     * Formats the string by replacing each set of curly brackets "{}" with one of the values in order, similarly to Rust's format macro.
     */
    public static String format(String string, Object... values) {
        int lastIndex = 0;
        Iterator<Object> iterator = Arrays.stream(values).iterator();
        while (iterator.hasNext()) {
            Object value = iterator.next();
            int bracket = string.indexOf("{}", lastIndex);
            if (bracket != -1) {
                string = string.substring(0, bracket) + (value + "") + string.substring(bracket + 2);
                lastIndex = bracket + 2;
            }
        }
        return string;
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
        return String.format("%,d", number);
    }

    public static String formatSeparator(int number) {
        return formatSeparator((long) number);
    }

    public static String formatSeparator(double number) {
        return String.format("%,.1f", number);
    }

    public static String formatSeparator(float number) {
        return formatSeparator((double) number);
    }

    public static void setScreen(Screen screen) {
        newScreen = screen;
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (newScreen != null) {
            mc.setScreen(newScreen);
            newScreen = null;
        }
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
