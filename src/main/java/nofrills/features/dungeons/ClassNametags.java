package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingDouble;
import nofrills.events.EventListener;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

@EventListener
public class ClassNametags {
    public static final Feature instance = new Feature("classNametags");

    public static final SettingDouble scale = new SettingDouble(0.5, "scale", instance.key());

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && mc.level != null && Utils.isInDungeons() && DungeonUtil.isDungeonStarted()) {
            for (AbstractClientPlayer player : DungeonUtil.getTeammateEntities()) {
                if (player.equals(mc.player)) {
                    continue;
                }
                String name = player.getName().getString();
                String dungeonClass = DungeonUtil.getPlayerClass(name);
                ClassColors.getForClass(dungeonClass).ifPresent(color -> {
                    Vec3 pos = player.getPosition(event.delta()).add(0.0, 3.25, 0.0);
                    MutableComponent text = Component.literal(Utils.format("§e[{}]§r {}", dungeonClass.substring(0, 1), name));
                    event.drawDistanceScaledText(pos, text, scale.valueFloat() * 0.1f, true, color);
                });
            }
        }
    }
}
