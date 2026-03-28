package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
            Vec3 pos = new Vec3(event.pos.getX(), event.pos.getY(), event.pos.getZ());
            if (Utils.horizontalDistance(beaconEntity.position(), pos) <= 4.0) {
                beaconPos = event.pos;
                beaconEntity = null;
            }
        }
    }

    @EventHandler
    private static void onUpdate(EntityUpdatedEvent event) {
        if (isActive() && event.entity instanceof ArmorStand stand) {
            ItemStack helmet = Utils.getEntityArmor(stand).getFirst();
            Entity boss = SlayerUtil.getBossEntity();
            if (!helmet.getItem().equals(Items.BEACON) || boss == null) return;
            if (Utils.horizontalDistance(boss.position(), event.entity.position()) <= 4.0) {
                beaconEntity = event.entity;
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (beaconPos != null && mc.level != null && isActive()) {
            if (!mc.level.getBlockState(beaconPos).getBlock().equals(Blocks.BEACON)) {
                beaconPos = null;
                return;
            }
            event.drawOutline(AABB.encapsulatingFullBlocks(beaconPos, beaconPos), false, color.value());
            event.drawTracer(beaconPos.getCenter(), color.value());
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        beaconEntity = null;
        beaconPos = null;
    }
}