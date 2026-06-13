package nofrills.misc;

import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import nofrills.events.EntityNamedEvent;
import nofrills.events.PlaySoundEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;

import java.util.List;
import java.util.Optional;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

public class DebugStuff {
    private static long tickCounter = 0;
    private static boolean logSounds = false;
    private static boolean logNametags = false;

    private static void print(MutableText text) {
        Utils.infoRaw(text.setStyle(text.getStyle().withHoverEvent(new HoverEvent.ShowText(Text.literal("at tick: " + tickCounter)))));
    }

    private static void print(String message, Object... values) {
        print(Text.literal(Utils.format(message, values)));
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
                    ItemStack stack = living.getEquippedStack(slot);
                    if (stack.getItem() instanceof PlayerHeadItem) {
                        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
                        if (profile == null || profile.getGameProfile() == null) continue;
                        Optional<String> payload = Utils.getTexturePayload(profile.getGameProfile());
                        Vec3d pos = living.getEntityPos();
                        LOGGER.info(Utils.format("\n\tURL - {}\n\tPayload - {}\n\tPayload hashcode - {}\n\tSlot - {}\n\tEntity Name - {}\n\tHead Name - {}\n\tPosition - {} {} {}",
                                Utils.getTextureUrl(profile.getGameProfile()),
                                payload.orElse("null"),
                                payload.orElse("").hashCode(),
                                Utils.toUpper(slot.name()),
                                living.getName().getString(),
                                stack.getName().getString(),
                                pos.getX(),
                                pos.getY(),
                                pos.getZ()
                        ));
                    }
                }
            }
        }
        Utils.info("Dumped head texture URLs to latest.log.");
    }

    public static void dumpPlayerTextures() {
        MinecraftSessionService service = mc.getApiServices().sessionService();
        for (Entity ent : Utils.getEntities()) {
            if (ent instanceof PlayerEntity player) {
                if (player.getGameProfile() != null) {
                    MinecraftProfileTextures textures = service.getTextures(player.getGameProfile());
                    Vec3d pos = player.getEntityPos();
                    if (textures.skin() == null) {
                        continue;
                    }
                    LOGGER.info(Utils.format("\n\tURL - {}\n\tEntity Name - {}\n\tPosition - {} {} {}",
                            textures.skin().getUrl(),
                            player.getName().getString(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    ));
                }
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
            if (entity instanceof ArmorStandEntity stand) {
                Text name = stand.getCustomName();
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
        List<ClientBossBar> bossBars = Utils.getBossBars();
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

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (logSounds) {
            print("sound: {}, category: {}, volume: {}, pitch: {}, x: {}, y: {}, z: {}",
                    event.packet.getSound().value().id(),
                    event.packet.getCategory().getName(),
                    event.volume(),
                    event.pitch(),
                    event.pos.getX(),
                    event.pos.getY(),
                    event.pos.getZ()
            );
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (logNametags) {
            print(Utils.toMutable(event.name));
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
