package nofrills.features.dungeons;

import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.mob.ZombieEntity;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.Utils;

public class MimicMessage {
    public static final Feature instance = new Feature("mimicMessage");

    public static final SettingString msg = new SettingString("/pc Mimic killed, +2 score!", "msg", instance);

    private static String mimicUuid = "";

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && event.entity instanceof ZombieEntity zombie && zombie.isBaby()) {
            GameProfile textures = Utils.getTextures(Utils.getEntityArmor(zombie).getFirst());
            String uuid = zombie.getUuidAsString();
            if (mimicUuid.isEmpty() && Utils.isTextureEqual(textures, "e19c12543bc7792605ef68e1f8749ae8f2a381d9085d4d4b780ba1282d3597a0")) {
                mimicUuid = uuid;
            }
            if (zombie.getHealth() == 0.0f && uuid.equals(mimicUuid)) {
                Utils.sendMessage(msg.value());
                mimicUuid = "doid";
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        mimicUuid = "";
    }
}