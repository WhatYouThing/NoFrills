package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.BlockUpdateEvent;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class BeaconTracer {
    public static final Feature instance = new Feature("beaconTracer");

    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xff00ffff), "color", instance);

    private static Entity beaconEntity = null;
    private static BlockPos beaconPos = null;

    private static boolean isActive() {
        return instance.isActive() && Utils.isInArea("The End") && SlayerUtil.isFightingBoss(SlayerUtil.voidgloom);
    }

    @EventHandler
    private static void onBlock(BlockUpdateEvent event) {
        if (isActive() && beaconEntity != null && event.newState.getBlock().equals(Blocks.BEACON)) {
            Vec3d pos = new Vec3d(event.pos.getX(), event.pos.getY(), event.pos.getZ());
            if (Utils.horizontalDistance(beaconEntity.getEntityPos(), pos) <= 4.0) {
                beaconPos = event.pos;
                beaconEntity = null;
            }
        }
    }

    @EventHandler
    private static void onUpdate(EntityUpdatedEvent event) {
        if (isActive() && event.entity instanceof ArmorStandEntity stand) {
            ItemStack helmet = Utils.getEntityArmor(stand).getFirst();
            Entity boss = SlayerUtil.getBossEntity();
            if (!helmet.getItem().equals(Items.BEACON) || boss == null) return;
            if (Utils.horizontalDistance(boss.getEntityPos(), event.entity.getEntityPos()) <= 4.0) {
                beaconEntity = event.entity;
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (beaconPos != null && mc.world != null && isActive()) {
            if (!mc.world.getBlockState(beaconPos).getBlock().equals(Blocks.BEACON)) {
                beaconPos = null;
                return;
            }
            event.drawOutline(Box.enclosing(beaconPos, beaconPos), false, color.value());
            event.drawTracer(beaconPos.toCenterPos(), color.value());
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        beaconEntity = null;
        beaconPos = null;
    }
}