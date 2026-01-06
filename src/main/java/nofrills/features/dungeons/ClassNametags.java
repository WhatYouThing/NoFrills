package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingDouble;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;

import static nofrills.Main.mc;

public class ClassNametags {
    public static final Feature instance = new Feature("classNametags");

    public static final SettingDouble scale = new SettingDouble(0.5, "scale", instance.key());
    public static final SettingColor healer = new SettingColor(RenderColor.fromHex(0xecb50c), "healerColor", instance.key());
    public static final SettingColor mage = new SettingColor(RenderColor.fromHex(0x1793c4), "mageColor", instance.key());
    public static final SettingColor bers = new SettingColor(RenderColor.fromHex(0xe7413c), "bersColor", instance.key());
    public static final SettingColor arch = new SettingColor(RenderColor.fromHex(0x4a14b7), "archColor", instance.key());
    public static final SettingColor tank = new SettingColor(RenderColor.fromHex(0x768f46), "tankColor", instance.key());

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && mc.world != null && Utils.isInDungeons() && DungeonUtil.isDungeonStarted()) {
            for (AbstractClientPlayerEntity player : new ArrayList<>(mc.world.getPlayers())) {
                if (player.equals(mc.player)) {
                    //continue;
                }
                String name = player.getName().getString();
                String dungeonClass = DungeonUtil.getPlayerClass(name);
                RenderColor color = switch (dungeonClass) {
                    case "Healer" -> healer.value();
                    case "Mage" -> mage.value();
                    case "Berserk" -> bers.value();
                    case "Archer" -> arch.value();
                    case "Tank" -> tank.value();
                    default -> null;
                };
                if (!dungeonClass.isEmpty() && color != null) {
                    Vec3d pos = player.getLerpedPos(event.tickCounter.getTickProgress(true)).add(0.0, 3.25, 0.0);
                    MutableText text = Text.literal(Utils.format("§e[{}]§r {}", dungeonClass.substring(0, 1), name));
                    event.drawText(pos, text, scale.valueFloat() * 0.1f, true, color);
                }
            }
        }
    }
}
