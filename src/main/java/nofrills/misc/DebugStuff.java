package nofrills.misc;

import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.Vec3;
import nofrills.events.*;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

@EventListener
public class DebugStuff {
    private static long tickCounter = 0;
    private static boolean logSounds = false;
    private static boolean logNametags = false;
    private static boolean logParticles = false;
    private static boolean logEntityUpdates = false;

    private static void print(MutableComponent text) {
        Utils.infoRaw(text.setStyle(text.getStyle().withHoverEvent(new HoverEvent.ShowText(Component.literal("at tick: " + tickCounter)))));
    }

    private static void print(String message, Object... values) {
        print(Component.literal(Utils.format(message, values)));
    }

    private static void logNbt(String message, Entity entity) {
        Thread.startVirtualThread(() -> {
            EntityDataAccessor accessor = new EntityDataAccessor(entity);
            LOGGER.info(Utils.format(message, NbtUtils.prettyPrint(accessor.getData(), true)));
        });
    }

    public static void dumpHeadTextures() {
        List<EquipmentSlot> searchedSlots = List.of(
                EquipmentSlot.HEAD,
                EquipmentSlot.MAINHAND,
                EquipmentSlot.OFFHAND
        );
        for (Entity ent : Utils.getEntities()) {
            if (ent instanceof LivingEntity living) {
                for (EquipmentSlot slot : searchedSlots) {
                    ItemStack stack = living.getItemBySlot(slot);
                    if (stack.getItem() instanceof PlayerHeadItem) {
                        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
                        if (profile == null) continue;
                        Optional<String> payload = Utils.getTexturePayload(profile.partialProfile());
                        Vec3 pos = living.position();
                        LOGGER.info(Utils.format("\n\tURL - {}\n\tPayload - {}\n\tPayload hashcode - {}\n\tSlot - {}\n\tEntity Name - {}\n\tHead Name - {}\n\tPosition - {} {} {}",
                                Utils.getTextureUrl(profile.partialProfile()),
                                payload.orElse("null"),
                                payload.orElse("").hashCode(),
                                Utils.toUpper(slot.name()),
                                living.getName().getString(),
                                stack.getHoverName().getString(),
                                pos.x,
                                pos.y,
                                pos.z
                        ));
                    }
                }
            }
        }
        Utils.info("Dumped head texture URLs to latest.log.");
    }

    public static void dumpPlayerTextures() {
        MinecraftSessionService service = mc.services().sessionService();
        for (Entity ent : Utils.getEntities()) {
            if (ent instanceof Player player) {
                MinecraftProfileTextures textures = service.getTextures(player.getGameProfile());
                Vec3 pos = player.position();
                if (textures.skin() == null) {
                    continue;
                }
                LOGGER.info(Utils.format("\n\tURL - {}\n\tEntity Name - {}\n\tPosition - {} {} {}",
                        textures.skin().getUrl(),
                        player.getName().getString(),
                        pos.x,
                        pos.y,
                        pos.z
                ));
            }
        }
        Utils.info("Dumped player texture URLs to latest.log.");
    }

    public static void dumpTabList() {
        for (String line : Utils.getTabListLines()) {
            Utils.info(line);
        }
    }

    public static void dumpNameTags() {
        for (Entity entity : Utils.getEntities()) {
            if (entity instanceof ArmorStand stand) {
                Component name = stand.getCustomName();
                if (name != null) {
                    Utils.infoRaw(name.copy());
                }
            }
        }
    }

    public static void dumpTabListFooter() {
        for (String line : Utils.getFooterLines()) {
            Utils.info(line);
        }
    }

    public static void dumpBossBarLabel() {
        List<LerpingBossEvent> bossBars = Utils.getBossBars();
        if (!bossBars.isEmpty()) {
            Utils.infoRaw(bossBars.getFirst().getName().copy());
        }
    }

    public static void toggleLogSounds() {
        logSounds = !logSounds;
        if (logSounds) {
            Utils.info("Sound logging enabled.");
        } else {
            Utils.info("Sound logging disabled.");
        }
    }

    public static void toggleLogNametags() {
        logNametags = !logNametags;
        if (logNametags) {
            Utils.info("Nametag logging enabled.");
        } else {
            Utils.info("Nametag logging disabled.");
        }
    }

    public static void toggleLogParticles() {
        logParticles = !logParticles;
        if (logParticles) {
            Utils.info("Particle logging enabled.");
        } else {
            Utils.info("Particle logging disabled.");
        }
    }

    public static void toggleLogEntityUpdates() {
        logEntityUpdates = !logEntityUpdates;
        if (logEntityUpdates) {
            Utils.info("Entity logging enabled.");
        } else {
            Utils.info("Entity logging disabled.");
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (logSounds) {
            print("sound: {}, category: {}, volume: {}, pitch: {}, x: {}, y: {}, z: {}",
                    event.packet.getSound().value().location(),
                    event.packet.getSource().getName(),
                    event.volume(),
                    event.pitch(),
                    event.pos.x,
                    event.pos.y,
                    event.pos.z
            );
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (logNametags) {
            print(Utils.toMutable(event.name));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private static void onParticle(SpawnParticleEvent event) {
        if (logParticles) {
            String msg = Utils.format("particle: {}, count: {}, speed: {}, offsetX: {}, offsetY: {}, offsetZ: {}, force: {}, important: {}, x: {}, y: {}, z: {}",
                    event.getParticleId(),
                    event.packet.getCount(),
                    event.packet.getMaxSpeed(),
                    event.packet.getXDist(),
                    event.packet.getYDist(),
                    event.packet.getZDist(),
                    event.packet.isOverrideLimiter(),
                    event.packet.alwaysShow(),
                    event.packet.getX(),
                    event.packet.getY(),
                    event.packet.getZ()
            );
            if (event.packet.getParticle() instanceof DustParticleOptions dustParticle) {
                Vector3f color = dustParticle.getColor();
                msg = Utils.format("{}, color: {} {} {}", msg, color.x, color.y, color.z);
            }
            print(msg);
        }
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (logEntityUpdates) {
            logNbt("entity update (tick " + tickCounter + "): {}", event.entity);
        }
    }

    @EventHandler
    private static void onRemoved(EntityRemovedEvent event) {
        if (logEntityUpdates) {
            logNbt("entity remove (tick " + tickCounter + "): {}", event.entity);
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        tickCounter++;
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        tickCounter = 0;
    }
}
