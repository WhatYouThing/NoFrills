package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class EtherwarpOverlay {
    public static final Feature instance = new Feature("etherwarpOverlay");

    public static final SettingEnum<style> highlightStyle = new SettingEnum<>(style.Filled, style.class, "highlightStyle", instance.key());
    public static final SettingColor fillCorrect = new SettingColor(new RenderColor(0, 255, 0, 127), "fillCorrect", instance.key());
    public static final SettingColor fillWrong = new SettingColor(new RenderColor(255, 0, 0, 127), "fillWrong", instance.key());
    public static final SettingColor outlineCorrect = new SettingColor(new RenderColor(0, 255, 0, 255), "outlineCorrect", instance.key());
    public static final SettingColor outlineWrong = new SettingColor(new RenderColor(255, 0, 0, 255), "outlineWrong", instance.key());

    private static final int baseDistance = 57;

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
                    if (!highlightStyle.value().equals(style.Outline)) {
                        event.drawFilled(box, true, valid ? fillCorrect.value() : fillWrong.value());
                    }
                    if (!highlightStyle.value().equals(style.Filled)) {
                        event.drawOutline(box, true, valid ? outlineCorrect.value() : outlineWrong.value());
                    }
                }
            }
        }
    }

    public enum style {
        Outline,
        Filled,
        Both
    }
}
