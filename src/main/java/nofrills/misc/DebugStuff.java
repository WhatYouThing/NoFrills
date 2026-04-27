package nofrills.misc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.phys.Vec3;
import nofrills.events.PlaySoundEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;

import java.util.List;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

public class DebugStuff {
    private static int tickCounter = 0;
    private static boolean logSounds = false;

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
                    GameProfile textures = Utils.getTextures(stack);
                    if (textures != null && stack.getItem() instanceof PlayerHeadItem) {
                        Vec3 pos = living.position();
                        LOGGER.info(Utils.format("\n\tURL - {}\n\tSlot - {}\n\tEntity Name - {}\n\tHead Name - {}\n\tPosition - {} {} {}",
                                Utils.getTextureUrl(textures),
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

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (logSounds) {
            Utils.infoFormat("Sound event: x: {}, y: {}, z: {}, volume: {}, pitch: {}, category: {}, identifier: {}, at tick: {}",
                    event.pos.x,
                    event.pos.y,
                    event.pos.z,
                    event.volume(),
                    event.pitch(),
                    event.packet.getSource().getName(),
                    event.packet.getSound().getRegisteredName(),
                    tickCounter
            );
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
