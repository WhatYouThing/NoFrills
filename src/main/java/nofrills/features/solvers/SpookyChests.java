package nofrills.features.solvers;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.*;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SpookyChests {
    public static final Feature instance = new Feature("spookyChests");

    public static final SettingBool tracer = new SettingBool(false, "tracer", instance);
    public static final SettingColor color = new SettingColor(new RenderColor(1.0f, 0.67f, 0.0f, 0.67f), "color", instance);

    private static final EntityCache chestList = new EntityCache();
    private static final EntityCache clickedList = new EntityCache();

    private static boolean isSpooky() {
        for (String line : SkyblockData.getLines()) {
            if (line.startsWith("Spooky Festival") && line.contains(":")) {
                return true;
            }
        }
        return false;
    }

    private static void clickChest(Entity ent) {
        List<Entity> chests = new ArrayList<>(chestList.get());
        chests.sort(Comparator.comparingDouble(chest -> Utils.horizontalDistance(ent, chest)));
        if (!chests.isEmpty() && Utils.horizontalDistance(ent, chests.getFirst()) <= 2.0) {
            clickedList.add(chests.getFirst());
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && isSpooky() && event.entity.isCustomNameVisible() && !chestList.has(event.entity)) {
            String name = Utils.toLower(event.namePlain);
            if (name.equals("trick or treat?") || name.equals("party chest")) {
                Utils.showTitle("§6§lCHEST SPAWNED!", "", 5, 20, 5);
                Utils.playSound(SoundEvents.BLOCK_VAULT_ACTIVATE, SoundCategory.MASTER, 1.0f, 1.0f);
                chestList.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onInteractEntity(InteractEntityEvent event) {
        if (instance.isActive() && isSpooky()) clickChest(event.entity);
    }

    @EventHandler
    private static void onAttackEntity(AttackEntityEvent event) {
        if (instance.isActive() && isSpooky()) clickChest(event.entity);
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !chestList.empty()) {
            for (Entity chest : chestList.get()) {
                if (clickedList.removeDead().has(chest)) {
                    continue;
                }
                BlockPos pos = Utils.findGround(chest.getBlockPos(), 4).up(1);
                event.drawFilledWithBeam(Box.enclosing(pos, pos), 256, true, color.value());
                if (tracer.value()) event.drawTracer(pos.toCenterPos(), color.valueWithAlpha(1.0f));
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        chestList.clear();
        clickedList.clear();
    }
}
