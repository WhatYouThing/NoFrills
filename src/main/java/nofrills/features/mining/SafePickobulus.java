package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.ChestBlock;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import nofrills.config.Feature;
import nofrills.events.InteractBlockEvent;
import nofrills.events.InteractItemEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class SafePickobulus {
    public static final Feature instance = new Feature("safePickobulus");

    private static boolean shouldBlock() {
        return (Utils.isOnPrivateIsland() || Utils.isInGarden()) && Utils.getRightClickAbility(Utils.getHeldItem()).contains("Pickobulus");
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (instance.isActive() && shouldBlock()) {
            Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.MASTER, 1.0f, 0.0f);
            event.cancel();
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (instance.isActive() && shouldBlock()) {
            if (mc.world.getBlockState(event.blockHitResult.getBlockPos()).getBlock() instanceof ChestBlock) {
                return;
            }
            Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.MASTER, 1.0f, 0.0f);
            event.cancel();
        }
    }
}
