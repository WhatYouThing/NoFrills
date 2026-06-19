package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static nofrills.Main.mc;

public class DeviceSolvers {
    public static final Feature instance = new Feature("deviceSolvers");

    public static final SettingBool sharpshooter = new SettingBool(false, "sharpshooter", instance.key());
    public static final SettingColor sharpTargetColor = new SettingColor(RenderColor.green, "sharpTargetColor", instance.key());
    public static final SettingColor sharpHitColor = new SettingColor(RenderColor.red, "sharpHitColor", instance.key());
    public static final SettingBool arrowAlign = new SettingBool(false, "arrowAlign", instance.key());
    public static final SettingBool alignBlockWrong = new SettingBool(false, "alignBlockWrong", instance.key());
    public static final SettingBool alignBlockInvert = new SettingBool(false, "alignBlockInvert", instance);

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

    @EventHandler
    private static void onEntityInteract(InteractEntityEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("7")) {
            if (arrowAlign.value()) {
                ArrowAlign.interactEntity(event);
            }
        }
    }

    @EventHandler
    private static void onEntityUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("7")) {
            if (arrowAlign.value()) {
                ArrowAlign.updateEntity(event);
            }
        }
    }

    public static final class Sharpshooter {
        public static final List<BlockPos> list = new ArrayList<>();
        public static final AABB target = AABB.encapsulatingFullBlocks(new BlockPos(68, 130, 50), new BlockPos(64, 126, 50));
        public static final AABB area = new AABB(63.2, 127, 35.8, 63.8, 128, 35.2);
        public static final BlockState terracotta = Blocks.BLUE_TERRACOTTA.defaultBlockState();
        public static final BlockState emerald = Blocks.EMERALD_BLOCK.defaultBlockState();
        public static BlockPos next = null;

        private static boolean isActive() {
            return mc.player != null && area.intersects(mc.player.getBoundingBox());
        }

        private static BlockPos findTarget() {
            for (double x = target.minX; x <= target.maxX; x++) {
                for (double y = target.minY; y <= target.maxY; y++) {
                    for (double z = target.minZ; z <= target.maxZ; z++) {
                        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                        if (mc.level.getBlockState(pos).equals(emerald)) {
                            return pos;
                        }
                    }
                }
            }
            return null;
        }

        public static void blockUpdate(BlockUpdateEvent event) {
            if (target.contains(event.pos.getCenter()) && isActive()) {
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
                    event.drawFilled(AABB.encapsulatingFullBlocks(pos, pos), true, sharpHitColor.value());
                }
            }
            if (next != null) {
                event.drawFilled(AABB.encapsulatingFullBlocks(next, next), true, sharpTargetColor.value());
            }
        }
    }

    public static final class ArrowAlign {
        public static final AABB area = new AABB(-2, 125, 81, 4, 120, 74);
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
        public static final ConcurrentHashMap<ItemFrame, Integer> solutionMap = new ConcurrentHashMap<>();
        public static final ConcurrentHashMap<ItemFrame, Integer> clicksMap = new ConcurrentHashMap<>();

        public static boolean isActive() {
            return mc.player != null && area.getCenter().distanceTo(mc.player.position()) <= 8.0;
        }

        private static int getNeededClicks(int current, int target) {
            return (8 - current + target) % 8;
        }

        public static List<ItemFrame> getFrames() {
            List<Entity> entities = Utils.getOtherEntities(null, ArrowAlign.area, entity -> entity instanceof ItemFrame);
            List<ItemFrame> frames = new ArrayList<>();
            for (Entity entity : entities) {
                ItemFrame frame = (ItemFrame) entity;
                if (frame.getItem().getItem().equals(Items.ARROW)) {
                    frames.add(frame);
                }
            }
            return frames;
        }

        public static int toIndex(ItemFrame entity) {
            BlockPos pos = entity.blockPosition();
            return corner.getZ() - pos.getZ() + 5 * (corner.getY() - pos.getY());
        }

        public static boolean matchSolution(int[] array, List<ItemFrame> frames) {
            int count = 0;
            for (ItemFrame frame : frames) {
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

        public static int[] findSolution(List<ItemFrame> frames) {
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
                    clicksMap.clear();
                }
                return;
            }
            if (solutionMap.isEmpty()) {
                List<ItemFrame> frames = getFrames();
                int[] solution = findSolution(frames);
                if (frames.isEmpty() || solution.length == 0) return;
                for (ItemFrame frame : frames) {
                    solutionMap.put(frame, solution[ArrowAlign.toIndex(frame)]);
                }
            }
        }

        public static void render(WorldRenderEvent event) {
            for (Map.Entry<ItemFrame, Integer> entry : solutionMap.entrySet()) {
                ItemFrame frame = entry.getKey();
                Vec3 pos = frame.getEyePosition().add(0.0, 0.2, 0.0);
                int rotation = clicksMap.containsKey(frame) ? clicksMap.get(frame) : frame.getRotation();
                int clicks = getNeededClicks(rotation, entry.getValue());
                if (clicks > 0) {
                    event.drawText(pos, Component.literal(String.valueOf(clicks)), 0.04f, true, RenderColor.white);
                }
            }
        }

        public static boolean shouldBlock() {
            if (alignBlockWrong.value()) {
                if (alignBlockInvert.value()) {
                    return mc.options.keyShift.isDown();
                }
                return !mc.options.keyShift.isDown();
            }
            return false;
        }

        public static void interactEntity(InteractEntityEvent event) {
            if (event.entity instanceof ItemFrame frame && solutionMap.containsKey(frame)) {
                int rotation = clicksMap.containsKey(frame) ? clicksMap.get(frame) : frame.getRotation();
                if (shouldBlock() && getNeededClicks(rotation, solutionMap.get(frame)) == 0) {
                    event.cancel();
                    return;
                }
                clicksMap.put(frame, (rotation + 1) % 8);
            }
        }

        public static void updateEntity(EntityUpdatedEvent event) {
            if (event.entity instanceof ItemFrame frame && solutionMap.containsKey(frame)) {
                if (clicksMap.containsKey(frame) && frame.getRotation() == clicksMap.get(frame)) {
                    clicksMap.remove(frame);
                }
            }
        }
    }
}
