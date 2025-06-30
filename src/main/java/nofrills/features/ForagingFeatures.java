package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Config;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.SpawnParticleEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class ForagingFeatures {
    private static final List<Block> treeBlockList = List.of(
            Blocks.MANGROVE_WOOD,
            Blocks.MANGROVE_LEAVES,
            Blocks.STRIPPED_SPRUCE_WOOD,
            Blocks.AZALEA_LEAVES
    );
    private static final List<Invisibug> invisibugList = new ArrayList<>();
    private static final RenderColor invisibugColor = RenderColor.fromHex(0xff0000, 0.5f);

    public static boolean isTreeBlock(Entity entity) {
        if (Utils.isInArea("Galatea") && entity instanceof DisplayEntity.BlockDisplayEntity blockDisplay) {
            Block block = blockDisplay.getBlockState().getBlock();
            for (Block blacklisted : treeBlockList) {
                if (block.equals(blacklisted)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInvisibugParticle(ParticleS2CPacket packet) {
        return packet.getCount() == 1 && packet.getSpeed() == 0.0f && packet.getOffsetX() == 0.0f
                && packet.getOffsetY() == 0.0f && packet.getOffsetZ() == 0.0f
                && packet.isImportant() && packet.shouldForceSpawn();
    }

    private static boolean hasInvisibugMarker(Vec3d pos) {
        if (mc.world != null) {
            List<Entity> other = mc.world.getOtherEntities(null, Box.of(pos, 1, 2, 1));
            if (other.size() == 1 && other.getFirst() instanceof ArmorStandEntity marker) {
                return marker.isMarker() && marker.getCustomName() == null && marker.getMainHandStack().isEmpty();
            }
        }
        return false;
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (Config.invisibugHighlight && Utils.isInArea("Galatea") && event.type == ParticleTypes.CRIT && isInvisibugParticle(event.packet)) {
            if (hasInvisibugMarker(event.pos)) {
                for (Invisibug bug : new ArrayList<>(invisibugList)) {
                    if (bug.isNear(event.pos)) {
                        bug.add(event.pos);
                        return;
                    }
                }
                invisibugList.add(new Invisibug(event.pos));
            }
        }
    }

    @EventHandler
    private static void onServerTick(ServerTickEvent event) {
        if (Config.invisibugHighlight && Utils.isInArea("Galatea")) {
            for (Invisibug bug : new ArrayList<>(invisibugList)) {
                bug.tick();
                if (bug.updateTicks == 0) {
                    invisibugList.remove(bug);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (Config.invisibugHighlight && Utils.isInArea("Galatea")) {
            for (Invisibug bug : new ArrayList<>(invisibugList)) {
                if (bug.positions.size() == 4) {
                    event.drawFilled(Box.of(bug.positions.getLast(), 1, 1, 1), false, invisibugColor);
                    event.drawText(bug.positions.getLast().add(0, 1, 0), Text.of("Invisibug"), 0.035f, false, RenderColor.fromHex(0xffffff));
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        invisibugList.clear();
    }

    private static class Invisibug {
        public int updateTicks = 20;
        public List<Vec3d> positions = new ArrayList<>();

        public Invisibug(Vec3d initialPos) {
            this.positions.add(initialPos);
        }

        public void tick() {
            if (this.updateTicks > 0) {
                this.updateTicks -= 1;
            }
        }

        public boolean isNear(Vec3d pos) {
            return !this.positions.isEmpty() && pos.distanceTo(this.positions.getLast()) <= 0.2;
        }

        public void add(Vec3d pos) {
            if (this.positions.size() == 4) {
                this.positions.removeFirst();
            }
            this.positions.add(pos);
            this.updateTicks = 20;
        }
    }
}
