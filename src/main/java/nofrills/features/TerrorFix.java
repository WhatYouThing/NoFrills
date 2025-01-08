package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.PlaySoundEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class TerrorFix {
    private static int terrorPieces = 0;

    private static int countPieces() {
        int pieces = 0;
        for (ItemStack piece : mc.player.getArmorItems()) {
            String pieceName = Formatting.strip(piece.getName().getString());
            if (pieceName.contains("Terror")) {
                pieces++;
            }
        }
        return pieces;
    }

    @EventHandler
    public static void onTick(WorldTickEvent event) {
        if (Config.terrorFix) {
            terrorPieces = countPieces();
        }
    }

    @EventHandler
    public static void onSound(PlaySoundEvent event) {
        if (Config.terrorFix && terrorPieces >= 2) {
            if (event.isSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER) && event.packet.getPitch() == 0.7936508f) {
                Utils.playSound(SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.MASTER, 3.0f, 1.0f);
                Utils.playSound(SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.MASTER, 3.0f, 1.0f);
            }
            if (event.isSound(SoundEvents.BLOCK_PISTON_EXTEND) || event.isSound(SoundEvents.BLOCK_PISTON_CONTRACT)) {
                event.cancel();
            }
        }
    }
}
