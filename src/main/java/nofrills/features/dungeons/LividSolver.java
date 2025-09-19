package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

    private static final HashMap<Block, Livid> lividData = new HashMap<>();
    private static final EntityCache lividCache = new EntityCache();
    private static String currentName = "";

    static {
        lividData.put(Blocks.RED_WOOL, new Livid("Hockey Livid", "§c§lRED"));
        lividData.put(Blocks.YELLOW_WOOL, new Livid("Arcade Livid", "§e§lYELLOW"));
        lividData.put(Blocks.LIME_WOOL, new Livid("Smile Livid", "§a§lGREEN"));
        lividData.put(Blocks.GREEN_WOOL, new Livid("Frog Livid", "§2§lDARK GREEN"));
        lividData.put(Blocks.BLUE_WOOL, new Livid("Scream Livid", "§9§lBLUE"));
        lividData.put(Blocks.MAGENTA_WOOL, new Livid("Crossed Livid", "§d§lPINK"));
        lividData.put(Blocks.PURPLE_WOOL, new Livid("Purple Livid", "§5§lPURPLE"));
        lividData.put(Blocks.GRAY_WOOL, new Livid("Doctor Livid", "§7§lGRAY"));
        lividData.put(Blocks.WHITE_WOOL, new Livid("Vendetta Livid", "§f§lWHITE"));
    }

    @EventHandler
    private static void onBlock(BlockUpdateEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("5") && event.pos.getY() == 108 && lividData.containsKey(event.newState.getBlock())) {
            Livid livid = lividData.get(event.newState.getBlock());
            if (!currentName.equals(livid.name)) {
                Utils.showTitle(livid.title + "!", "", 0, 50, 10);
                Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                currentName = livid.name;
            }
        }
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("5") && event.entity instanceof PlayerEntity player && !Utils.isPlayer(player)) {
            String name = Utils.toPlainString(player.getName());
            if (lividData.values().stream().anyMatch(livid -> livid.name.equals(name))) {
                lividCache.add(player);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !Utils.isInstanceOver() && Utils.isInDungeonBoss("5")) {
            for (Entity livid : lividCache.get()) {
                if (Utils.toPlainString(livid.getName()).equals(currentName)) {
                    event.drawOutline(Utils.getLerpedBox(livid, event.tickCounter.getTickProgress(true)), false, color.value());
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

    private static class Livid {
        public String name;
        public String title;

        public Livid(String name, String title) {
            this.name = name;
            this.title = title;
        }
    }
}