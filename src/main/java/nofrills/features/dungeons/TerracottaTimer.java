package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingDouble;
import nofrills.events.*;
import nofrills.misc.ConcurrentHashSet;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;

@EventListener
public class TerracottaTimer {
    public static final Feature instance = new Feature("terracottaTimer");

    public static final SettingDouble scale = new SettingDouble(0.4, "scale", instance);

    private static final ConcurrentHashSet<Terracotta> terracottas = new ConcurrentHashSet<>();

    @EventHandler
    private static void onBlockUpdate(BlockUpdateEvent event) {
        if (instance.isActive() && DungeonUtil.isInBossRoom("6")) {
            if (event.oldState.isAir() && event.newState.getBlock() instanceof FlowerPotBlock) {
                terracottas.add(new Terracotta(event.pos.immutable(), DungeonUtil.isOnFloor("M6") ? 240 : 300));
            }
            if (event.newState.isAir()) {
                BlockPos pos = event.pos.immutable();
                terracottas.removeIf(t -> t.pos.equals(pos));
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && DungeonUtil.isInBossRoom("6")) {
            for (Terracotta terra : terracottas) {
                if (terra.ticks == 0) {
                    terracottas.remove(terra);
                    continue;
                }
                event.drawText(
                        Vec3.atCenterOf(terra.pos),
                        Component.literal(Utils.formatDecimal(terra.ticks / 20.0f) + "s"),
                        scale.valueFloat() * 0.1f,
                        true,
                        Utils.getPercentageColor(terra.ticks / (double) terra.maxTicks, true)
                );
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        terracottas.clear();
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (instance.isActive() && DungeonUtil.isInBossRoom("6")) {
            for (Terracotta terra : terracottas) {
                terra.tick();
            }
        }
    }

    private static class Terracotta {
        public final int maxTicks;
        public BlockPos pos;
        public int ticks;

        public Terracotta(BlockPos pos, int ticks) {
            this.maxTicks = ticks;
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
