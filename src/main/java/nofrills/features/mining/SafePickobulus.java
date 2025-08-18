package nofrills.features.mining;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import nofrills.config.Feature;
import nofrills.events.InteractBlockEvent;
import nofrills.events.InteractItemEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class SafePickobulus {
    public static final Feature instance = new Feature("safePickobulus");

    private static boolean pickobulusCheck() {
        return (Utils.isOnPrivateIsland() || Utils.isInGarden()) && Utils.getRightClickAbility(Utils.getHeldItem()).contains("Pickobulus");
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (instance.isActive() && pickobulusCheck()) {
            Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.MASTER, 1.0f, 0.0f);
            event.cancel();
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (instance.isActive() && pickobulusCheck()) {
            BlockPos pos = event.blockHitResult.getBlockPos();
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!(block instanceof ChestBlock)) {
                Utils.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.MASTER, 1.0f, 0.0f);
                event.cancel();
            }
        }
    }
}
