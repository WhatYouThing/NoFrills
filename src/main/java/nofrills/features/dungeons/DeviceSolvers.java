package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.*;
import nofrills.hud.HudManager;
import nofrills.misc.ConcurrentHashSet;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static nofrills.Main.mc;

@EventListener
public class DeviceSolvers {
    public static final Feature instance = new Feature("deviceSolvers");

    public static final SettingBool sharpshooter = new SettingBool(false, "sharpshooter", instance.key());
    public static final SettingBool sharpDoneAlert = new SettingBool(false, "sharpDoneAlert", instance.key());
    public static final SettingEnum<RenderStyle> sharpStyle = new SettingEnum<>(RenderStyle.Filled, RenderStyle.class, "sharpStyle", instance);
    public static final SettingColor sharpTargetColorFill = new SettingColor(RenderColor.green, "sharpTargetColor", instance.key());
    public static final SettingColor sharpTargetColorOutline = new SettingColor(RenderColor.green, "sharpTargetColorOutline", instance.key());
    public static final SettingColor sharpHitColorFill = new SettingColor(RenderColor.red, "sharpHitColor", instance.key());
    public static final SettingColor sharpHitColorOutline = new SettingColor(RenderColor.red, "sharpHitColorOutline", instance.key());
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
        if (instance.isActive() && sharpshooter.value() && Utils.isInDungeonBoss("7")) {
            if (Sharpshooter.target.contains(event.pos.getCenter()) && Sharpshooter.isActive()) {
                if (event.oldState.getBlock().equals(Blocks.EMERALD_BLOCK) && event.newState.getBlock().equals(Blocks.BLUE_TERRACOTTA)) {
                    Sharpshooter.list.add(event.pos);
                } else if (event.oldState.getBlock().equals(Blocks.BLUE_TERRACOTTA) && event.newState.getBlock().equals(Blocks.EMERALD_BLOCK)) {
                    Sharpshooter.next = event.pos;
                }
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

    @EventHandler
    private static void onEntityNamed(EntityNamedEvent event) {
        if (instance.isActive() && sharpshooter.value() && Utils.isInDungeonBoss("7")) {
            if (!Sharpshooter.done && event.namePlain.equals("Active") && Sharpshooter.area.getCenter().distanceTo(event.entity.position()) < 3.0) {
                if (sharpDoneAlert.value()) {
                    HudManager.setCustomTitle("§aSharpshooter Done", 40);
                    Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }
                Sharpshooter.done = true;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        Sharpshooter.done = false;
    }

    public static final class Sharpshooter {
        public static final ConcurrentHashSet<BlockPos> list = new ConcurrentHashSet<>();
        public static final AABB target = AABB.encapsulatingFullBlocks(new BlockPos(68, 130, 50), new BlockPos(64, 126, 50));
        public static final BlockPos area = new BlockPos(63, 126, 35);
        public static BlockPos next = null;
        public static boolean done = false;

        private static boolean isActive() {
            return mc.level != null && mc.level.hasSignal(area, Direction.DOWN);
        }

        public static void tick() {
            if (!isActive() && (!list.isEmpty() || next != null)) {
                next = null;
                list.clear();
            }
        }

        public static void render(WorldRenderEvent event) {
            if (!list.isEmpty()) {
                for (BlockPos pos : list) {
                    if (pos.equals(next)) continue;
                    AABB box = AABB.encapsulatingFullBlocks(pos, pos);
                    event.drawStyled(box, sharpStyle.value(), false, sharpHitColorOutline.value(), sharpHitColorFill.value());
                }
            }
            if (next != null) {
                AABB box = AABB.encapsulatingFullBlocks(next, next);
                event.drawStyled(box, sharpStyle.value(), false, sharpTargetColorOutline.value(), sharpTargetColorFill.value());
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
