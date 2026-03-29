package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingDouble;
import nofrills.events.*;
import nofrills.misc.ConcurrentHashSet;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import static nofrills.Main.mc;
import static nofrills.hud.HudManager.terraGyroTimer;

public class TerracottaTimer {
    public static final Feature instance = new Feature("terracottaTimer");

    public static final SettingDouble scale = new SettingDouble(0.4, "scale", instance);
    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0xffff00), "color", instance);

    private static final ConcurrentHashSet<Terracotta> terracottas = new ConcurrentHashSet<>();

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("6")) {
            if (event.messagePlain.equals("[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!")) {
                terraGyroTimer.setStartTicks(267);
                terraGyroTimer.start();
            }
        }
    }

    @EventHandler
    private static void onBlockUpdate(BlockUpdateEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("6") && event.oldState.isAir()) {
            if (event.newState.getBlock() instanceof FlowerPotBlock) { // EVERY POTTED FLOWER HAS ITS OWN BLOCK ID AAAAAAAAHHH
                terracottas.add(new Terracotta(event.pos, Utils.isOnDungeonFloor("M6") ? 240 : 300));
            }
            if (!terraGyroTimer.isTicking() && event.newState.getBlock().equals(Blocks.NETHER_BRICK_FENCE)) {
                terraGyroTimer.setStartTicks(235);
                terraGyroTimer.start();
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("6")) {
            for (Terracotta terra : terracottas) {
                if (terra.ticks == 0 || mc.level.getBlockState(terra.pos).isAir()) {
                    terracottas.remove(terra);
                    continue;
                }
                MutableComponent text = Component.literal(Utils.formatDecimal(terra.ticks / 20.0f) + "s");
                event.drawText(terra.pos.getCenter(), text, scale.valueFloat() * 0.1f, true, color.value());
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        terracottas.clear();
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("6")) {
            for (Terracotta terra : terracottas) {
                terra.tick();
            }
        }
    }

    private static class Terracotta {
        public BlockPos pos;
        public int ticks;

        public Terracotta(BlockPos pos, int ticks) {
            this.pos = pos;
            this.ticks = ticks;
        }

        public void tick() {
            if (this.ticks > 0) {
                this.ticks--;
            }
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Terracotta terracotta && this.pos.equals(terracotta.pos);
        }

        @Override
        public int hashCode() {
            return this.pos.hashCode();
        }
    }
}
