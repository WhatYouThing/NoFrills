package nofrills.features.dungeons.devicesolvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.*;
import nofrills.events.*;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import nofrills.misc.Utils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static nofrills.Main.mc;

@EventListener
public class SimonSaysSolver {
    public static final SettingBool blockWrong = new SettingBool(false, "simonBlockWrong", DeviceSolvers.instance);
    public static final SettingBool blockInvert = new SettingBool(false, "simonBlockInvert", DeviceSolvers.instance);
    public static final SettingInt throttleTicks = new SettingInt(1, "simonThrottleTicks", DeviceSolvers.instance);
    public static final SettingBool progressSend = new SettingBool(false, "simonProgressSend", DeviceSolvers.instance);
    public static final SettingString progressMsg = new SettingString("/pc Simon Says {percent}", "simonProgressMsg", DeviceSolvers.instance);
    public static final SettingString resetMsg = new SettingString("/pc Simon Says Reset", "simonResetMsg", DeviceSolvers.instance);
    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Both, RenderStyle.class, "simonStyle", DeviceSolvers.instance);
    public static final SettingColor firstColorFill = new SettingColor(RenderColor.fromFormat(ChatFormatting.GREEN).withAlpha(0.5f), "simonFirstFill", DeviceSolvers.instance);
    public static final SettingColor firstColorOutline = new SettingColor(RenderColor.fromFormat(ChatFormatting.GREEN), "simonFirstOutline", DeviceSolvers.instance);
    public static final SettingColor secondColorFill = new SettingColor(RenderColor.fromFormat(ChatFormatting.GOLD).withAlpha(0.5f), "simonSecondFill", DeviceSolvers.instance);
    public static final SettingColor secondColorOutline = new SettingColor(RenderColor.fromFormat(ChatFormatting.GOLD), "simonSecondOutline", DeviceSolvers.instance);
    public static final SettingColor thirdColorFill = new SettingColor(RenderColor.fromFormat(ChatFormatting.RED).withAlpha(0.5f), "simonThirdFill", DeviceSolvers.instance);
    public static final SettingColor thirdColorOutline = new SettingColor(RenderColor.fromFormat(ChatFormatting.RED), "simonThirdOutline", DeviceSolvers.instance);

    private static final CopyOnWriteArrayList<BlockPos> list = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<BlockPos> board = new CopyOnWriteArrayList<>();
    private static final AABB lightsArea = AABB.encapsulatingFullBlocks(new BlockPos(111, 123, 92), new BlockPos(111, 120, 95));
    private static final AABB buttonsArea = AABB.encapsulatingFullBlocks(new BlockPos(110, 123, 92), new BlockPos(110, 120, 95));
    private static final AtomicInteger failedAt = new AtomicInteger();
    private static final AtomicInteger validClicks = new AtomicInteger();
    private static final AtomicInteger tickCounter = new AtomicInteger();
    private static final AtomicInteger lastClick = new AtomicInteger();

    private static boolean isSolverActive() {
        return DeviceSolvers.instance.isActive() && DeviceSolvers.simonSays.value();
    }

    private static boolean isDeviceActive() {
        return mc.player != null && buttonsArea.getCenter().distanceTo(mc.player.position()) <= 8.0;
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
    private static void onBlockUpdate(BlockUpdateEvent event) {
        if (isSolverActive() && mc.level != null && DungeonUtil.isInBossRoom("7") && isDeviceActive()) {
            Vec3 center = Vec3.atCenterOf(event.pos);
            if (event.oldBlock.equals(Blocks.OBSIDIAN) && event.newBlock.equals(Blocks.SEA_LANTERN) && lightsArea.contains(center)) {
                if (list.contains(event.pos)) return;
                list.add(event.pos.immutable());
            }
            if (event.oldState.isAir() && event.newState.isAir() && buttonsArea.contains(center)) {
                if (mc.level.getBlockState(event.pos.offset(1, 0, 0)).getBlock().equals(Blocks.SEA_LANTERN) && !list.isEmpty()) {
                    list.removeFirst(); // air -> air change happens due to successful skip
                }
            }
            if (event.oldState.isAir() && event.newBlock.equals(Blocks.STONE_BUTTON) && buttonsArea.contains(center)) {
                board.add(event.pos.immutable());
            }
            if (event.oldBlock.equals(Blocks.STONE_BUTTON) && event.newState.isAir() && buttonsArea.contains(center)) {
                board.remove(event.pos);
                if (board.isEmpty()) {
                    if (failedAt.get() == tickCounter.get()) {
                        if (progressSend.value()) {
                            Utils.sendMessage(resetMsg.value());
                        }
                        validClicks.set(0);
                    } else if (validClicks.get() > 0) {
                        if (progressSend.value() && validClicks.get() < 4) {
                            Utils.sendMessage(progressMsg.value().replaceAll("\\{percent}", (int) (validClicks.get() / 4.0 * 100) + "%"));
                        }
                        validClicks.set(0);
                    }
                }
                if (mc.level.getBlockState(event.pos.offset(1, 0, 0)).getBlock().equals(Blocks.OBSIDIAN)) {
                    list.remove(event.pos.offset(1, 0, 0));
                }
            }
        }
    }

    @EventHandler
    private static void onBlockInteract(InteractBlockEvent event) {
        if (isSolverActive() && !list.isEmpty() && DungeonUtil.isInBossRoom("7") && isDeviceActive()) {
            BlockPos pos = event.blockHitResult.getBlockPos();
            if (buttonsArea.contains(Vec3.atCenterOf(pos))) {
                if (list.getFirst().equals(pos.offset(1, 0, 0))) {
                    if (lastClick.get() + throttleTicks.value() >= tickCounter.get()) {
                        event.cancel();
                        return;
                    }
                    list.removeFirst();
                    lastClick.set(tickCounter.get());
                } else if (shouldBlock()) {
                    event.cancel();
                }
            }
        }
    }

    @EventHandler
    private static void onBlockBreak(AttackBlockEvent event) {
        if (isSolverActive() && DungeonUtil.isInBossRoom("7")) {
            Vec3 pos = Vec3.atCenterOf(event.blockPos);
            if (lightsArea.contains(pos) || buttonsArea.contains(pos)) {
                event.cancel(); // prevent device blocks from being accidentally broken to not break the solver
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (isSolverActive() && DungeonUtil.isInBossRoom("7")) {
            tickCounter.incrementAndGet();
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (isSolverActive() && DungeonUtil.isInBossRoom("7") && isDeviceActive()) {
            if (event.isSound(SoundEvents.ENDERMAN_TELEPORT) && event.volume() == 8.0f && event.pitch() == 0.0f) {
                failedAt.set(tickCounter.get());
            }
            if (event.isSound(SoundEvents.NOTE_BLOCK_PLING) && event.volume() == 8.0f && event.pitch() == 4.047619f) {
                validClicks.incrementAndGet();
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (isSolverActive() && !list.isEmpty() && DungeonUtil.isInBossRoom("7") && isDeviceActive()) {
            for (BlockPos pos : list) {
                Pair<RenderColor, RenderColor> color = switch (list.indexOf(pos)) {
                    case 0 -> Pair.of(firstColorOutline.value(), firstColorFill.value());
                    case 1 -> Pair.of(secondColorOutline.value(), secondColorFill.value());
                    default -> Pair.of(thirdColorOutline.value(), thirdColorFill.value());
                };
                event.drawStyled(AABB.encapsulatingFullBlocks(pos, pos), style.value(), false, color.getLeft(), color.getRight());
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        failedAt.set(0);
        validClicks.set(0);
        tickCounter.set(0);
        lastClick.set(0);
    }
}
