package nofrills.features.dungeons.devicesolvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.*;
import nofrills.hud.HudManager;
import nofrills.misc.*;

import static nofrills.Main.mc;

@EventListener
public class SharpshooterSolver {
    public static final SettingBool doneAlert = new SettingBool(false, "sharpDoneAlert", DeviceSolvers.instance);
    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Filled, RenderStyle.class, "sharpStyle", DeviceSolvers.instance);
    public static final SettingColor targetColorFill = new SettingColor(RenderColor.GREEN, "sharpTargetColor", DeviceSolvers.instance);
    public static final SettingColor targetColorOutline = new SettingColor(RenderColor.GREEN, "sharpTargetColorOutline", DeviceSolvers.instance);
    public static final SettingColor hitColorFill = new SettingColor(RenderColor.RED, "sharpHitColor", DeviceSolvers.instance);
    public static final SettingColor hitColorOutline = new SettingColor(RenderColor.RED, "sharpHitColorOutline", DeviceSolvers.instance);

    private static final ConcurrentHashSet<BlockPos> list = new ConcurrentHashSet<>();
    private static final AABB target = AABB.encapsulatingFullBlocks(new BlockPos(68, 130, 50), new BlockPos(64, 126, 50));
    private static final BlockPos area = new BlockPos(63, 126, 35);
    private static BlockPos next = null;
    private static boolean done = false;

    private static boolean isDeviceActive() {
        return mc.level != null && mc.level.hasSignal(area, Direction.DOWN);
    }

    private static boolean isTargetBlock(BlockPos pos) {
        return target.contains(pos.getCenter()) && pos.getX() % 2 == 0 && pos.getY() % 2 == 0;
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (DeviceSolvers.instance.isActive() && DeviceSolvers.sharpshooter.value() && DungeonUtil.isInBossRoom("7")) {
            if (isDeviceActive() && (!list.isEmpty() || next != null)) {
                next = null;
                list.clear();
            }
        }
    }

    @EventHandler
    public static void onBlockUpdate(BlockUpdateEvent event) {
        if (DeviceSolvers.instance.isActive() && DeviceSolvers.sharpshooter.value() && DungeonUtil.isInBossRoom("7") && isTargetBlock(event.pos)) {
            if (!isDeviceActive()) return;
            if (event.oldBlock.equals(Blocks.EMERALD_BLOCK) && event.newBlock.equals(Blocks.BLUE_TERRACOTTA)) {
                list.add(event.pos.immutable());
                if (next != null && next.equals(event.pos)) {
                    next = null;
                }
            } else if (event.oldBlock.equals(Blocks.BLUE_TERRACOTTA) && event.newBlock.equals(Blocks.EMERALD_BLOCK)) {
                list.remove(event.pos);
                next = event.pos.immutable();
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (DeviceSolvers.instance.isActive() && DeviceSolvers.sharpshooter.value() && DungeonUtil.isInBossRoom("7")) {
            if (!list.isEmpty()) {
                for (BlockPos pos : list) {
                    if (pos.equals(next)) continue;
                    AABB box = AABB.encapsulatingFullBlocks(pos, pos);
                    event.drawStyled(box, style.value(), false, hitColorOutline.value(), hitColorFill.value());
                }
            }
            if (next != null) {
                AABB box = AABB.encapsulatingFullBlocks(next, next);
                event.drawStyled(box, style.value(), false, targetColorOutline.value(), targetColorFill.value());
            }
        }
    }

    @EventHandler
    private static void onEntityNamed(EntityNamedEvent event) {
        if (DeviceSolvers.instance.isActive() && DeviceSolvers.sharpshooter.value() && DungeonUtil.isInBossRoom("7")) {
            if (!done && event.namePlain.equals("Active") && area.getCenter().distanceTo(event.entity.position()) < 3.0) {
                if (doneAlert.value()) {
                    HudManager.setCustomTitle("§aSharpshooter Done", 40);
                    Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }
                done = true;
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        done = false;
        next = null;
        list.clear();
    }
}
