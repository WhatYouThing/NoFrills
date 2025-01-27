package nofrills.misc;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static nofrills.Main.*;

public class Utils {
    public static final MessageIndicator noFrillsIndicator = new MessageIndicator(0xff5555, null, Text.of("Message from NoFrills mod."), "NoFrills Mod");
    public static final Pattern partyMessagePattern = Pattern.compile("Party > .*: .*");
    private static final Random soundRandom = Random.create(0);

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
        ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        mc.inGameHud.getChatHud().addMessage(Text.literal("§c[NoFrills]§r " + message + "§r").setStyle(Style.EMPTY.withClickEvent(click)), null, noFrillsIndicator);
    }

    public static void infoLink(String message, String url) {
        ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        mc.inGameHud.getChatHud().addMessage(Text.literal("§c[NoFrills]§r " + message + "§r").setStyle(Style.EMPTY.withClickEvent(click)), null, noFrillsIndicator);
    }

    public static void infoRaw(Text message) {
        mc.inGameHud.getChatHud().addMessage(Text.literal("§c[NoFrills]§r ").append(message).append("§r"), null, noFrillsIndicator);
    }

    /**
     * Disables a specific slot in the provided screen, preventing it from being clicked and hiding its tooltip.
     *
     * @param screen The current screen
     * @param slot   The slot to disable
     */
    public static void setDisabled(Screen screen, Slot slot, boolean disabled) {
        ((ScreenOptions) screen).nofrills_mod$disableSlot(slot, disabled);
    }

    public static ItemStack setStackName(ItemStack stack, String name) {
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(name));
        return stack;
    }

    /**
     * Spoofs a slot to render a specific item stack, rather than the item that is actually in that slot.
     *
     * @param screen      The current screen
     * @param slot        The slot to spoof
     * @param replacement The ItemStack to spoof as
     */
    public static void setSpoofed(Screen screen, Slot slot, ItemStack replacement) {
        ((ScreenOptions) screen).nofrills_mod$spoofSlot(slot, replacement);
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
                Text displayName = listEntry.getDisplayName();
                if (displayName != null) {
                    String name = Formatting.strip(displayName.getString());
                    return !name.contains(" ");
                }
            }
        }
        return entity == mc.player;
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
        Vec3d vec3d = entity.getCameraPosVec(tickDelta);
        Vec3d vec3d2 = entity.getRotationVec(tickDelta);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
        RaycastContext context = new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, net.minecraft.world.RaycastContext.FluidHandling.ANY, entity);
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

    public static class Symbols {
        public static String zone = "⏣";
        public static String zoneRift = "ф";
        public static String star = "✯";
        public static String heart = "❤";
        public static String skull = "☠";
        public static String format = "§";
        public static String vampLow = "҉";
    }

    public static class Keybinds {
        public static final KeyBinding getPearls = new KeyBinding("Refill Pearls", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "NoFrills");
        public static final KeyBinding recipeLookup = new KeyBinding("Recipe Lookup", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "NoFrills");
    }

    public static class SpoofedSlot {
        public int slotId;
        public ItemStack replacementStack;

        public SpoofedSlot(Slot slot, ItemStack replacementStack) {
            this.slotId = slot.id;
            this.replacementStack = replacementStack;
        }

        public boolean isSlot(Slot slot) {
            return slot != null && slot.id == slotId;
        }
    }

    public static class DisabledSlot {
        public int slotId;

        public DisabledSlot(Slot slot) {
            this.slotId = slot.id;
        }

        public boolean isSlot(Slot slot) {
            return slot != null && slot.id == slotId;
        }
    }
}
