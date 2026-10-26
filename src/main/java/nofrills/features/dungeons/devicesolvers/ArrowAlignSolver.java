package nofrills.features.dungeons.devicesolvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.SettingBool;
import nofrills.events.*;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static nofrills.Main.mc;

@EventListener
public class ArrowAlignSolver {
    public static final SettingBool blockWrong = new SettingBool(false, "alignBlockWrong", DeviceSolvers.instance);
    public static final SettingBool blockInvert = new SettingBool(false, "alignBlockInvert", DeviceSolvers.instance);

    private static final AABB area = new AABB(-2, 125, 81, 4, 120, 74);
    private static final BlockPos corner = new BlockPos(-2, 124, 79);
    private static final int[][] solutions = new int[][]{
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
    private static final ConcurrentHashMap<ItemFrame, Integer> solutionMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ItemFrame, Integer> clicksMap = new ConcurrentHashMap<>();

    private static boolean isDeviceActive() {
        return mc.player != null && area.getCenter().distanceTo(mc.player.position()) <= 8.0;
    }

    private static int getNeededClicks(int current, int target) {
        return (8 - current + target) % 8;
    }

    private static List<ItemFrame> getFrames() {
        List<Entity> entities = Utils.getOtherEntities(null, area, entity -> entity instanceof ItemFrame);
        List<ItemFrame> frames = new ArrayList<>();
        for (Entity entity : entities) {
            ItemFrame frame = (ItemFrame) entity;
            if (frame.getItem().getItem().equals(Items.ARROW)) {
                frames.add(frame);
            }
        }
        return frames;
    }

    private static int toIndex(ItemFrame entity) {
        BlockPos pos = entity.blockPosition();
        return corner.getZ() - pos.getZ() + 5 * (corner.getY() - pos.getY());
    }

    private static boolean matchSolution(int[] array, List<ItemFrame> frames) {
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

    private static int[] findSolution(List<ItemFrame> frames) {
        if (frames.isEmpty()) return new int[]{};
        for (int[] array : solutions) {
            if (!matchSolution(array, frames)) {
                continue;
            }
            return array;
        }
        return new int[]{}; // should never occur unless they add new patterns to the device
    }

    private static boolean shouldBlock() {
        if (blockWrong.value()) {
            if (blockInvert.value()) {
                return mc.options.keyShift.isDown();
            }
            return !mc.options.keyShift.isDown();
        }
        return false;
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (DeviceSolvers.instance.isActive() && DeviceSolvers.arrowAlign.value() && DungeonUtil.isInBossRoom("7")) {
            if (isDeviceActive() && solutionMap.isEmpty()) {
                List<ItemFrame> frames = getFrames();
                int[] solution = findSolution(frames);
                if (frames.isEmpty() || solution.length == 0) return;
                for (ItemFrame frame : frames) {
                    solutionMap.put(frame, solution[toIndex(frame)]);
                }
            } else if (!solutionMap.isEmpty()) {
                solutionMap.clear();
                clicksMap.clear();
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (DeviceSolvers.instance.isActive() && DeviceSolvers.arrowAlign.value() && isDeviceActive() && DungeonUtil.isInBossRoom("7")) {
            for (Map.Entry<ItemFrame, Integer> entry : solutionMap.entrySet()) {
                ItemFrame frame = entry.getKey();
                Vec3 pos = frame.getEyePosition().add(0.0, 0.2, 0.0);
                int rotation = clicksMap.containsKey(frame) ? clicksMap.get(frame) : frame.getRotation();
                int clicks = getNeededClicks(rotation, entry.getValue());
                if (clicks > 0) {
                    event.drawText(pos, Component.literal(String.valueOf(clicks)), 0.04f, true, RenderColor.WHITE);
                }
            }
        }
    }

    @EventHandler
    private static void onEntityInteract(InteractEntityEvent event) {
        if (DeviceSolvers.instance.isActive() && DeviceSolvers.arrowAlign.value() && isDeviceActive() && DungeonUtil.isInBossRoom("7")) {
            if (event.entity instanceof ItemFrame frame && solutionMap.containsKey(frame)) {
                int rotation = clicksMap.containsKey(frame) ? clicksMap.get(frame) : frame.getRotation();
                if (shouldBlock() && getNeededClicks(rotation, solutionMap.get(frame)) == 0) {
                    event.cancel();
                    return;
                }
                clicksMap.put(frame, (rotation + 1) % 8);
            }
        }
    }

    @EventHandler
    private static void onEntityUpdated(EntityUpdatedEvent event) {
        if (DeviceSolvers.instance.isActive() && DeviceSolvers.arrowAlign.value() && isDeviceActive() && DungeonUtil.isInBossRoom("7")) {
            if (event.entity instanceof ItemFrame frame && solutionMap.containsKey(frame)) {
                if (clicksMap.containsKey(frame) && frame.getRotation() == clicksMap.get(frame)) {
                    clicksMap.remove(frame);
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        solutionMap.clear();
        clicksMap.clear();
    }
}
