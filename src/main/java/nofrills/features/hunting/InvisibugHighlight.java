package nofrills.features.hunting;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.events.SpawnParticleEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class InvisibugHighlight {
    public static final Feature instance = new Feature("invisibugHighlight");

    public static final SettingColor color = new SettingColor(RenderColor.fromHex(0xff0000, 0.5f), "color", instance.key());

    private static final List<Invisibug> invisibugList = new ArrayList<>();

    private static boolean isInvisibugParticle(ClientboundLevelParticlesPacket packet) {
        return packet.getCount() == 1 && packet.getMaxSpeed() == 0.0f && packet.getXDist() == 0.0f
                && packet.getYDist() == 0.0f && packet.getZDist() == 0.0f
                && packet.alwaysShow() && packet.isOverrideLimiter();
    }

    private static boolean hasInvisibugMarker(Vec3 pos) {
        List<Entity> other = Utils.getOtherEntities(mc.player, AABB.ofSize(pos, 1, 2, 1), null);
        if (other.size() == 1 && other.getFirst() instanceof ArmorStand marker) {
            return marker.isMarker() && marker.getCustomName() == null && marker.getMainHandItem().isEmpty();
        }
        return false;
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && Utils.isInArea("Galatea") && event.type.equals(ParticleTypes.CRIT) && isInvisibugParticle(event.packet)) {
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
        if (instance.isActive() && Utils.isInArea("Galatea")) {
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
        if (instance.isActive() && Utils.isInArea("Galatea")) {
            for (Invisibug bug : new ArrayList<>(invisibugList)) {
                if (bug.positions.size() == 4) {
                    event.drawText(bug.positions.getLast().add(0, 1, 0), Component.nullToEmpty("Invisibug"), 0.035f, false, RenderColor.white);
                    event.drawFilled(AABB.ofSize(bug.positions.getLast(), 1, 1, 1), false, color.value());
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
        public List<Vec3> positions = new ArrayList<>();

        public Invisibug(Vec3 initialPos) {
            this.positions.add(initialPos);
        }

        public void tick() {
            if (this.updateTicks > 0) {
                this.updateTicks -= 1;
            }
        }

        public boolean isNear(Vec3 pos) {
            return !this.positions.isEmpty() && pos.distanceTo(this.positions.getLast()) <= 0.3;
        }

        public void add(Vec3 pos) {
            if (this.positions.size() == 4) {
                this.positions.removeFirst();
            }
            this.positions.add(pos);
            this.updateTicks = 20;
        }
    }
}
