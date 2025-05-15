package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Config;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class EtherwarpOverlay {
    private static final int baseDistance = 57;
    private static final RenderColor colorCorrect = new RenderColor(0, 255, 0, 127);
    private static final RenderColor colorWrong = new RenderColor(255, 0, 0, 127);

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
            case CauldronBlock ignored -> isAbove;
            case WallBannerBlock ignored -> !isAbove;
            case BannerBlock ignored -> !isAbove;
            case SignBlock ignored -> !isAbove;
            case WallSignBlock ignored -> !isAbove;
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
        if (Config.overlayEtherwarp) {
            int dist = getWarpDistance();
            if (dist > 0) {
                HitResult hitResult = Utils.raycastFullBlock(mc.player, dist, event.tickCounter.getTickProgress(true));
                if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult) {
                    BlockPos pos = blockHitResult.getBlockPos();
                    Box box = Box.enclosing(pos, pos);
                    boolean valid = isBlockValid(pos, 0) && isBlockValid(pos, 1) && isBlockValid(pos, 2);
                    event.drawFilled(box, true, valid ? colorCorrect : colorWrong);
                }
            }
        }
    }
}