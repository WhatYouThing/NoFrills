package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import nofrills.config.*;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import nofrills.misc.Utils;

import java.util.Optional;

import static nofrills.Main.mc;

public class EtherwarpOverlay {
    public static final Feature instance = new Feature("etherwarpOverlay");

    public static final SettingBool doSound = new SettingBool(false, "doSound", instance);
    public static final SettingBool pinglessSound = new SettingBool(true, "pinglessSound", instance);
    public static final SettingString sound = new SettingString("minecraft:block.note_block.pling", "sound", instance);
    public static final SettingDouble volume = new SettingDouble(1.0, "volume", instance);
    public static final SettingDouble pitch = new SettingDouble(1.0, "pitch", instance);
    public static final SettingBool cancelSound = new SettingBool(false, "cancelSound", instance);
    public static final SettingEnum<RenderStyle> highlightStyle = new SettingEnum<>(RenderStyle.Filled, RenderStyle.class, "highlightStyle", instance.key());
    public static final SettingColor fillCorrect = new SettingColor(new RenderColor(0, 255, 0, 127), "fillCorrect", instance.key());
    public static final SettingColor fillWrong = new SettingColor(new RenderColor(255, 0, 0, 127), "fillWrong", instance.key());
    public static final SettingColor outlineCorrect = new SettingColor(new RenderColor(0, 255, 0, 255), "outlineCorrect", instance.key());
    public static final SettingColor outlineWrong = new SettingColor(new RenderColor(255, 0, 0, 255), "outlineWrong", instance.key());

    private static final int baseDistance = 57;
    private static boolean usedThisTick = false;

    private static boolean isBlockValid(BlockPos pos, int offset) {
        boolean isAbove = offset > 0;
        BlockState state = mc.level.getBlockState(pos.above(offset));
        return switch (state.getBlock()) {
            case WoolCarpetBlock ignored -> !isAbove;
            case PlayerHeadBlock ignored -> !isAbove;
            case SkullBlock ignored -> !isAbove;
            case WallSkullBlock ignored -> !isAbove;
            case CarpetBlock ignored -> !isAbove;
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
            case LadderBlock ignored -> false;
            case AirBlock ignored -> true;
            case TorchBlock ignored -> true;
            case EndPortalBlock ignored -> false;
            case FlowerPotBlock ignored -> !isAbove;
            case DoorBlock ignored -> !isAbove;
            case BrewingStandBlock ignored -> !isAbove;
            case SnowLayerBlock ignored -> !isAbove;
            case PressurePlateBlock ignored -> !isAbove;
            case WeightedPressurePlateBlock ignored -> !isAbove;
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

    private static Optional<BlockPos> doRaycast(int distance, float delta) {
        if (mc.player != null && mc.level != null) {
            HitResult hitResult = Utils.raycastFullBlock(mc.player, distance, delta);
            if (hitResult.getType().equals(HitResult.Type.BLOCK) && hitResult instanceof BlockHitResult blockHitResult) {
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState state = mc.level.getBlockState(pos);
                return switch (state.getBlock()) {
                    case FenceBlock ignored -> Optional.of(pos.above(1));
                    case FenceGateBlock ignored -> Optional.of(pos.above(1));
                    case WallBlock ignored -> Optional.of(pos.above(1));
                    default -> Optional.of(pos);
                };
            }
        }
        return Optional.empty();
    }

    private static void playCustomSound() {
        Utils.playSound(sound.value(), volume.valueFloat(), pitch.valueFloat());
    }

    private static void playPinglessSound() {
        int dist = getWarpDistance();
        if (dist > 0) {
            doRaycast(dist, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)).ifPresent(pos -> {
                if (isBlockValid(pos, 0) && isBlockValid(pos, 1) && isBlockValid(pos, 2)) {
                    playCustomSound();
                }
            });
        }
    }

    @EventHandler
    public static void onRender(WorldRenderEvent event) {
        if (instance.isActive()) {
            int dist = getWarpDistance();
            if (dist > 0) {
                doRaycast(dist, event.delta()).ifPresent(pos -> {
                    boolean valid = isBlockValid(pos, 0) && isBlockValid(pos, 1) && isBlockValid(pos, 2);
                    AABB box = AABB.encapsulatingFullBlocks(pos, pos);
                    event.drawStyled(
                            box,
                            highlightStyle.value(),
                            true,
                            valid ? outlineCorrect.value() : outlineWrong.value(),
                            valid ? fillCorrect.value() : fillWrong.value()
                    );
                });
            }
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (instance.isActive() && !usedThisTick && doSound.value() && pinglessSound.value()) {
            playPinglessSound();
            usedThisTick = true;
        }
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (instance.isActive() && !usedThisTick && doSound.value() && pinglessSound.value()) {
            playPinglessSound();
            usedThisTick = true;
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (instance.isActive() && event.isSound(SoundEvents.ENDER_DRAGON_HURT) && event.packet.getPitch() == 0.53968257f) {
            if (cancelSound.value()) {
                event.cancel();
            }
            if (doSound.value() && !pinglessSound.value()) {
                playCustomSound();
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && usedThisTick) {
            usedThisTick = false;
        }
    }
}
