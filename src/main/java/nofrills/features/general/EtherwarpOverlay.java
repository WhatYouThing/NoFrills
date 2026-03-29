package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import nofrills.config.*;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class EtherwarpOverlay {
    public static final Feature instance = new Feature("etherwarpOverlay");

    public static final SettingBool doSound = new SettingBool(false, "doSound", instance);
    public static final SettingString sound = new SettingString("minecraft:block.note_block.pling", "sound", instance);
    public static final SettingDouble volume = new SettingDouble(1.0, "volume", instance);
    public static final SettingDouble pitch = new SettingDouble(1.0, "pitch", instance);
    public static final SettingBool cancelSound = new SettingBool(false, "cancelSound", instance);
    public static final SettingEnum<Style> highlightStyle = new SettingEnum<>(Style.Filled, Style.class, "highlightStyle", instance.key());
    public static final SettingColor fillCorrect = new SettingColor(new RenderColor(0, 255, 0, 127), "fillCorrect", instance.key());
    public static final SettingColor fillWrong = new SettingColor(new RenderColor(255, 0, 0, 127), "fillWrong", instance.key());
    public static final SettingColor outlineCorrect = new SettingColor(new RenderColor(0, 255, 0, 255), "outlineCorrect", instance.key());
    public static final SettingColor outlineWrong = new SettingColor(new RenderColor(255, 0, 0, 255), "outlineWrong", instance.key());

    private static final int baseDistance = 57;
    private static boolean lastWarpValid = false;
    private static boolean usedThisTick = false;

    private static boolean isBlockValid(BlockPos pos, int offset) {
        boolean isAbove = offset > 0;
        BlockState state = mc.level.getBlockState(pos.above(offset));
        return switch (state.getBlock()) {
            case WoolCarpetBlock ignored -> isAbove;
            case PlayerHeadBlock ignored -> isAbove;
            case SkullBlock ignored -> isAbove;
            case WallSkullBlock ignored -> isAbove;
            case CarpetBlock ignored -> isAbove;
            case TrapDoorBlock ignored -> !isAbove;
            case HopperBlock ignored -> !isAbove;
            case StainedGlassPaneBlock ignored -> !isAbove;
            case IronBarsBlock ignored -> !isAbove;
            case AbstractCauldronBlock ignored -> !isAbove;
            case WallBannerBlock ignored -> !isAbove;
            case BannerBlock ignored -> !isAbove;
            case StandingSignBlock ignored -> !isAbove;
            case WallSignBlock ignored -> !isAbove;
            case AzaleaBlock ignored -> !isAbove;
            case LilyPadBlock ignored -> !isAbove;
            case LanternBlock ignored -> !isAbove;
            case LadderBlock ignored -> isAbove;
            case SnowLayerBlock ignored ->
                    isAbove ? state.getValue(BlockStateProperties.LAYERS) < 8 : state.getValue(BlockStateProperties.LAYERS) == 8;
            default ->
                    isAbove ? !state.canOcclude() && !state.isCollisionShapeFullBlock(mc.level, pos) : state.canOcclude() || state.isCollisionShapeFullBlock(mc.level, pos);
        };
    }

    private static int getWarpDistance() {
        CompoundTag data = Utils.getCustomData(Utils.getHeldItem());
        String itemId = Utils.getSkyblockId(data);
        if (data != null && !itemId.isEmpty()) {
            if (data.getByte("ethermerge").orElse((byte) 0) == 1 && mc.options.keyShift.isDown()) {
                if (itemId.equals("ASPECT_OF_THE_END") || itemId.equals("ASPECT_OF_THE_VOID")) {
                    return baseDistance + data.getInt("tuned_transmission").orElse(0);
                }
            } else if (itemId.equals("ETHERWARP_CONDUIT")) {
                return baseDistance;
            }
        }
        return 0;
    }

    private static void playCustomSound() {
        if (lastWarpValid && doSound.value()) {
            Utils.playSound(SoundEvent.createVariableRangeEvent(Identifier.parse(sound.value())), volume.valueFloat(), pitch.valueFloat());
        }
    }

    @EventHandler
    public static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && mc.player != null) {
            int dist = getWarpDistance();
            if (dist > 0) {
                HitResult hitResult = Utils.raycastFullBlock(mc.player, dist, event.tickCounter.getGameTimeDeltaPartialTick(true));
                if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult) {
                    BlockPos pos = blockHitResult.getBlockPos();
                    boolean valid = isBlockValid(pos, 0) && isBlockValid(pos, 1) && isBlockValid(pos, 2);
                    AABB box = AABB.encapsulatingFullBlocks(pos, pos);
                    lastWarpValid = valid;
                    if (!highlightStyle.value().equals(Style.Outline)) {
                        event.drawFilled(box, true, valid ? fillCorrect.value() : fillWrong.value());
                    }
                    if (!highlightStyle.value().equals(Style.Filled)) {
                        event.drawOutline(box, true, valid ? outlineCorrect.value() : outlineWrong.value());
                    }
                    return;
                }
            }
            lastWarpValid = false;
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (instance.isActive() && !usedThisTick && mc.level != null) {
            usedThisTick = true;
            Block block = mc.level.getBlockState(event.blockHitResult.getBlockPos()).getBlock();
            if (block instanceof ChestBlock || block instanceof HopperBlock) {
                return;
            }
            playCustomSound();
        }
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (instance.isActive() && !usedThisTick) {
            usedThisTick = true;
            playCustomSound();
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && cancelSound.value()) {
            if (event.isSound(SoundEvents.ENDER_DRAGON_HURT) && event.packet.getPitch() == 0.53968257f)
                event.cancel();
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && usedThisTick) {
            usedThisTick = false;
        }
    }

    public enum Style {
        Outline,
        Filled,
        Both
    }
}
