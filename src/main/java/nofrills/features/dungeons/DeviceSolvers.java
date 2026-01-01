package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.BlockUpdateEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nofrills.Main.mc;

public class DeviceSolvers {
    public static final Feature instance = new Feature("deviceSolvers");

    public static final SettingBool sharpshooter = new SettingBool(false, "sharpshooter", instance.key());
    public static final SettingBool arrowAlign = new SettingBool(false, "arrowAlign", instance.key());

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("7")) {
            if (arrowAlign.value()) {
                ArrowAlign.tick();
            }
            if (sharpshooter.value()) {
                Sharpshooter.tick();
            }
        }
    }

    @EventHandler
    public static void onBlockUpdate(BlockUpdateEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("7")) {
            if (sharpshooter.value()) {
                Sharpshooter.blockUpdate(event);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("7")) {
            if (arrowAlign.value()) {
                ArrowAlign.render(event);
            }
            if (sharpshooter.value()) {
                Sharpshooter.render(event);
            }
        }
    }

    public static final class Sharpshooter {
        public static final List<BlockPos> list = new ArrayList<>();
        public static final Box target = Box.enclosing(new BlockPos(68, 130, 50), new BlockPos(64, 126, 50));
        public static final Box area = new Box(63.2, 127, 35.8, 63.8, 128, 35.2);
        public static final BlockState terracotta = Blocks.BLUE_TERRACOTTA.getDefaultState();
        public static final BlockState emerald = Blocks.EMERALD_BLOCK.getDefaultState();
        public static BlockPos next = null;

        private static boolean isActive() {
            return mc.player != null && area.intersects(mc.player.getBoundingBox());
        }

        private static BlockPos findTarget() {
            for (double x = target.minX; x <= target.maxX; x++) {
                for (double y = target.minY; y <= target.maxY; y++) {
                    for (double z = target.minZ; z <= target.maxZ; z++) {
                        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                        if (mc.world.getBlockState(pos).equals(emerald)) {
                            return pos;
                        }
                    }
                }
            }
            return null;
        }

        public static void blockUpdate(BlockUpdateEvent event) {
            if (target.contains(event.pos.toCenterPos()) && isActive()) {
                if (event.oldState.equals(emerald) && event.newState.equals(terracotta) && next != null) {
                    list.add(next);
                }
                if (event.oldState.equals(terracotta) && event.newState.equals(emerald)) {
                    next = findTarget();
                }
            }
        }

        public static void tick() {
            if (!isActive()) {
                if (!list.isEmpty() || next != null) {
                    next = null;
                    list.clear();
                }
                return;
            }
            next = findTarget();
        }

        public static void render(WorldRenderEvent event) {
            if (!list.isEmpty()) {
                for (BlockPos pos : list) {
                    event.drawFilled(Box.enclosing(pos, pos), true, RenderColor.red);
                }
            }
            if (next != null) {
                event.drawFilled(Box.enclosing(next, next), true, RenderColor.green);
            }
        }
    }

    public static final class ArrowAlign {
        public static final Box area = new Box(-2, 125, 81, 4, 120, 74);
        public static final BlockPos corner = new BlockPos(-2, 124, 79);
        public static final int[][] solutions = new int[][]{
                new int[]{1, 1, 3, -1, -1, 7, -1, 3, -1, -1, -1, -1, 3, -1, -1, -1, -1, 3, -1, 7, -1, -1, 1, 1, 7},
                new int[]{1, 1, -1, 5, 5, 7, -1, -1, -1, 7, 7, 5, -1, 1, 7, -1, 7, -1, 7, -1, -1, -1, -1, -1, -1},
                new int[]{3, 5, 5, -1, -1, 3, -1, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, 3, -1, 7, -1, -1, 1, 1, 7},
                new int[]{-1, -1, -1, -1, -1, 3, -1, -1, -1, 3, 3, -1, -1, -1, 3, 3, -1, 7, -1, 3, 1, 1, 7, 5, 5},
                new int[]{1, 1, 1, 1, 3, 7, -1, -1, -1, 3, 7, -1, -1, -1, 3, 7, -1, 7, -1, 3, -1, -1, 7, 5, 5},
                new int[]{1, 1, 3, -1, -1, 7, -1, 3, -1, 7, 7, -1, 3, -1, 7, 7, -1, 3, -1, 7, -1, -1, 1, 1, 7},
                new int[]{-1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, -1},
                new int[]{-1, 1, 1, 3, -1, -1, 7, -1, 3, -1, -1, 7, -1, 3, -1, -1, 7, -1, 3, -1, -1, 7, -1, 1, -1},
                new int[]{-1, 1, 3, -1, -1, -1, -1, 1, 1, -1, -1, 1, 7, -1, -1, -1, -1, 1, 1, -1, -1, 1, 7, -1, -1}
        }; // solution set from Skyblocker, no idea how Odin formats its solutions which sure does prevent me from copying them
        public static final HashMap<ItemFrameEntity, Integer> solutionMap = new HashMap<>();

        private static boolean isActive() {
            return mc.player != null && area.getCenter().distanceTo(mc.player.getPos()) <= 8.0;
        }

        private static int getNeededClicks(int current, int target) {
            return (8 - current + target) % 8;
        }

        public static List<ItemFrameEntity> getFrames() {
            List<Entity> entities = Utils.getOtherEntities(null, ArrowAlign.area, entity -> entity instanceof ItemFrameEntity);
            List<ItemFrameEntity> frames = new ArrayList<>();
            for (Entity entity : entities) {
                ItemFrameEntity frame = (ItemFrameEntity) entity;
                if (frame.getHeldItemStack().getItem().equals(Items.ARROW)) {
                    frames.add(frame);
                }
            }
            return frames;
        }

        public static int toIndex(ItemFrameEntity entity) {
            BlockPos pos = entity.getBlockPos();
            return corner.getZ() - pos.getZ() + 5 * (corner.getY() - pos.getY());
        }

        public static boolean matchSolution(int[] array, List<ItemFrameEntity> frames) {
            int count = 0;
            for (ItemFrameEntity frame : frames) {
                if (array[toIndex(frame)] == -1) {
                    return false;
                }
            }
            for (int i : array) {
                if (i != -1) {
                    count++;
                }
            }
            return count == frames.size();
        }

        public static int[] findSolution(List<ItemFrameEntity> frames) {
            if (frames.isEmpty()) return new int[]{};
            for (int[] array : solutions) {
                if (!matchSolution(array, frames)) {
                    continue;
                }
                return array;
            }
            return new int[]{}; // should never occur unless they add new patterns to the device
        }

        public static void tick() {
            if (!isActive()) {
                if (!solutionMap.isEmpty()) {
                    solutionMap.clear();
                }
                return;
            }
            if (solutionMap.isEmpty()) {
                List<ItemFrameEntity> frames = getFrames();
                int[] solution = findSolution(frames);
                if (frames.isEmpty() || solution.length == 0) return;
                for (ItemFrameEntity frame : frames) {
                    solutionMap.put(frame, solution[ArrowAlign.toIndex(frame)]);
                }
            }
        }

        public static void render(WorldRenderEvent event) {
            for (Map.Entry<ItemFrameEntity, Integer> entry : solutionMap.entrySet()) {
                ItemFrameEntity frame = entry.getKey();
                Vec3d pos = frame.getEyePos().add(0.0, 0.2, 0.0);
                int clicks = getNeededClicks(frame.getRotation(), entry.getValue());
                if (clicks > 0) {
                    event.drawText(pos, Text.literal(String.valueOf(clicks)), 0.04f, true, RenderColor.white);
                }
            }
        }
    }
}
