package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import nofrills.config.Feature;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

public class TerracottaTimer {
    public static final Feature instance = new Feature("terracottaTimer");

    private static final List<SpawningTerracotta> terracottas = new ArrayList<>();
    private static final BlockPos sadanPit = new BlockPos(-9, 67, 66);
    private static int gyroTicks = 0;

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive()) {
            if (event.messagePlain.equals("[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!")) {
                gyroTicks = 267;
            }
            if (event.messagePlain.equals("[BOSS] Sadan: ENOUGH!")) {
                terracottas.clear();
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("6")) {
            if (gyroTicks > 0) {
                Utils.showTitleCustom("GYRO: " + Utils.formatDecimal(gyroTicks / 20.0f) + "s", 1, 25, 2.5f, 0xffff00);
            }
        }
    }

    @EventHandler
    private static void onBlockUpdate(BlockUpdateEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("6") && SkyblockData.getLines().stream().anyMatch(line -> line.endsWith("sadan"))) {
            if (event.newState.getBlock() instanceof FlowerPotBlock) { // EVERY POTTED FLOWER HAS ITS OWN BLOCK ID AAAAAAAAHHH
                if (terracottas.stream().noneMatch(terra -> terra.pos.equals(event.pos))) {
                    terracottas.add(new SpawningTerracotta(event.pos, Utils.isOnDungeonFloor("M6") ? 240 : 300));
                }
            }
            if (event.pos.equals(sadanPit) && event.newState.isAir() && event.oldState.getBlock().equals(Blocks.GRAY_STAINED_GLASS)) {
                gyroTicks = 195;
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !terracottas.isEmpty()) {
            List<SpawningTerracotta> terras = new ArrayList<>(terracottas);
            for (SpawningTerracotta terra : terras) {
                if (terra.pos != null) {
                    event.drawText(terra.pos.toCenterPos(), Text.of(Utils.formatDecimal(terra.ticks / 20.0f) + "s"), 0.035f, true, RenderColor.fromHex(0xffff00));
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        gyroTicks = 0;
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("6")) {
            if (gyroTicks > 0) {
                gyroTicks--;
            }
            List<SpawningTerracotta> terras = new ArrayList<>(terracottas);
            for (SpawningTerracotta terra : terras) {
                terra.ticks -= 1;
            }
            terracottas.removeIf(terra -> terra.ticks <= 0);
        }
    }

    private static class SpawningTerracotta {
        public BlockPos pos;
        public int ticks;

        public SpawningTerracotta(BlockPos pos, int ticks) {
            this.pos = pos;
            this.ticks = ticks;
        }
    }
}
