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
import java.util.List;

public class LividSolver {
    public static final Feature instance = new Feature("lividSolver");

    public static final SettingColor color = new SettingColor(RenderColor.fromArgb(0xff00ff00), "color", instance.key());

    private static final HashMap<Block, Livid> lividData = buildLividData();
    private static final EntityCache lividCache = new EntityCache();
    private static String currentName = "";

    private static HashMap<Block, Livid> buildLividData() {
        HashMap<Block, Livid> map = new HashMap<>();
        List<Livid> list = List.of(
                new Livid("Hockey Livid", "§c§lRED", Blocks.RED_WOOL, Blocks.RED_STAINED_GLASS),
                new Livid("Arcade Livid", "§e§lYELLOW", Blocks.YELLOW_WOOL, Blocks.YELLOW_STAINED_GLASS),
                new Livid("Smile Livid", "§a§lGREEN", Blocks.LIME_WOOL, Blocks.LIME_STAINED_GLASS),
                new Livid("Frog Livid", "§2§lDARK GREEN", Blocks.GREEN_WOOL, Blocks.GREEN_STAINED_GLASS),
                new Livid("Scream Livid", "§9§lBLUE", Blocks.BLUE_WOOL, Blocks.BLUE_STAINED_GLASS),
                new Livid("Crossed Livid", "§d§lPINK", Blocks.MAGENTA_WOOL, Blocks.MAGENTA_STAINED_GLASS),
                new Livid("Purple Livid", "§5§lPURPLE", Blocks.PURPLE_WOOL, Blocks.PURPLE_STAINED_GLASS),
                new Livid("Doctor Livid", "§7§lGRAY", Blocks.GRAY_WOOL, Blocks.GRAY_STAINED_GLASS),
                new Livid("Vendetta Livid", "§f§lWHITE", Blocks.WHITE_WOOL, Blocks.WHITE_STAINED_GLASS)
        );
        for (Livid livid : list) {
            map.put(livid.wool, livid);
            map.put(livid.glass, livid);
        }
        return map;
    }

    @EventHandler
    private static void onBlock(BlockUpdateEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("5") && event.pos.getY() >= 107 && event.pos.getY() <= 110) {
            if (lividData.containsKey(event.newState.getBlock())) {
                Livid livid = lividData.get(event.newState.getBlock());
                if (!currentName.equals(livid.name)) {
                    Utils.showTitle(livid.title + "!", "", 0, 50, 10);
                    Utils.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
                    currentName = livid.name;
                }
            }
        }
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeonBoss("5") && event.entity instanceof PlayerEntity player && !Utils.isPlayer(player)) {
            String name = Utils.toPlainString(player.getName());
            if (!lividCache.has(event.entity) && lividData.values().stream().anyMatch(livid -> livid.name.equals(name))) {
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
                    break;
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
        public Block wool;
        public Block glass;

        public Livid(String name, String title, Block wool, Block glass) {
            this.name = name;
            this.title = title;
            this.wool = wool;
            this.glass = glass;
        }
    }
}