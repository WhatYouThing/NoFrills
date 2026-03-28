package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.BlockUpdateEvent;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;
import nofrills.misc.Utils;

import java.util.HashMap;
import java.util.List;

public class LividSolver {
    public static final Feature instance = new Feature("lividSolver");

    public static final SettingBool title = new SettingBool(true, "title", instance);
    public static final SettingBool highlight = new SettingBool(true, "highlight", instance);
    public static final SettingBool tracer = new SettingBool(false, "tracer", instance);
    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Outline, RenderStyle.class, "style", instance);
    public static final SettingColor outlineColor = new SettingColor(RenderColor.fromArgb(0xff00ff00), "color", instance);
    public static final SettingColor fillColor = new SettingColor(RenderColor.fromHex(0x00ff00, 0.5f), "fillColor", instance);
    public static final SettingColor tracerColor = new SettingColor(RenderColor.fromArgb(0xff00ff00), "tracerColor", instance);

    private static final HashMap<Block, Livid> lividData = buildLividData();
    private static final EntityCache lividCache = new EntityCache();
    private static String currentName = "";

    private static boolean isActive() {
        return instance.isActive() && Utils.isInDungeonBoss("5");
    }

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
        if (isActive() && event.pos.getY() >= 107 && event.pos.getY() <= 110 && lividData.containsKey(event.newState.getBlock())) {
            Livid livid = lividData.get(event.newState.getBlock());
            if (!currentName.equals(livid.name)) {
                if (title.value()) {
                    Utils.showTitle(livid.title + "!", "", 0, 50, 10);
                    Utils.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 0);
                }
                currentName = livid.name;
            }
        }
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (isActive() && event.entity instanceof Player player && !Utils.isPlayer(player)) {
            String name = Utils.toPlain(player.getName());
            if (!lividCache.has(event.entity) && lividData.values().stream().anyMatch(livid -> livid.name.equals(name))) {
                lividCache.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (isActive() && lividCache.size() > 1) {
            for (Entity livid : lividCache.get()) {
                if (Utils.toPlain(livid.getName()).equals(currentName)) {
                    float delta = event.tickCounter.getGameTimeDeltaPartialTick(true);
                    AABB box = Utils.getLerpedBox(livid, delta);
                    if (highlight.value()) {
                        event.drawStyled(box, style.value(), false, outlineColor.value(), fillColor.value());
                    }
                    if (tracer.value()) {
                        event.drawTracer(box.getCenter(), tracerColor.value());
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        currentName = "";
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