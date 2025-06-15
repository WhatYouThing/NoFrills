package nofrills.misc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import nofrills.config.Config;
import nofrills.events.WorldTickEvent;
import nofrills.features.LeapOverlay;
import nofrills.mixin.HandledScreenAccessor;
import nofrills.mixin.PlayerListHudAccessor;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static nofrills.Main.*;

public class Utils {
    public static final MessageIndicator noFrillsIndicator = new MessageIndicator(0xff5555, null, Text.of("Message from NoFrills mod."), "NoFrills Mod");
    public static final Pattern partyMessagePattern = Pattern.compile("Party > .*: .*");
    private static final Random soundRandom = Random.create(0);
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private static final List<String> abilityWhitelist = List.of(
            "SUPERBOOM_TNT",
            "INFINITE_SUPERBOOM_TNT",
            "ARROW_SWAPPER",
            "PUMPKIN_LAUNCHER",
            "SNOW_CANNON",
            "SNOW_BLASTER",
            "SNOW_HOWITZER"
    );
    private static Screen newScreen = null;

    public static void showTitle(String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        mc.inGameHud.setTitle(Text.of(title));
        mc.inGameHud.setSubtitle(Text.of(subtitle));
        mc.inGameHud.setTitleTicks(fadeInTicks, stayTicks, fadeOutTicks);
    }

    public static void showTitleCustom(String title, int stayTicks, int yOffset, float scale, int color) {
        ((TitleRendering) mc.inGameHud).nofrills_mod$setRenderTitle(title, stayTicks, yOffset, scale, color);
    }

    public static boolean isRenderingCustomTitle() {
        return ((TitleRendering) mc.inGameHud).nofrills_mod$isRenderingTitle();
    }

    public static void playSound(SoundEvent event, SoundCategory category, float volume, float pitch) {
        Vec3d coords = mc.cameraEntity.getPos();
        PositionedSoundInstance sound = new PositionedSoundInstance(event, category, volume, pitch, soundRandom, coords.getX(), coords.getY(), coords.getZ());
        mc.getSoundManager().play(sound);
    }

    public static void sendMessage(String message) {
        if (message.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(message.substring(1));
        } else {
            mc.player.networkHandler.sendChatMessage(message);
        }
    }

    public static void info(String message) {
        mc.inGameHud.getChatHud().addMessage(Text.of("§c[NoFrills]§r " + message + "§r"), null, noFrillsIndicator);
    }

    public static void infoNoPrefix(String message) {
        mc.inGameHud.getChatHud().addMessage(Text.of(message), null, noFrillsIndicator);
    }

    public static void infoButton(String message, String command) {
        ClickEvent click = new ClickEvent.RunCommand(command);
        mc.inGameHud.getChatHud().addMessage(Text.literal("§c[NoFrills]§r " + message + "§r").setStyle(Style.EMPTY.withClickEvent(click)), null, noFrillsIndicator);
    }

    public static void infoLink(String message, String url) {
        ClickEvent click = new ClickEvent.OpenUrl(URI.create(url));
        mc.inGameHud.getChatHud().addMessage(Text.literal("§c[NoFrills]§r " + message + "§r").setStyle(Style.EMPTY.withClickEvent(click)), null, noFrillsIndicator);
    }

    public static void infoRaw(Text message) {
        mc.inGameHud.getChatHud().addMessage(Text.literal("§c[NoFrills]§r ").append(message).append("§r"), null, noFrillsIndicator);
    }

    public static void infoFormat(String message, Object... values) {
        mc.inGameHud.getChatHud().addMessage(Text.of("§c[NoFrills]§r " + format(message, values) + "§r"), null, noFrillsIndicator);
    }

    /**
     * Disables a specific slot in the provided screen, preventing it from being clicked and hiding its tooltip.
     *
     * @param slot The slot to disable
     */
    public static void setDisabled(Slot slot, boolean disabled) {
        SlotOptions.disableSlot(slot, disabled);
    }

    /**
     * Spoofs a slot to render a specific item stack, rather than the item that is actually in that slot.
     *
     * @param slot        The slot to spoof
     * @param replacement The ItemStack to spoof as
     */
    public static void setSpoofed(Slot slot, ItemStack replacement) {
        SlotOptions.spoofSlot(slot, replacement);
    }

    public static String getCoordsFormatted(String format) {
        Vec3d pos = mc.player.getPos();
        DecimalFormat decimalFormat = new DecimalFormat("#");
        return format
                .replace("{x}", decimalFormat.format(Math.ceil(pos.x)))
                .replace("{y}", decimalFormat.format(Math.floor(pos.y)))
                .replace("{z}", decimalFormat.format(Math.ceil(pos.z)));

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

    public static boolean isInKuudra() {
        return isInZone(Symbols.zone + " Kuudra's Hollow", false);
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
     * Returns true if the player is currently on a 1.21+ Skyblock island (currently only Park and Galatea)
     */
    public static boolean isOnModernIsland() {
        return isInArea("The Park") || isInArea("Galatea");
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
        } else {
            return entity instanceof LivingEntity;
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
                return data.copyNbt();
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

    public static boolean isTextureEqual(GameProfile profile, String textureId) {
        String url = getTextureUrl(profile);
        if (url != null) {
            return url.endsWith("texture/" + textureId);
        }
        return false;
    }

    public static boolean isFixEnabled(Config.fixMode mode) {
        return mode == Config.fixMode.AlwaysOn || SkyblockData.isInSkyblock() &&
                ((mode == Config.fixMode.SkyblockLegacyOnly && !Utils.isOnModernIsland()) || mode == Config.fixMode.SkyblockOnly);
    }

    /**
     * Returns every line of the stack's lore with no formatting, or else an empty list.
     */
    public static List<String> getLoreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                lines.add(Formatting.strip(line.getString()).trim());
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
            if (id.startsWith("ABIPHONE") || id.equals("ABINGOPHONE")) {
                return true;
            }
            for (String item : abilityWhitelist) {
                if (id.equals(item)) {
                    return true;
                }
            }
        }
        return !getRightClickAbility(stack).isEmpty();
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

    private static String[] getVersionNumber(String version) {
        if (version.startsWith("mod_version=")) {
            return version.replace("mod_version=", "").split("\\.");
        }
        return null;
    }

    public static void checkUpdate(boolean notifyIfMatch) {
        new Thread(() -> {
            String propertiesURL = "https://raw.githubusercontent.com/WhatYouThing/NoFrills/refs/heads/main/gradle.properties";
            try {
                ModMetadata metadata = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();
                String version = metadata.getVersion().getFriendlyString();
                if (version.equals("${version}")) {
                    version = "0.0.0";
                }
                String[] versionLocal = version.split("\\.");
                InputStream connection = URI.create(propertiesURL).toURL().openStream();
                for (Scanner iteratorNewest = new Scanner(connection); iteratorNewest.hasNext(); ) {
                    String[] versionNewest = getVersionNumber(iteratorNewest.next());
                    if (versionNewest != null) {
                        for (int i = 0; i <= versionLocal.length - 1; i++) {
                            if (Integer.parseInt(versionLocal[i]) < Integer.parseInt(versionNewest[i])) {
                                infoLink("§a§lNew version available! §aClick here to open the Modrinth releases page. §7Current: " + String.join(".", versionLocal) + ", Newest: " + String.join(".", versionNewest), "https://modrinth.com/mod/nofrills/versions");
                                return;
                            }
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
        Vec3d height = entity.getLerpedPos(tickDelta).add(0, 1.62, 0); // this is the standing eye height, hoping that is a bug
        Vec3d camPos = entity.getCameraPosVec(tickDelta);
        Vec3d rot = entity.getRotationVec(tickDelta);
        Vec3d pos = new Vec3d(camPos.getX(), height.getY(), camPos.getZ());
        Vec3d end = pos.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);
        RaycastContext context = new RaycastContext(pos, end, RaycastContext.ShapeType.OUTLINE, net.minecraft.world.RaycastContext.FluidHandling.ANY, entity);
        ((RaycastOptions) context).nofrills_mod$setConsiderAllFull(true);
        return entity.getWorld().raycast(context);
    }

    /**
     * Wrapper for the getOtherEntities function.
     */
    public static List<Entity> getNearbyEntities(Entity entity, double distX, double distY, double distZ, Predicate<? super Entity> predicate) {
        return entity.getWorld().getOtherEntities(
                entity,
                Box.of(entity.getPos(), distX, distY, distZ),
                predicate
        );
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
            if (ent.getType() != EntityType.ARMOR_STAND && ent.getPos().getY() < maxY && dist < lowestDist) {
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
        return decimalFormat.format(number);
    }

    public static String formatDecimal(float number) {
        return decimalFormat.format(number);
    }

    /**
     * Attempts to calculate the actual health value from the provided Entity's (max) health. Mostly applies to bosses or anything that has millions of HP, because their actual health value is reduced.
     */
    public static float getTrueHealth(float health) {
        return (health - 1024.0f) * 10000.0f;
    }

    public static boolean isLeapMenu(String title) {
        return Config.leapOverlay && Utils.isInDungeons() && title.equals(LeapOverlay.leapMenuName);
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
    }

    public static class Keybinds {
        public static final KeyBinding getPearls = new KeyBinding("key.nofrills.refillPearls", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.nofrills");
        public static final KeyBinding recipeLookup = new KeyBinding("key.nofrills.recipeLookup", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.nofrills");
        public static final KeyBinding bindSlots = new KeyBinding("key.nofrills.bindSlots", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.nofrills");
    }
}
