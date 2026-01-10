package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.*;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class TerracottaTimer {
    public static final Feature instance = new Feature("terracottaTimer");

    public static final SettingBool mageCheck = new SettingBool(false, "mageCheck", instance);
    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0xffff00), "color", instance);

    private static final List<SpawningTerracotta> terracottas = new ArrayList<>();
    private static int gyroTicks = 0;

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("6")) {
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
        if (instance.isActive() && gyroTicks > 0 && Utils.isInDungeonBoss("6")) {
            if (mageCheck.value() && !SkyblockData.dungeonClass.equals("Mage")) return;
            Utils.showTitleCustom("GYRO: " + Utils.formatDecimal(gyroTicks / 20.0f) + "s", 1, 25, 2.5f, color.value());
        }
    }

    @EventHandler
    private static void onBlockUpdate(BlockUpdateEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("6")) {
            if (event.newState.getBlock() instanceof FlowerPotBlock) { // EVERY POTTED FLOWER HAS ITS OWN BLOCK ID AAAAAAAAHHH
                if (terracottas.stream().noneMatch(terra -> terra.pos.equals(event.pos))) {
                    terracottas.add(new SpawningTerracotta(event.pos, Utils.isOnDungeonFloor("M6") ? 240 : 300));
                }
            }
            if (gyroTicks == 0 && event.oldState.isAir() && event.newState.getBlock().equals(Blocks.NETHER_BRICK_FENCE)) {
                gyroTicks = 235;
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !terracottas.isEmpty() && Utils.isInDungeonBoss("6")) {
            for (SpawningTerracotta terra : terracottas) {
                if (terra.pos == null) continue;
                MutableText text = Text.literal(Utils.formatDecimal(terra.ticks / 20.0f) + "s");
                event.drawText(terra.pos.toCenterPos(), text, 0.035f, true, color.value());
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        gyroTicks = 0;
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("6")) {
            if (gyroTicks > 0) {
                gyroTicks--;
            }

            for (Iterator<SpawningTerracotta> it = terracottas.iterator(); it.hasNext();) {
                SpawningTerracotta terra = it.next();

                terra.tick();

                if (terra.ticks == 0) {
                    it.remove();
                }
            }
        }
    }

    private static class SpawningTerracotta {
        public BlockPos pos;
        public int ticks;

        public SpawningTerracotta(BlockPos pos, int ticks) {
            this.pos = pos;
            this.ticks = ticks;
        }

        public void tick() {
            if (this.ticks > 0) {
                this.ticks--;
            }
        }
    }
}
