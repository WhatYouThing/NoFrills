package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Config;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class EventFeatures {
    private static final List<Entity> chestList = new ArrayList<>();

    private static boolean isSpooky() {
        for (String line : SkyblockData.getLines()) {
            if (line.startsWith("Spooky Festival") && line.contains(":")) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (isSpooky() && (Config.spookyChestAlert || Config.spookyChestHighlight)) {
            String name = event.namePlain.toLowerCase();
            if (name.equals("trick or treat?") || name.equals("party chest")) {
                if (!chestList.contains(event.entity) && event.entity.distanceTo(mc.player) <= 16.0f) {
                    if (Config.spookyChestAlert) {
                        Utils.showTitle("§6§lCHEST SPAWNED!", "", 5, 20, 5);
                        Utils.playSound(SoundEvents.BLOCK_VAULT_ACTIVATE, SoundCategory.MASTER, 1.0f, 1.0f);
                    }
                    if (Config.spookyChestHighlight) {
                        chestList.add(event.entity);
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (Config.spookyChestHighlight && !chestList.isEmpty()) {
            List<Entity> chests = new ArrayList<>(chestList);
            for (Entity chest : chests) {
                if (chest.isRemoved()) {
                    chestList.remove(chest);
                } else {
                    BlockPos pos = Utils.findGround(chest.getBlockPos(), 4);
                    event.drawFilledWithBeam(Box.enclosing(pos, pos), 128, true, RenderColor.fromColor(Config.spookyChestHighlightColor));
                }
            }
        }
    }
}
