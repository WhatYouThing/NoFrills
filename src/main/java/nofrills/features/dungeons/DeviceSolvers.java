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

    public static final SettingBool sharpshooter = new SettingBool(false, "sharpshooter", instance);
    public static final SettingBool arrowAlign = new SettingBool(false, "arrowAlign", instance);

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("7")) {
            if (arrowAlign.value()) {
                if (ArrowAlign.isActive()) {
                    if (ArrowAlign.solutionMap.isEmpty()) {
                        List<ItemFrame> frames = ArrowAlign.getFrames();
                        int[] solution = ArrowAlign.findSolution(frames);
                        if (frames.isEmpty() || solution.length == 0) return;
                        for (ItemFrame frame : frames) {
                            ArrowAlign.solutionMap.put(frame, solution[ArrowAlign.toIndex(frame)]);
                        }
                    }
                } else {
                    if (!ArrowAlign.solutionMap.isEmpty()) {
                        ArrowAlign.solutionMap.clear();
                        ArrowAlign.clicksMap.clear();
                    }
                }
            }
            if (sharpshooter.value() && !Sharpshooter.isActive() && (!Sharpshooter.list.isEmpty() || Sharpshooter.next != null)) {
                Sharpshooter.next = null;
                Sharpshooter.list.clear();
            }
        }
    }

    @EventHandler
    public static void onBlockUpdate(BlockUpdateEvent event) {
        if (instance.isActive() && Sharpshooter.isTargetBlock(event.pos) && Utils.isInDungeonBoss("7") && Sharpshooter.isActive()) {
            if (event.oldBlock.equals(Blocks.EMERALD_BLOCK) && event.newBlock.equals(Blocks.BLUE_TERRACOTTA)) {
                Sharpshooter.list.add(event.pos.immutable());
                if (Sharpshooter.next != null && Sharpshooter.next.equals(event.pos)) {
                    Sharpshooter.next = null;
                }
            } else if (event.oldBlock.equals(Blocks.BLUE_TERRACOTTA) && event.newBlock.equals(Blocks.EMERALD_BLOCK)) {
                Sharpshooter.list.remove(event.pos);
                Sharpshooter.next = event.pos.immutable();
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("7")) {
            if (arrowAlign.value() && ArrowAlign.isActive()) {
                for (Map.Entry<ItemFrame, Integer> entry : ArrowAlign.solutionMap.entrySet()) {
                    ItemFrame frame = entry.getKey();
                    Vec3 pos = frame.getEyePosition().add(0.0, 0.2, 0.0);
                    int rotation = ArrowAlign.clicksMap.containsKey(frame) ? ArrowAlign.clicksMap.get(frame) : frame.getRotation();
                    int clicks = ArrowAlign.getNeededClicks(rotation, entry.getValue());
                    if (clicks > 0) {
                        event.drawText(pos, Component.literal(String.valueOf(clicks)), 0.04f, true, RenderColor.white);
                    }
                }
            }
            if (sharpshooter.value()) {
                if (!Sharpshooter.list.isEmpty()) {
                    for (BlockPos pos : Sharpshooter.list) {
                        if (pos.equals(Sharpshooter.next)) continue;
                        AABB box = AABB.encapsulatingFullBlocks(pos, pos);
                        event.drawStyled(box, Sharpshooter.style.value(), false, Sharpshooter.hitColorOutline.value(), Sharpshooter.hitColorFill.value());
                    }
                }
                if (Sharpshooter.next != null) {
                    AABB box = AABB.encapsulatingFullBlocks(Sharpshooter.next, Sharpshooter.next);
                    event.drawStyled(box, Sharpshooter.style.value(), false, Sharpshooter.targetColorOutline.value(), Sharpshooter.targetColorFill.value());
                }
            }
        }
    }

    @EventHandler
    private static void onEntityInteract(InteractEntityEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("7")) {
            if (arrowAlign.value()) {
                if (event.entity instanceof ItemFrame frame && ArrowAlign.solutionMap.containsKey(frame)) {
                    int rotation = ArrowAlign.clicksMap.containsKey(frame) ? ArrowAlign.clicksMap.get(frame) : frame.getRotation();
                    if (ArrowAlign.shouldBlock() && ArrowAlign.getNeededClicks(rotation, ArrowAlign.solutionMap.get(frame)) == 0) {
                        event.cancel();
                        return;
                    }
                    ArrowAlign.clicksMap.put(frame, (rotation + 1) % 8);
                }
            }
        }
    }

    @EventHandler
    private static void onEntityUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("7")) {
            if (arrowAlign.value()) {
                if (event.entity instanceof ItemFrame frame && ArrowAlign.solutionMap.containsKey(frame)) {
                    if (ArrowAlign.clicksMap.containsKey(frame) && frame.getRotation() == ArrowAlign.clicksMap.get(frame)) {
                        ArrowAlign.clicksMap.remove(frame);
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onEntityNamed(EntityNamedEvent event) {
        if (instance.isActive() && sharpshooter.value() && Utils.isInDungeonBoss("7")) {
            if (!Sharpshooter.done && event.namePlain.equals("Active") && Sharpshooter.area.getCenter().distanceTo(event.entity.position()) < 3.0) {
                if (Sharpshooter.doneAlert.value()) {
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
        Sharpshooter.next = null;
        Sharpshooter.list.clear();
        ArrowAlign.solutionMap.clear();
        ArrowAlign.clicksMap.clear();
    }

    public static final class Sharpshooter {
        public static final SettingBool doneAlert = new SettingBool(false, "sharpDoneAlert", instance);
        public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Filled, RenderStyle.class, "sharpStyle", instance);
        public static final SettingColor targetColorFill = new SettingColor(RenderColor.green, "sharpTargetColor", instance);
        public static final SettingColor targetColorOutline = new SettingColor(RenderColor.green, "sharpTargetColorOutline", instance);
        public static final SettingColor hitColorFill = new SettingColor(RenderColor.red, "sharpHitColor", instance);
        public static final SettingColor hitColorOutline = new SettingColor(RenderColor.red, "sharpHitColorOutline", instance);

        public static final ConcurrentHashSet<BlockPos> list = new ConcurrentHashSet<>();
        public static final AABB target = AABB.encapsulatingFullBlocks(new BlockPos(68, 130, 50), new BlockPos(64, 126, 50));
        public static final BlockPos area = new BlockPos(63, 126, 35);
        public static BlockPos next = null;
        public static boolean done = false;

        public static boolean isActive() {
            return sharpshooter.value() && mc.level != null && mc.level.hasSignal(area, Direction.DOWN);
        }

        public static boolean isTargetBlock(BlockPos pos) {
            return target.contains(pos.getCenter()) && pos.getX() % 2 == 0 && pos.getY() % 2 == 0;
        }
    }

    public static final class ArrowAlign {
        public static final SettingBool blockWrong = new SettingBool(false, "alignBlockWrong", instance);
        public static final SettingBool blockInvert = new SettingBool(false, "alignBlockInvert", instance);

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

        public static boolean shouldBlock() {
            if (blockWrong.value()) {
                if (blockInvert.value()) {
                    return mc.options.keyShift.isDown();
                }
                return !mc.options.keyShift.isDown();
            }
            return false;
        }
    }
}
