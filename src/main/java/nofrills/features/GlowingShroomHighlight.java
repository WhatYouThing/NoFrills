package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Config;
import nofrills.events.SpawnParticleEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static nofrills.Main.mc;

public class GlowingShroomHighlight {
    private static final List<BlockPos> shroomData = new ArrayList<>();
    private static final RenderColor shroomColor = RenderColor.fromHex(0x00ff00, 0.5f);
    private static boolean isInCave = false;

    @EventHandler
    public static void onParticle(SpawnParticleEvent event) {
        if (!Config.shroomHighlight) {
            return;
        }
        if (isInCave && event.packet.getParameters().getType() == ParticleTypes.ENTITY_EFFECT) {
            BlockPos pos = BlockPos.ofFloored(event.packet.getX(), event.packet.getY(), event.packet.getZ());
            Block block = mc.world.getBlockState(pos).getBlock();
            if (shroomData.contains(pos)) {
                return;
            }
            if (block == Blocks.RED_MUSHROOM || block == Blocks.BROWN_MUSHROOM) {
                shroomData.add(pos);
            }
        }
    }

    @EventHandler
    public static void onTick(WorldTickEvent event) {
        isInCave = Utils.skyblockData.currentLocation.equals(Utils.Symbols.zone + " Glowing Mushroom Cave");
        if (!isInCave && !shroomData.isEmpty()) {
            shroomData.clear();
        }
    }

    @EventHandler
    public static void onRender(WorldRenderEvent event) {
        if (!Config.shroomHighlight) {
            return;
        }
        Iterator<BlockPos> iterator = shroomData.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            Block block = mc.world.getBlockState(pos).getBlock();
            if (block == Blocks.RED_MUSHROOM || block == Blocks.BROWN_MUSHROOM) {
                Rendering.drawFilled(event.matrices, event.consumer, event.camera, Box.enclosing(pos, pos), false, shroomColor);
            } else {
                iterator.remove();
            }
        }
    }
}
