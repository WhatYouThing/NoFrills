package nofrills.misc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;

public class DebugStuff {

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
                    GameProfile textures = Utils.getTextures(stack);
                    if (textures != null && stack.getItem() instanceof PlayerHeadItem) {
                        Vec3d pos = living.getEntityPos();
                        LOGGER.info(Utils.format("\n\tURL - {}\n\tSlot - {}\n\tEntity Name - {}\n\tHead Name - {}\n\tPosition - {} {} {}",
                                Utils.getTextureUrl(textures),
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
}
