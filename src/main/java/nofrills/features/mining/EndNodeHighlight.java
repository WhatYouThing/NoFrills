package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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

    private static boolean isNodeParticle(ClientboundLevelParticlesPacket packet) {
        return packet.alwaysShow() && packet.isOverrideLimiter()
                && packet.getCount() == 2 && packet.getMaxSpeed() == 0.0f
                && (packet.getXDist() == 0.25f || packet.getYDist() == 0.25f || packet.getZDist() == 0.25f);
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && event.type.equals(ParticleTypes.WITCH) && isNodeParticle(event.packet) && Utils.isInArea("The End")) {
            BlockPos blockPos = BlockPos.containing(event.pos);
            for (Vec3i offset : offsets) {
                BlockPos pos = blockPos.offset(offset);
                if (!nodes.contains(pos) && isNodeBlock(mc.level.getBlockState(pos))) {
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
                if (!isNodeBlock(mc.level.getBlockState(node))) {
                    nodes.remove(node);
                    continue;
                }
                event.drawStyled(AABB.encapsulatingFullBlocks(node, node), style.value(), false, outlineColor.value(), fillColor.value());
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        nodes.clear();
    }
}
