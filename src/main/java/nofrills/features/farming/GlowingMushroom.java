package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.SpawnParticleEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class GlowingMushroom {
    public static final Feature instance = new Feature("shroomHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0x00ff00, 0.5f), "color", instance.key());

    private static final List<BlockPos> shroomData = new ArrayList<>();
    private static boolean isInCave = false;

    private static boolean isShroom(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.RED_MUSHROOM || block == Blocks.BROWN_MUSHROOM;
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && isInCave && event.type.equals(ParticleTypes.ENTITY_EFFECT)) {
            BlockPos pos = BlockPos.ofFloored(event.pos);
            if (!shroomData.contains(pos) && isShroom(pos)) {
                shroomData.add(pos);
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            isInCave = SkyblockData.getLocation().equals(Utils.Symbols.zone + " Glowing Mushroom Cave");
            if (!isInCave && !shroomData.isEmpty()) {
                shroomData.clear();
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && isInCave && !shroomData.isEmpty()) {
            List<BlockPos> shrooms = new ArrayList<>(shroomData);
            for (BlockPos pos : shrooms) {
                if (isShroom(pos)) {
                    event.drawFilled(Box.enclosing(pos, pos), false, color.value());
                } else {
                    shroomData.remove(pos);
                }
            }
        }
    }
}
