package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.hud.elements.BuilderRulerCounter;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class BuilderRulerPreview {
    public static final Feature instance = new Feature("builderRulerPreview");

    public static final SettingEnum<RenderStyle> highlightStyle = new SettingEnum<>(RenderStyle.Outline, RenderStyle.class, "highlightStyle", instance.key());
    public static final SettingColor placeColor = new SettingColor(new RenderColor(0, 200, 255, 160), "placeColor", instance.key());
    public static final SettingColor destroyColor = new SettingColor(new RenderColor(255, 80, 80, 160), "destroyColor", instance.key());
    public static final SettingColor outlinePlaceColor = new SettingColor(new RenderColor(0, 200, 255, 255), "outlinePlaceColor", instance.key());
    public static final SettingColor outlineDestroyColor = new SettingColor(new RenderColor(255, 80, 80, 255), "outlineDestroyColor", instance.key());

    private static final int MAX_RANGE = 241;

    private static final int PLOT_SIZE = 96;
    private static final int PLOT_OFFSET = 48;

    private static final List<BlockPos> cachedBlocks = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static boolean cachedPlace = true;

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        cachedBlocks.clear();
        if (!instance.isActive() || mc.player == null || mc.world == null) return;

        String id = Utils.getSkyblockId(Utils.getCustomData(Utils.getHeldItem()));
        if (id == null || !id.equals("BUILDERS_RULER")) {
            if (BuilderRulerCounter.instance != null) BuilderRulerCounter.instance.hide();
            return;
        }

        cachedPlace = !mc.options.sneakKey.isPressed();

        HitResult hit = mc.player.raycast(5.0, 0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos target = blockHit.getBlockPos();
        Direction face = blockHit.getSide();
        Direction moveDir = mc.player.getHorizontalFacing();

        if (cachedPlace) {
            BlockPos startPos = target.offset(face);
            BlockPos cursor = startPos;
            for (int i = 0; i < MAX_RANGE; i++) {
                if (!mc.world.getBlockState(cursor).isAir()) break;
                if (!isInPlot(startPos, cursor)) break;
                cachedBlocks.add(cursor);
                cursor = cursor.offset(moveDir);
            }
        } else {
            net.minecraft.block.Block startBlock = mc.world.getBlockState(target).getBlock();
            BlockPos cursor = target;
            for (int i = 0; i < MAX_RANGE; i++) {
                BlockState state = mc.world.getBlockState(cursor);
                if (state.getBlock() != startBlock) break;
                if (!isInPlot(target, cursor)) break;
                cachedBlocks.add(cursor);
                cursor = cursor.offset(moveDir);
            }
        }

        if (BuilderRulerCounter.instance != null) {
            BuilderRulerCounter.instance.update(cachedBlocks.size(), cachedBlocks.size(), cachedPlace);
        }
    }

    @EventHandler
    public static void onRender(WorldRenderEvent event) {
        if (!instance.isActive() || mc.player == null || mc.world == null || cachedBlocks.isEmpty()) return;

        String id = Utils.getSkyblockId(Utils.getCustomData(Utils.getHeldItem()));
        if (id == null || !id.equals("BUILDERS_RULER")) return;

        RenderColor fill = cachedPlace ? placeColor.value() : destroyColor.value();
        RenderColor outline = cachedPlace ? outlinePlaceColor.value() : outlineDestroyColor.value();

        for (BlockPos pos : cachedBlocks) {
            event.drawStyled(new Box(pos), highlightStyle.value(), true, outline, fill);
        }
    }

    private static boolean isInPlot(BlockPos startPos, BlockPos pos) {
        if (!Utils.isInGarden()) {
            return true;
        }

        return Math.floorDiv(startPos.getX() + PLOT_OFFSET, PLOT_SIZE) == Math.floorDiv(pos.getX() + PLOT_OFFSET, PLOT_SIZE)
                && Math.floorDiv(startPos.getZ() + PLOT_OFFSET, PLOT_SIZE) == Math.floorDiv(pos.getZ() + PLOT_OFFSET, PLOT_SIZE);
    }
}
