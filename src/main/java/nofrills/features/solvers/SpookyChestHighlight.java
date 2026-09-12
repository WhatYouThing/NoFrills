package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.*;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

@EventListener
public class SpookyChestHighlight {
    public static final Feature instance = new Feature("spookyChests");

    public static final SettingBool tracer = new SettingBool(false, "tracer", instance);
    public static final SettingColor color = new SettingColor(new RenderColor(1.0f, 0.67f, 0.0f, 0.67f), "color", instance);

    private static final EntityCache chestList = new EntityCache();
    private static final EntityCache clickedList = new EntityCache();

    private static boolean isSpookyChest(String name) {
        return name.equalsIgnoreCase("Trick or Treat?") || name.equalsIgnoreCase("Party Chest");
    }

    private static boolean isSpooky() {
        for (String line : SkyblockData.getLines()) {
            if (line.startsWith("Spooky Festival") && line.contains(":")) {
                return true;
            }
        }
        return false;
    }

    private static void clickChest(Entity ent) {
        if (ent instanceof ArmorStand) {
            BlockPos pos = ent.blockPosition();
            chestList.get().stream()
                    .filter(e -> e.getBlockX() == pos.getX() && e.getBlockZ() == pos.getZ())
                    .findFirst()
                    .ifPresent(clickedList::add);
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && isSpookyChest(event.namePlain) && isSpooky() && !chestList.has(event.entity)) {
            chestList.add(event.entity);
        }
    }

    @EventHandler
    private static void onInteractEntity(InteractEntityEvent event) {
        if (instance.isActive() && !chestList.empty() && isSpooky()) clickChest(event.entity);
    }

    @EventHandler
    private static void onAttackEntity(AttackEntityEvent event) {
        if (instance.isActive() && !chestList.empty() && isSpooky()) clickChest(event.entity);
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !chestList.empty()) {
            for (Entity chest : chestList.get()) {
                if (clickedList.has(chest)) {
                    continue;
                }
                BlockPos pos = Utils.findGround(chest.blockPosition(), 4).above(1);
                event.drawFilledWithBeam(AABB.encapsulatingFullBlocks(pos, pos), 256, true, color.value());
                if (tracer.value()) event.drawTracer(Vec3.atCenterOf(pos), color.valueWithAlpha(1.0f));
            }
        }
    }
}
