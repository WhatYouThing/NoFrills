package nofrills.misc;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static nofrills.Main.*;

public class Utils {
    public static final MessageIndicator noFrillsIndicator = new MessageIndicator(0xff5555, null, Text.of("Message from NoFrills mod."), "NoFrills Mod");
    public static final Pattern partyMessagePattern = Pattern.compile("Party > .*: .*");
    private static final Random soundRandom = Random.create(0);
    public static List<String> scoreboardLines = new ArrayList<>();

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

    /**
     * Enable/disable the glowing effect for a specific entity. Only needs to be called once.
     *
     * @param color color in hex format
     */
    public static void setGlowing(Entity ent, boolean glowing, int color) {
        ((EntityRendering) ent).nofrills_mod$setGlowingColored(glowing, color);
    }

    /**
     * Checks if an entity has had the glowing effect applied with the <code>setGlowing</code> function. Vanilla/Server glows will return false.
     */
    public static boolean isGlowing(Entity ent) {
        return ((EntityRendering) ent).nofrills_mod$getGlowing();
    }

    public static void setRenderOutline(Entity ent, boolean render, float red, float green, float blue, float alpha) {
        ((EntityRendering) ent).nofrills_mod$setRenderBoxOutline(render, red, green, blue, alpha);
    }

    public static boolean isRenderingOutline(Entity ent) {
        return ((EntityRendering) ent).nofrills_mod$getRenderingOutline();
    }

    public static void setRenderFilled(Entity ent, boolean render, float red, float green, float blue, float alpha) {
        ((EntityRendering) ent).nofrills_mod$setRenderBoxFilled(render, red, green, blue, alpha);
    }

    public static boolean isRenderingFilled(Entity ent) {
        return ((EntityRendering) ent).nofrills_mod$getRenderingFilled();
    }

    /**
     * Disables a specific slot in the provided screen, preventing it from being clicked and hiding its tooltip.
     *
     * @param screen The current screen
     * @param slotId The ID of the slot to disable
     */
    public static void setDisabled(Screen screen, int slotId, boolean disabled) {
        ((ScreenOptions) screen).nofrills_mod$disableSlot(slotId, disabled);
    }

    /**
     * Disables a specific slot in the provided screen, while also replacing it with a specific item.
     *
     * @param screen      The current screen
     * @param slotId      The ID of the slot to disable
     * @param replacement The item stack to put in place of the previous item
     */
    public static void setDisabled(Screen screen, int slotId, boolean disabled, ItemStack replacement) {
        ((ScreenOptions) screen).nofrills_mod$disableSlot(slotId, disabled, replacement);
    }

    public static void sendCoords(String format) {
        Vec3d pos = mc.player.getPos();
        DecimalFormat decimalFormat = new DecimalFormat("#");
        sendMessage(format
                .replace("{x}", decimalFormat.format(Math.ceil(pos.x)))
                .replace("{y}", decimalFormat.format(Math.floor(pos.y)))
                .replace("{z}", decimalFormat.format(Math.ceil(pos.z)))
        );
    }

    public static boolean isInZone(String zone, boolean containsCheck) {
        for (String line : scoreboardLines) {
            if (!containsCheck && line.startsWith(zone)) {
                return true;
            }
            if (containsCheck && line.contains(zone)) {
                return true;
            }
        }
        return false;
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

    public static boolean isOnGardenPlot() {
        return isInZone("Plot -", true);
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
                                infoLink("§a§lNew version available! §aClick here to open the GitHub releases page.\n§7Current Version: " + String.join(".", versionLocal) + "\n§7Newest Version: " + String.join(".", versionNewest), "https://github.com/WhatYouThing/NoFrills/releases");
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

    public static float horizontalDistance(Entity from, Entity to) {
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
        for (Entity ent : otherEntities) {
            float dist = horizontalDistance(ent, armorStand);
            if (ent.getType() != EntityType.ARMOR_STAND && dist < lowestDist) {
                entity = ent;
                lowestDist = dist;
            }
        }
        return entity;
    }

    public static class Symbols {
        public static String zone = "⏣";
        public static String zoneRift = "ф";
        public static String star = "✯";
        public static String heart = "❤";
        public static String skull = "☠";
        public static String format = "§";
    }

    public static class renderLayers {
        public static final RenderLayer.MultiPhase boxFilledNoCull = RenderLayer.of(
                "nofrills_filled_no_cull",
                VertexFormats.POSITION_COLOR,
                VertexFormat.DrawMode.TRIANGLE_STRIP,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.COLOR_PROGRAM)
                        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .depthTest(RenderPhase.ALWAYS_DEPTH_TEST) // doesnt work most of the time with sodium installed
                        .cull(RenderPhase.DISABLE_CULLING)
                        .build(false));
        public static final RenderLayer.MultiPhase boxFilled = RenderLayer.of(
                "nofrills_filled",
                VertexFormats.POSITION_COLOR,
                VertexFormat.DrawMode.TRIANGLE_STRIP,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.COLOR_PROGRAM)
                        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .build(false));
    }
}
