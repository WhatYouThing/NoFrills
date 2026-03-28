package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.ServerJoinEvent;
import nofrills.events.SpawnParticleEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.ConcurrentHashSet;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

public class EndNodeHighlight {
    public static final Feature instance = new Feature("endNodeHighlight");

    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Filled, RenderStyle.class, "style", instance);
    public static final SettingColor outlineColor = new SettingColor(new RenderColor(0, 255, 0, 255), "outlineColor", instance);
    public static final SettingColor fillColor = new SettingColor(new RenderColor(0, 255, 0, 255), "color", instance);

    private static final ConcurrentHashSet<BlockPos> nodes = new ConcurrentHashSet<>();
    private static final List<Vec3i> offsets = List.of(
            new Vec3i(1, 0, 0),
            new Vec3i(-1, 0, 0),
            new Vec3i(0, 1, 0),
            new Vec3i(0, -1, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(0, 0, -1)
    );

    private static boolean isNodeBlock(BlockState state) {
        return state.getBlock().equals(Blocks.PURPLE_TERRACOTTA);
    }

    private static boolean isNodeParticle(ParticleS2CPacket packet) {
        return packet.isImportant() && packet.shouldForceSpawn()
                && packet.getCount() == 2 && packet.getSpeed() == 0.0f
                && (packet.getOffsetX() == 0.25f || packet.getOffsetY() == 0.25f || packet.getOffsetZ() == 0.25f);
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && event.type.equals(ParticleTypes.WITCH) && isNodeParticle(event.packet) && Utils.isInArea("The End")) {
            BlockPos blockPos = BlockPos.ofFloored(event.pos);
            for (Vec3i offset : offsets) {
                BlockPos pos = blockPos.add(offset);
                if (!nodes.contains(pos) && isNodeBlock(mc.world.getBlockState(pos))) {
                    nodes.add(pos);
                    break;
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !nodes.isEmpty() && Utils.isInArea("The End")) {
            for (BlockPos node : nodes) {
                if (!isNodeBlock(mc.world.getBlockState(node))) {
                    nodes.remove(node);
                    continue;
                }
                event.drawStyled(Box.enclosing(node, node), style.value(), false, outlineColor.value(), fillColor.value());
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        nodes.clear();
    }
}
