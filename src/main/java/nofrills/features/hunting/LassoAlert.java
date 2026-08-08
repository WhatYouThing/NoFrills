package nofrills.features.hunting;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import nofrills.config.Feature;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EventListener;
import nofrills.hud.HudManager;
import nofrills.misc.Utils;

import java.util.List;

import static nofrills.Main.mc;

@EventListener
public class LassoAlert {
    public static final Feature instance = new Feature("lassoAlert");

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && mc.player != null && event.namePlain.equals("REEL")) {
            List<Leashable> leashed = Leashable.leashableLeashedTo(mc.player);
            if (leashed.isEmpty()) return;
            for (Leashable leashable : leashed) {
                if (leashable instanceof LivingEntity living && Utils.horizontalDistance(event.entity, living) <= 2.0) {
                    HudManager.setCustomTitle(Component.literal("REEL").withStyle(ChatFormatting.YELLOW), 20);
                    Utils.playSound(SoundEvents.NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    break;
                }
            }
        }
    }
}