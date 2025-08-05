package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.BlockUpdateEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class DeviceSolvers {
    public static final Feature instance = new Feature("deviceSolvers");

    public static final SettingBool sharpshooter = new SettingBool(false, "sharpshooter", instance.key());

    private static final List<BlockPos> sharpshooterList = new ArrayList<>();
    private static final Box sharpshooterTarget = Box.enclosing(new BlockPos(68, 130, 50), new BlockPos(64, 126, 50));
    private static final Box sharpshooterArea = new Box(63.2, 127, 35.8, 63.8, 128, 35.2);
    private static BlockPos sharpshooterNext = null;

    private static boolean isSharpshooterActive() {
        return mc.player != null && sharpshooterArea.intersects(mc.player.getBoundingBox());
    }

    private static BlockPos findSharpshooterTarget() {
        for (double x = sharpshooterTarget.minX; x <= sharpshooterTarget.maxX; x++) {
            for (double y = sharpshooterTarget.minY; y <= sharpshooterTarget.maxY; y++) {
                for (double z = sharpshooterTarget.minZ; z <= sharpshooterTarget.maxZ; z++) {
                    BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                    if (mc.world.getBlockState(pos).equals(Blocks.EMERALD_BLOCK.getDefaultState())) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && sharpshooter.value() && Utils.isOnDungeonFloor("7")) {
            if (isSharpshooterActive()) {
                sharpshooterNext = findSharpshooterTarget();
            } else if (!sharpshooterList.isEmpty() || sharpshooterNext != null) {
                sharpshooterNext = null;
                sharpshooterList.clear();
            }
        }
    }

    @EventHandler
    public static void onBlockUpdate(BlockUpdateEvent event) {
        if (instance.isActive() && sharpshooter.value() && Utils.isOnDungeonFloor("7")) {
            if (sharpshooterTarget.contains(event.pos.toCenterPos()) && isSharpshooterActive()) {
                BlockState terracotta = Blocks.BLUE_TERRACOTTA.getDefaultState();
                BlockState emerald = Blocks.EMERALD_BLOCK.getDefaultState();
                if (event.oldState.equals(emerald) && event.newState.equals(terracotta)) {
                    if (sharpshooterNext != null) {
                        sharpshooterList.add(sharpshooterNext);
                    }
                }
                if (event.oldState.equals(terracotta) && event.newState.equals(emerald)) {
                    sharpshooterNext = findSharpshooterTarget();
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && sharpshooter.value()) {
            if (!sharpshooterList.isEmpty()) {
                for (BlockPos pos : sharpshooterList) {
                    event.drawFilled(Box.enclosing(pos, pos), true, RenderColor.fromHex(0xff0000, 1.0f));
                }
            }
            if (sharpshooterNext != null) {
                event.drawFilled(Box.enclosing(sharpshooterNext, sharpshooterNext), true, RenderColor.fromHex(0x00ff00, 1.0f));
            }
        }
    }
}
