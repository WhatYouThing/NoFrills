package nofrills.features.dungeons;

import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.mob.ZombieEntity;
import nofrills.config.Feature;
import nofrills.config.SettingString;
import nofrills.events.EntityRemovedEvent;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class MimicMessage {
    public static final Feature instance = new Feature("mimicMessage");

    public static final SettingString msg = new SettingString("/pc Mimic Killed!", "msg", instance);

    private static final EntityCache cache = new EntityCache();
    private static boolean mimicKilled = false;

    private static void processDeath() {
        if (instance.isActive()) {
            Utils.sendMessage(msg.value());
        }
        if (ScoreCalculator.instance.isActive()) {
            ScoreCalculator.mimicKilled();
        }
        mimicKilled = true;
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (!mimicKilled && event.entity instanceof ZombieEntity zombie && zombie.isBaby() && Utils.isInDungeons()) {
            GameProfile textures = Utils.getTextures(Utils.getEntityArmor(zombie).getFirst());
            if (Utils.isTextureEqual(textures, "e19c12543bc7792605ef68e1f8749ae8f2a381d9085d4d4b780ba1282d3597a0")) {
                cache.add(zombie);
            }
            if (!zombie.isAlive() && cache.has(zombie)) {
                processDeath();
            }
        }
    }

    @EventHandler
    private static void onRemoved(EntityRemovedEvent event) {
        if (!mimicKilled && cache.has(event.entity) && event.entity.distanceTo(mc.player) <= 64.0) {
            processDeath();
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        mimicKilled = false;
    }
}