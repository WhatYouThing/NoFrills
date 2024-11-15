package nofrills.features;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Config;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.Utils;
import org.lwjgl.opengl.GL11;

import static nofrills.Main.mc;

public class EtherwarpOverlay {
    private static final int baseDistance = 57;

    private static boolean isBlockValid(BlockState state, BlockPos pos, boolean isAbove) {
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
            case CauldronBlock ignored -> !isAbove;
            case WallBannerBlock ignored -> !isAbove;
            case BannerBlock ignored -> !isAbove;
            case SignBlock ignored -> !isAbove;
            case WallSignBlock ignored -> !isAbove;
            case SnowBlock ignored -> isAbove ? state.get(Properties.LAYERS) < 8 : state.get(Properties.LAYERS) == 8;
            default ->
                    isAbove ? !state.isOpaque() && !state.isFullCube(mc.world, pos) : state.isOpaque() || state.isFullCube(mc.world, pos);
        };
    }

    @EventHandler
    public static void onRender(WorldRenderEvent event) {
        if (Config.overlayEtherwarp && mc.options.sneakKey.isPressed()) {
            ItemStack item = mc.player.getMainHandStack();
            NbtComponent component = item.get(DataComponentTypes.CUSTOM_DATA);
            if (!item.isEmpty() && component != null) {
                NbtCompound data = component.copyNbt();
                String itemId = data.getString("id");
                int dist = 0;
                if (itemId.equals("ASPECT_OF_THE_END") || itemId.equals("ASPECT_OF_THE_VOID")) {
                    if (data.contains("ethermerge") && data.getByte("ethermerge") == 1) {
                        int extraDist = data.contains("tuned_transmission") ? data.getInt("tuned_transmission") : 0;
                        dist = baseDistance + extraDist;
                    }
                } else if (itemId.equals("ETHERWARP_CONDUIT")) {
                    dist = baseDistance;
                }
                if (dist > 0) {
                    HitResult hitResult = Utils.raycastFullBlock(mc.player, dist, event.tickCounter.getTickDelta(true));
                    if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult) {
                        BlockPos pos = blockHitResult.getBlockPos();
                        BlockPos posUpOne = pos.up(1);
                        BlockPos posUpTwo = pos.up(2);
                        BlockState state = mc.world.getBlockState(pos);
                        BlockState stateUpOne = mc.world.getBlockState(posUpOne);
                        BlockState stateUpTwo = mc.world.getBlockState(posUpTwo);
                        event.matrices.push();
                        Vec3d camPos = event.camera.getPos().negate();
                        event.matrices.translate(camPos.x, camPos.y, camPos.z);
                        float red = 1.0f;
                        float green = 0.0f;
                        if (isBlockValid(state, pos, false) && isBlockValid(stateUpOne, posUpOne, true) && isBlockValid(stateUpTwo, posUpTwo, true)) {
                            red = 0.0f;
                            green = 1.0f;
                        }
                        RenderSystem.enableDepthTest(); // had to sprinkle in some voodoo so that this actually works with sodium
                        RenderSystem.depthFunc(GL11.GL_ALWAYS);
                        WorldRenderer.renderFilledBox(event.matrices, event.consumer.getBuffer(Utils.renderLayers.boxFilledNoCull), pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, red, green, 0.0f, 0.33f);
                        event.matrices.pop();
                        RenderSystem.disableDepthTest();
                        RenderSystem.depthFunc(GL11.GL_LEQUAL);
                    }
                }
            }
        }
    }
}
