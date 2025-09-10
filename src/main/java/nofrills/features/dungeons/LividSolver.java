package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.BlockUpdateEvent;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.HashMap;

public class LividSolver {
    public static final Feature instance = new Feature("lividSolver");

    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xff00ff00), "color", instance.key());

    private static final HashMap<Block, String> lividData = buildLividData();
    private static final EntityCache lividCache = new EntityCache();
    private static String currentName = "";

    private static HashMap<Block, String> buildLividData() {
        HashMap<Block, String> data = new HashMap<>();
        data.put(Blocks.RED_WOOL, "Hockey Livid");
        data.put(Blocks.YELLOW_WOOL, "Arcade Livid");
        data.put(Blocks.LIME_WOOL, "Smile Livid");
        data.put(Blocks.GREEN_WOOL, "Frog Livid");
        data.put(Blocks.BLUE_WOOL, "Scream Livid");
        data.put(Blocks.MAGENTA_WOOL, "Crossed Livid");
        data.put(Blocks.PURPLE_WOOL, "Purple Livid");
        data.put(Blocks.GRAY_WOOL, "Doctor Livid");
        data.put(Blocks.WHITE_WOOL, "Vendetta Livid");
        return data;
    }

    private static void announceColor(Block block) {
        String name = Utils.format("{}!", block.getName().getString().replace(" Wool", "").trim().toUpperCase());
        Utils.showTitleCustom(name, 60, -20, 4.0f, block.getDefaultMapColor().color);
        Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
    }

    @EventHandler
    private static void onBlock(BlockUpdateEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("5") && event.pos.getY() == 110) {
            Block block = event.newState.getBlock();
            if (!lividData.containsKey(block)) return;
            String name = lividData.get(block);
            if (!currentName.equals(name)) {
                announceColor(block);
                currentName = lividData.get(block);
            }
        }
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("5") && event.entity instanceof PlayerEntity player && !Utils.isPlayer(player)) {
            if (player.getName().getString().endsWith(" Livid")) {
                lividCache.add(player);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !Utils.isInstanceOver() && Utils.isInDungeonBoss("5")) {
            for (Entity livid : lividCache.get()) {
                if (livid.getName().getString().equals(currentName)) {
                    Box box = livid.getDimensions(EntityPose.STANDING).getBoxAt(livid.getLerpedPos(event.tickCounter.getTickProgress(true)));
                    event.drawOutline(box, false, color.value());
                    return;
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        currentName = "";
        lividCache.clear();
    }
}
