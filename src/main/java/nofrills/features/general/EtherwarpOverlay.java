package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
        BlockState state = mc.world.getBlockState(pos.up(offset));
        return switch (state.getBlock()) {
            case DyedCarpetBlock ignored -> isAbove;
            case PlayerSkullBlock ignored -> isAbove;
            case SkullBlock ignored -> isAbove;
            case WallSkullBlock ignored -> isAbove;
            case CarpetBlock ignored -> isAbove;
            case TrapdoorBlock ignored -> !isAbove;
            case HopperBlock ignored -> !isAbove;
            case StainedGlassPaneBlock ignored -> !isAbove;
            case PaneBlock ignored -> !isAbove;
            case AbstractCauldronBlock ignored -> !isAbove;
            case WallBannerBlock ignored -> !isAbove;
            case BannerBlock ignored -> !isAbove;
            case SignBlock ignored -> !isAbove;
            case WallSignBlock ignored -> !isAbove;
            case AzaleaBlock ignored -> !isAbove;
            case LilyPadBlock ignored -> !isAbove;
            case LanternBlock ignored -> !isAbove;
            case LadderBlock ignored -> isAbove;
            case SnowBlock ignored -> isAbove ? state.get(Properties.LAYERS) < 8 : state.get(Properties.LAYERS) == 8;
            default ->
                    isAbove ? !state.isOpaque() && !state.isFullCube(mc.world, pos) : state.isOpaque() || state.isFullCube(mc.world, pos);
        };
    }

    private static int getWarpDistance() {
        NbtCompound data = Utils.getCustomData(Utils.getHeldItem());
        String itemId = Utils.getSkyblockId(data);
        if (data != null && !itemId.isEmpty()) {
            if (data.getByte("ethermerge").orElse((byte) 0) == 1 && mc.options.sneakKey.isPressed()) {
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
            Utils.playSound(SoundEvent.of(Identifier.of(sound.value())), volume.valueFloat(), pitch.valueFloat());
        }
    }

    @EventHandler
    public static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && mc.player != null) {
            int dist = getWarpDistance();
            if (dist > 0) {
                HitResult hitResult = Utils.raycastFullBlock(mc.player, dist, event.tickCounter.getTickProgress(true));
                if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult) {
                    BlockPos pos = blockHitResult.getBlockPos();
                    boolean valid = isBlockValid(pos, 0) && isBlockValid(pos, 1) && isBlockValid(pos, 2);
                    Box box = Box.enclosing(pos, pos);
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
        if (instance.isActive() && !usedThisTick && mc.world != null) {
            usedThisTick = true;
            Block block = mc.world.getBlockState(event.blockHitResult.getBlockPos()).getBlock();
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
            if (event.isSound(SoundEvents.ENTITY_ENDER_DRAGON_HURT) && event.packet.getPitch() == 0.53968257f)
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
